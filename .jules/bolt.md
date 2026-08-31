## 2026-08-31 - Direct String Parsing for Coordinate Keys in Render Loop
**Learning:** Calling `k.split(',').map(Number)` inside hot per-frame rendering functions (such as `torchSource()`) allocates multiple short-lived arrays and functions per iteration, creating measurable GC pressure. Using direct `indexOf` and `substring` numeric parsing is over 130x faster and avoids allocations.
**Action:** Always parse string coordinate keys (`"x,y,z"`) using `indexOf` / `substring` in functions called every frame.
