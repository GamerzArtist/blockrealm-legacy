## 2025-08-19 - Procedural Terrain Noise Caching
**Learning:** Procedural terrain evaluation (`terrainInfo`) executes heavy FBM noise calculations thousands of times per chunk generation and frame update. Caching calculations in a bounded `Map` significantly speeds up chunk generation and meshing.
**Action:** Use a `terrainCache` Map capped at 10,000 entries with `.clear()` when full or when world seeds change.
