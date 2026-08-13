# Bolt's Journal - Critical Learnings Only

## 2026-08-13 - Bounded Cache Optimization in JavaScript Hot-Paths
**Learning:** Procedural terrain generation, noise functions, and biome checks are extremely hot execution paths that run thousands of times during chunk streaming and physics tick updates. Caching these computations is essential. However, using manual FIFO iterator deletions (such as `Map.prototype.keys().next().value`) to keep the Map bounded creates significant performance overhead due to iterator garbage collection inside performance-critical loops.
**Action:** Always use a simple bounded `Map` cache that clears completely using `.clear()` when the size threshold is reached. This completely eliminates iterator overhead and garbage collection pauses on hot-paths, keeping JS engine performance optimal.
