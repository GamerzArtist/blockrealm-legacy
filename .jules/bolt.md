## 2025-10-09 - Hoist frame-static WebGL program uniforms in multi-chunk mesh rendering
**Learning:** Setting WebGL uniforms and activating programs (`gl.useProgram`, `gl.uniform*`, `gl.bindTexture`) inside per-chunk iteration loops creates hundreds of redundant JavaScript-to-C++ WebGL driver calls per frame when rendering chunked voxel terrain.
**Action:** Always hoist program activation, texture binding, and frame-static uniforms into pass-level functions (`prepareWorldProgram`) invoked once per render pass before iterating over chunk meshes.
