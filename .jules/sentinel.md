## 2026-09-24 - DOM-based XSS in Debug HUD Seed Rendering
**Vulnerability:** User-supplied world seed strings were inserted into `dom.debug.innerHTML` without escaping in `updateDebug()`.
**Learning:** `innerHTML` template strings easily introduce DOM-based XSS when dynamic string properties like world seed contain unescaped HTML characters.
**Prevention:** Sanitize user-provided strings with `escapeHtml()` or use `textContent` when updating innerHTML or DOM properties.
