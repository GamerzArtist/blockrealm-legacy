# Sentinel's Journal - Critical Security Learnings

## 2026-03-05 - DOM-based XSS via World Seed in Debug Panel
**Vulnerability:** DOM-based Cross-Site Scripting (XSS) in the FPS/debug overlay.
**Learning:** The world seed input from the "Create World" form was displayed directly inside `dom.debug.innerHTML` in the `updateDebug()` function. Because the seed was inserted unescaped into a template string and set via `innerHTML`, an attacker could craft a world seed with HTML/JS payload (e.g., `<img src=x onerror=alert(1)>`) which would execute when the FPS/debug overlay was active.
**Prevention:** Always escape user-controlled strings (like world names, seeds, custom labels) before inserting them into HTML content, or use `textContent`/secure DOM creation APIs instead of `innerHTML` for dynamic values.
