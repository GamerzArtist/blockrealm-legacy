## 2026-08-20 - Unsanitized User-Controlled Seed in DOM InnerHTML
**Vulnerability:** User-controlled world seed string was directly interpolated into `dom.debug.innerHTML` in `updateDebug()`, creating a DOM-based Cross-Site Scripting (XSS) vulnerability when FPS overlay is enabled.
**Learning:** Even diagnostic and debug overlays that concatenate state into `innerHTML` can execute arbitrary script tags or image error handlers if populated with user input.
**Prevention:** Always sanitize strings using HTML entity escaping (`escapeHTML`) before inserting dynamic user input into `innerHTML` template literals.
