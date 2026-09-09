## 2026-09-09 - Direct Key Parsing in Hot Rendering Paths
**Learning:** In hot per-frame rendering functions such as `torchSource()`, using `k.split(',').map(Number)` to parse coordinate string keys ('x,y,z') creates temporary array and string object allocations on every frame, leading to GC pauses and reduced FPS.
**Action:** Parse coordinate keys directly using `indexOf` and `substring` with unary `+` numeric conversion (`+k.substring(...)`) to avoid heap allocations during rendering loops.
