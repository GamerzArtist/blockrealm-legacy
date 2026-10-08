## 2026-10-08 - Sanitize debug overlay world seed against DOM-based XSS
**Vulnerability:** Unsanitized user-controlled world seed string interpolated directly into `dom.debug.innerHTML` in `updateDebug()`.
**Learning:** `innerHTML` rendering of dynamic state variables allows HTML markup/scripts in user inputs (like world seeds) to be executed in DOM context.
**Prevention:** Always sanitize strings with an HTML escaper (e.g., `escapeHtml`) or use `textContent` when updating DOM elements with user-controlled input.
