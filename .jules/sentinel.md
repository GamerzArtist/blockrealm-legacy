# Sentinel Security Journal

## 2026-03-05 - Stored XSS in Voxel Game Seed Debug Render
**Vulnerability:** A Stored Cross-Site Scripting (XSS) vulnerability existed in the web game's debug panel render logic. The application accepted arbitrary, unsanitized user input for the world seed during creation and saved it inside persistent storage (`STORE`/localStorage). When the FPS debug display was enabled, the seed was output directly into `dom.debug.innerHTML` via template literal concatenation without any HTML escaping.
**Learning:** Even in purely client-side static sandbox environments, user inputs saved persistently and output directly inside `innerHTML` elements present a Stored XSS risk, which is especially hazardous if other scripts or extensions run in the same origin.
**Prevention:** Always escape or sanitize user-defined variables before inserting them into elements using `innerHTML`, or prefer using safer alternatives like `textContent` where applicable.
