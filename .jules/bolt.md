## 2026-08-22 - Bounded Terrain Info Cache Optimization
**Learning:** Procedural terrain generation (`terrainInfo`) performs multiple `fbm` noise calculations per block column and redundant evaluations across overlapping chunk decoration bounds. Caching `terrainInfo` in a bounded `Map` (`terrainCache`) significantly accelerates procedural generation and biome sampling.
**Action:** Clear the bounded `terrainCache` whenever world seed changes, world starts, or menu quit occurs, and cap map size at 10,000 entries using `.clear()` to prevent V8 iterator overhead.
