## 2026-10-13 - Zero-Allocation Coordinate Key Parsing in Per-Frame & Hot Paths

**Learning:** Calling `k.split(',').map(Number)` inside hot per-frame rendering loops (such as `torchSource()` in `renderFrame()`) or chunk meshing/streaming loops generates high volumes of temporary arrays (`.split()` array, `.map()` array, destructuring array). This causes frequent garbage collection pauses during gameplay.

**Action:** Parse string coordinate keys (`"x,y,z"` or `"cx,cz"`) directly using `indexOf(',')` and `substring()` with unary `+` numeric coercion to eliminate array heap allocations.
