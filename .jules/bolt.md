## 2026-08-19 - Bounded Terrain Caching Optimization
**Learning:** Procedural terrain noise computations inside `terrainInfo` were re-evaluating `fbm` multiple times per column and during chunk generation/decorations. Bounded caching via `terrainCache` Map (cleared completely at capacity or world transitions) avoids redundant noise calculations and FIFO iterator JS engine overhead.
**Action:** Always check and cache procedural column calculations in hot meshing loops using a Map capped at 10,000 entries with `.clear()` resets.
