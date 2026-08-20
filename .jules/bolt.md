## 2026-08-20 - Procedural Terrain Computation Caching
**Learning:** `terrainInfo(x, z)` computes multiple octaves of fBm noise (~28 valueNoise evaluations per column) and is called repeatedly during chunk generation, decoration, and height calculations.
**Action:** Cache `terrainInfo` results in a bounded `Map` (`terrainCache`), capping size at 10,000 entries and clearing via `.clear()` to prevent cross-world leakage and avoid JS iterator overhead.
