## 2026-09-26 - Sanitize World Seed in Debug Overlay
**Vulnerability:** User-provided world seed strings were inserted raw into `dom.debug.innerHTML` in `updateDebug()`, exposing the game overlay to DOM-based XSS if a world seed contained HTML markup or malicious scripts.
**Learning:** Rendering state or debug HUD overlays using innerHTML string interpolation without HTML entity escaping can allow arbitrary script execution from user input or saved world metadata.
**Prevention:** Always sanitize strings using `escapeHtml()` or manipulate DOM node properties (`textContent`) directly when inserting untrusted or user-supplied strings into innerHTML template literals.
