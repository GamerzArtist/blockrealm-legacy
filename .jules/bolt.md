## 2026-07-28 - Bounded Map Caching for Procedural Terrain Info
**Learning:** Procedural FBM noise and terrain calculations are executed heavily during chunk meshing, player spawning, and surface queries. Caching terrain queries in a bounded `Map` significantly eliminates redundant math operations.
**Action:** Use a bounded `terrainCache` Map capped at 10,000 entries that clears completely via `.clear()` when full to prevent memory leaks while avoiding JS engine iterator overhead from manual FIFO deletions. Clear the cache during world loads and seed changes.
