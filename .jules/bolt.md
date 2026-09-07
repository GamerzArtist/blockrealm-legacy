## 2026-09-07 - Zero-Allocation Light Source Resolution in Frame Render
**Learning:** In 60 FPS WebGL frame rendering loops, methods like `torchSource()` that return temporary vector arrays `[x, y, z, w]` and parse string key coordinates using `k.split(',').map(Number)` generate high per-frame GC allocation pressure.
**Action:** Reuse a pre-allocated static 4-element array buffer (`torchTmp`) and parse string keys directly with `indexOf`/`substring` + unary `+` numeric conversion.
