## 2026-08-23 - WebGL Uniform Location Lookups in Render Loops
**Learning:** `gl.getUniformLocation` in WebGL is an expensive driver lookup. Calling `uni(p, n)` directly inside hot per-chunk and per-entity render loops generates ~1,000 driver queries per frame at 60 FPS.
**Action:** Always cache `WebGLUniformLocation` references on the program instance (`p.locs = p.locs || {}`) so subsequent uniform updates perform $O(1)$ property lookups.
