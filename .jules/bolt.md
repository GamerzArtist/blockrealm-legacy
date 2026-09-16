## 2026-09-16 - Per-Frame Torch Source Allocations
**Learning:** Functions called inside `renderFrame()` (such as `torchSource()`) must be completely zero-allocation to prevent GC pressure and frame stutters. Instantiating new return arrays or using `k.split(',').map(Number)` inside rendering loops creates constant heap allocations every frame.
**Action:** Always reuse static buffer arrays (e.g. `torchTmp`) and use direct `indexOf`/`substring` string key parsing in functions invoked per-frame during rendering.
