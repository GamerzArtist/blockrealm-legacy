## 2025-05-18 - Sanitize User Input in Debug HUD Overlays

**Vulnerability:** DOM XSS vulnerability in `updateDebug()` where unescaped user input (`currentWorld.seed`) was directly interpolated into `dom.debug.innerHTML`.
**Learning:** Even internal diagnostic overlays that render state strings via `innerHTML` can become XSS vectors if user-customizable values like world seeds are not escaped.
**Prevention:** Always escape user-controllable strings with `escapeHTML` when assigning to `innerHTML`, or construct DOM nodes using `.textContent`.
