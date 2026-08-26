## 2025-02-26 - WebGL Uniform Location Caching
**Learning:** `gl.getUniformLocation` queries trigger WebGL driver lookups on every invocation. Caching uniform locations lazily on the `WebGLProgram` instance (`p.locs`) eliminates CPU driver overhead across frame renders.
**Action:** Always cache uniform locations on shader program instances rather than looking them up dynamically during draw calls.
