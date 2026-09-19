## 2025-05-18 - Static Buffer Reuse and Fast Key Parsing in Per-Frame Torch Source
**Learning:** In per-frame rendering functions such as `torchSource()`, creating new 4-element arrays and splitting coordinate string keys (`k.split(',').map(Number)`) creates garbage array allocations on every frame tick, triggering GC pauses during rendering.
**Action:** Reuse a module-scoped pre-allocated array buffer (`torchTmp`) and parse string coordinate keys directly using `indexOf` and unary `+` numeric conversion.
