## 2025-05-18 - Bounded Terrain Generation Cache Optimization

**Learning:** Caching procedural fractional Brownian motion (fbm) terrain height and biome calculations in a bounded `Map` significantly reduces CPU calculations during chunk generation and spawn positioning. Bounding the cache and clearing it with `.clear()` when exceeding limits avoids the overhead of FIFO iterator deletions in performance-critical JavaScript execution paths.

**Action:** Maintain `terrainCache.clear()` whenever world seeds change or world saves are loaded, ensuring no stale or cross-world seed leakage while preserving maximum cache hit rate during continuous chunk generation.
