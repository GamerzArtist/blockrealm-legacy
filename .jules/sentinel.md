## 2025-03-08 - Escape User Input in Debug InnerHTML
**Vulnerability:** DOM-based XSS when user-supplied world seed strings containing HTML/script tags were rendered via `innerHTML` in `updateDebug()`.
**Learning:** String template interpolation into `innerHTML` is dangerous when handling user input like world seeds or names, even in developer overlays.
**Prevention:** Use an HTML escaping utility (`escapeHtml`) or DOM text nodes (`textContent`) whenever outputting dynamic user properties into HTML strings.
