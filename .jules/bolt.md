## 2026-09-26 - Per-frame torchSource array allocations
**Learning:** In hot per-frame rendering paths like `torchSource()`, returning new array literals and parsing string coordinate keys (`'x,y,z'`) using `k.split(',').map(Number)` generates garbage collector pressure on every frame.
**Action:** Reuse a module-level pre-allocated array buffer (`torchTmp`) and parse string coordinates directly with `indexOf` and `substring` to achieve zero-allocation per frame during rendering.
