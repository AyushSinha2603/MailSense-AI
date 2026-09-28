# MailSense-AI

A Gmail extension plus Spring Boot backend that drafts email replies with Google Gemini.
Pick a tone, or use **My Tone**, which retrieves your own past emails (RAG) so drafts sound like you.

<!-- Add a screenshot or GIF here: ![demo](docs/demo.gif) -->

## Features

- **Generate Reply button inside Gmail**, next to Send, with a tone dropdown
- Preset tones: professional, casual, friendly, concise, empathetic, neutral
- **My Tone**: retrieval-augmented generation over your own sent emails
- Retries and a fallback model when Gemini is overloaded
- Training page to add your emails: `http://localhost:8080/train.html`

## Architecture

```
Gmail (content.js) --> POST /api/email/generate --> EmailGeneratorService
                                                     |-- preset tone: build prompt
                                                     '-- My Tone: embed email -> top-5 similar
                                                         past emails -> few-shot prompt
                                                                   |
                                             Gemini generateContent (retry + fallback)
Training: train.html --> POST /api/style/ingest --> embeddings --> style-store.json
```

## How My Tone works

1. Each email you save is embedded (`gemini-embedding-001`, 768 dimensions) and stored locally in `style-store.json`.
2. For a new email, the backend embeds it and finds the 5 most similar saved emails by cosine similarity (exact search, in memory).
3. Those emails go into the prompt as style examples, and Gemini writes the reply.

This is basic RAG, used to retrieve *style examples*, not facts. No vector database is needed at this scale.

## Tech stack

Java 21, Spring Boot 3.5, Maven, WebClient, Jackson, Lombok / React (Vite) / Chrome Extension (Manifest V3) / Google Gemini API

## Setup

**Backend**
1. Install JDK 21.
2. Get a key from [Google AI Studio](https://aistudio.google.com/apikey).
3. Set it as an environment variable (never commit it): `GEMINI_KEY=your_key`
4. Run `MailSenseAiApplication` (or `./mvnw spring-boot:run` from the backend folder).

**Extension**
1. Open `chrome://extensions` and enable Developer mode.
2. Click **Load unpacked** and select the `mailsense-ext` folder.
3. Open Gmail, click Reply, and use the button next to Send.

**My Tone**
1. Open `http://localhost:8080/train.html`.
2. Paste emails you wrote, separated by a line containing only `--`, and save.

## API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/email/generate` | `{emailContent, tone}` returns the reply text |
| POST | `/api/style/ingest` | `{emails: [...]}` embeds and saves samples |
| GET | `/api/style/count` | number of saved samples |
| DELETE | `/api/style` | delete all saved samples |

## Privacy

Your emails are sent to Google's API for embedding and inside prompts. Samples are stored
locally in `style-store.json`, which is git-ignored. Don't paste anything sensitive.

## Known limitations

- Runs on localhost only, and the API has no authentication (local use only)
- Exact brute-force search over a JSON file; suitable for hundreds of emails, not millions
- Retrieval is by topic similarity, not writing style
- Gmail's page structure changes, so selectors may need updating
- No automated evaluation of draft quality yet

## Roadmap

- Store (email received, reply sent) pairs instead of replies only
- Evaluation: compare drafts to real replies
- pgvector and per-user auth for multi-user use
- Route requests through the extension's background worker and restrict CORS
