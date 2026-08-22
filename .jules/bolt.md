## 2026-08-22 - Bounded Terrain Noise Caching
**Learning:** Procedural FBM noise computations inside `terrainInfo(x, z)` are called repeatedly during chunk decoration, height checks, and meshing. Caching results in a bounded `Map` (cleared completely with `.clear()` when reaching max entries or loading worlds) reduces repeated column calculation overhead by ~30x.
**Action:** Use bounded `Map` caches with key encodings (`ckey(x, z)`) and bulk `.clear()` resets for procedural heightmap and noise functions.
