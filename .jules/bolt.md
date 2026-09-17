## 2026-09-17 - WebGL Uniform Hoisting per Render Pass
**Learning:** Re-activating WebGL programs (`gl.useProgram`) and re-setting global uniforms (`gl.uniform*`) inside per-chunk iteration loops creates severe driver overhead in JS/WebGL render loops.
**Action:** Always hoist global WebGL state setup and uniform bindings into a pass-level function (`prepareWorldProgram`) before iterating over chunk meshes.
