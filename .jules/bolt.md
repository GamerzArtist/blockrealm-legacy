## 2025-10-05 - Hoisting Entity WebGL Program State
**Learning:** Setting WebGL active program (`useProgram`), binding attribute pointers, and re-uploading frame-static uniforms (`uVP`, `uCamera`, `uFogColor`, `uFogNear`, `uFogFar`) inside primitive draw helpers (`drawBox`) causes hundreds of redundant GL API driver calls per frame when rendering entities (mobs and item drops).
**Action:** Hoist program activation, attribute pointer binding, and scene uniforms into a frame-level preparation function (`prepareEntityProgram`) before iterating over entity primitive calls.
