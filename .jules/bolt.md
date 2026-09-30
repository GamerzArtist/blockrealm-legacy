## 2026-09-30 - WebGL Uniform Location Caching
**Learning:** Calling `gl.getUniformLocation(p, name)` every frame across dozens of chunk meshes and rendered entities creates significant WebGL driver string lookup overhead.
**Action:** Cache uniform locations on the WebGLProgram instance (`p.locs`) using `p.locs = p.locs || {}; if (!(n in p.locs)) p.locs[n] = gl.getUniformLocation(p, n); return p.locs[n]` to eliminate redundant driver queries per frame.
