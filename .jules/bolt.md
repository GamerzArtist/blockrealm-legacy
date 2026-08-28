## 2026-08-28 - Bounded Terrain Computation Caching
**Learning:** Procedural terrain noise calculation (`terrainInfo`) is called multiple times per column during chunk generation, chunk decoration, surface lookup, and mob/player spawning. Caching `terrainInfo` in a bounded `Map` eliminates redundant FBM noise evaluation.
**Action:** Use a bounded `terrainCache` Map (capped at 10,000 items) and clear it completely via `.clear()` on world resets and seed changes to avoid JS engine iterator overhead.
