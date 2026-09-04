## 2026-09-04 - Avoid Array Allocations in Per-Frame Torch Source Calculations
**Learning:** `torchSource()` runs every rendered frame. Instantiating return arrays and using `k.split(',').map(Number)` inside iteration loops creates significant garbage collection pressure on WebGL rendering passes.
**Action:** Reuse a static pre-allocated array buffer (`torchTmp`) and parse coordinate strings directly using `indexOf` and `substring` with unary `+` numeric conversion in hot per-frame rendering functions.
