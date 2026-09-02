## 2026-09-02 - WebGL Uniform Location Caching
**Learning:** `gl.getUniformLocation` invokes WebGL driver string lookups into the compiled shader program binary. Calling it multiple times per frame for every chunk mesh and object being drawn accumulates significant CPU overhead, especially on mobile/Android GPU drivers.
**Action:** Store `p.locs` on the `WebGLProgram` instance and lazy-cache uniform locations on first access in helper functions like `uni(p, n)`.
