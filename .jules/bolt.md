## 2025-02-23 - Bounded Terrain Cache Optimization
**Learning:** Terrain noise generation (`terrainInfo`) is called repeatedly per chunk column and surface check during world rendering and mob spawning. Caching the terrain info in a bounded `Map` significantly reduces redundant FBM noise calculations.
**Action:** Use an integer bit-packed coordinate key for high-speed lookup in `terrainCache`, clearing completely via `.clear()` when cache bounds (10,000 entries) or world seed transitions occur to avoid JS iterator overhead and cross-world computation leaks.
