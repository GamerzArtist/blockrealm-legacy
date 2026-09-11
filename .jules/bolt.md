## 2026-09-11 - Eliminate per-frame allocations in hot rendering paths
**Learning:** In hot per-frame rendering functions such as `torchSource()`, creating new temporary array instances (e.g. `[0,0,0,0]`) or parsing string keys via `k.split(',').map(Number)` causes garbage collection pressure every frame during WebGL rendering.
**Action:** Use pre-allocated static array buffers (`torchTmp`) and parse string coordinate keys directly using `indexOf` and `substring` with unary `+` numeric conversion.
