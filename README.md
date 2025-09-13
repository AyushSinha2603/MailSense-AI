# ✨ MailSense AI ✨

## Project Description

MailSense AI is a full-stack AI email assistant designed to streamline your inbox. 📧 This project leverages the power of generative AI to summarize emails and draft intelligent replies automatically. It’s a complete web application with a modern React frontend and a robust Spring Boot backend, showcasing an end-to-end development approach.

## Key Features

* **Intelligent Summaries** 📝: Uses the Google Gemini API to condense long emails into key points.

* **AI-Powered Drafting** ✍️: Generates smart, context-aware email responses with a single click.

* **User-Friendly Interface** 💻: A slick, responsive React frontend provides a seamless user experience.

* **Robust Backend** ⚙️: A scalable RESTful API built with Spring Boot ensures reliable performance.

## Tech Stack

| Component | Technologies | 
 | ----- | ----- | 
| **Frontend** | `React`, `JavaScript` | 
| **Backend** | `Java`, `Spring Boot`, `Spring AI` | 
| **AI Model** | `Google Gemini API` | 
| **Build Tool** | `Maven` | 

## Getting Started

### Prerequisites

* JDK 17+

* Maven

* Node.js & npm

### Installation & Execution

1. Clone the repository and navigate to the project root.

2. **Backend Setup**:

   * Create an `application.properties` file in `src/main/resources`.

   * Add your Gemini API key: `spring.ai.google.gemini.api-key=YOUR_API_KEY`

   * Run with Maven: `./mvnw spring-boot:run`

3. **Frontend Setup**:

   * Navigate to the frontend directory.

   * Install dependencies: `npm install`

   * Start the React app: `npm start`

4. Enjoy your full-stack AI assistant! 🚀
