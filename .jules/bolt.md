## 2026-10-02 - Reuse static array buffers for per-frame render calculations
**Learning:** Functions called every frame inside `renderFrame()` (like `torchSource()`) generate unnecessary Garbage Collection pressure if they allocate array literals (`[x, y, z, w]`) or split coordinate string keys (`k.split(',').map(Number)`).
**Action:** Reuse a pre-allocated module-level `Float32Array` or static array buffer, and parse string coordinate keys with `indexOf` and `substring` to keep frame rendering allocation-free.
