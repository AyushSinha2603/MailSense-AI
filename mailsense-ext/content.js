// MailSense AI — Gmail inline reply generator
const BACKEND_URL = "http://localhost:8080/api/email/generate";
const TONES = [
  ["my_tone", "My Tone"],
  ["professional", "Professional"],
  ["casual", "Casual"],
  ["friendly", "Friendly"],
  ["concise", "Concise"],
  ["empathetic", "Empathetic"],
  ["neutral", "Neutral"]
];
console.log("[MailSense] content script loaded");

function isSendButton(el) {
  const label = (el.getAttribute("aria-label") || el.getAttribute("data-tooltip") || "").trim();
  return label.startsWith("Send") && !label.startsWith("Send &");
}

function findSendButtons() {
  return Array.from(document.querySelectorAll('div[role="button"]')).filter(isSendButton);
}

// Walk up from Send until we hit an ancestor that contains the editable message body.
// Works for inline replies AND popup compose windows.
function findComposeBody(sendBtn) {
  const selector =
    'div[contenteditable="true"][aria-label="Message Body"], ' +
    'div[contenteditable="true"][role="textbox"], ' +
    'div[g_editable="true"]';
  let node = sendBtn.parentElement;
  for (let i = 0; i < 15 && node; i++, node = node.parentElement) {
    const body = node.querySelector(selector);
    if (body) return body;
  }
  return null;
}

function getOriginalEmailText() {
  // Rendered message bodies in the thread view
  const bodies = Array.from(document.querySelectorAll(".a3s.aiL, .a3s"))
    .filter(el => el.offsetParent !== null && el.innerText.trim().length > 0);
  if (bodies.length === 0) return "";
  return bodies[bodies.length - 1].innerText.trim();
}

function insertAtStart(el, text) {
  el.focus();
  const range = document.createRange();
  range.setStart(el, 0);
  range.collapse(true);
  const sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(range);
  document.execCommand("insertText", false, text + "\n\n");
}

async function generateReply(button, select, sendBtn) {
  const composeBody = findComposeBody(sendBtn);
  const emailContent = getOriginalEmailText();
  const originalLabel = "Generate Reply";

  if (!composeBody || !emailContent) {
    console.warn("[MailSense] compose body found:", !!composeBody, "| email text found:", !!emailContent);
    button.textContent = !composeBody ? "No compose box" : "No email found";
    setTimeout(() => (button.textContent = originalLabel), 2000);
    return;
  }

  button.textContent = "Generating...";
  button.style.pointerEvents = "none";

  try {
    const res = await fetch(BACKEND_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ emailContent, tone: select.value })
    });
    const text = await res.text();
    if (!res.ok) throw new Error(text);
    insertAtStart(composeBody, text.replace(/^"|"$/g, ""));
    button.textContent = originalLabel;
  } catch (err) {
    console.error("[MailSense] error:", err);
    button.textContent = "Failed — check backend";
    setTimeout(() => (button.textContent = originalLabel), 2500);
  }
  button.style.pointerEvents = "auto";
}

function injectButton(sendBtn) {
  const group = sendBtn.parentElement;
  if (!group) return;

  // Already injected and still in the DOM? Skip.
  if (group.nextElementSibling?.classList.contains("mailsense-wrapper")) return;

  // Only inject into real compose areas
  if (!findComposeBody(sendBtn)) return;

  const wrapper = document.createElement("div");
  wrapper.className = "mailsense-wrapper";

  const select = document.createElement("select");
  select.className = "mailsense-tone";
  TONES.forEach(([value, label]) => {
    const opt = document.createElement("option");
    opt.value = value;
    opt.textContent = label;
    select.appendChild(opt);
  });
  const button = document.createElement("div");
  button.className = "mailsense-btn";
  button.setAttribute("role", "button");
  button.textContent = "Generate Reply";
  button.addEventListener("click", () => generateReply(button, select, sendBtn));

  wrapper.appendChild(select);
  wrapper.appendChild(button);
  group.insertAdjacentElement("afterend", wrapper);
  console.log("[MailSense] button injected");
}

let scheduled = false;
function scan() {
  if (scheduled) return;
  scheduled = true;
  setTimeout(() => {
    scheduled = false;
    findSendButtons().forEach(injectButton);
  }, 300);
}

new MutationObserver(scan).observe(document.body, { childList: true, subtree: true });
scan();