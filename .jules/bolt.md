## 2026-10-14 - WebGL Uniform Location Caching
**Learning:** Calling `gl.getUniformLocation` inside per-frame or per-mesh rendering routines introduces significant CPU overhead due to driver reflection string queries.
**Action:** Always cache `WebGLUniformLocation` references directly on the program instance (`p.locs = p.locs || {}`) when helper functions query uniform locations during frame rendering.
