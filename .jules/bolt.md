## 2026-08-25 - Cache WebGL Uniform Locations
**Learning:** Calling `gl.getUniformLocation` repeatedly inside hot rendering loops (such as per-frame mesh, entity, or line draws) forces driver string lookups and overhead.
**Action:** Cache uniform locations on the WebGLProgram instance (`p.locs = p.locs || {}; return p.locs[n] || (p.locs[n] = gl.getUniformLocation(p, n))`) inside the `uni(p, n)` helper function.
