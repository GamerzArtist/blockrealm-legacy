## 2025-05-18 - DOM-based XSS in FPS Debug Overlay
**Vulnerability:** Unsanitized world seed string injected directly into innerHTML in `updateDebug()`.
**Learning:** Seed strings created by users or stored in world metadata are rendered as raw HTML in the FPS overlay template string.
**Prevention:** Sanitize user-controlled text string parameters with `escapeHtml()` before interpolating into DOM `innerHTML` string templates.
