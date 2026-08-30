## 2026-08-30 - Terrain Noise Deduplication and Static Matrix Buffer Optimization
**Learning:** `terrainInfo` in `web/game.js` was redundantly evaluating multi-octave noise functions (`temp` and `moist`) twice per column lookup, while `M4.mul` created new 16-element typed arrays on every frame calculation.
**Action:** Deduplicate `temp` and `moist` computations inside `terrainInfo(x, z)`, cache terrain calculation results in a bounded `terrainCache` Map (cleared on seed/world load), and reuse a module-scoped `m4Tmp` Float32Array in `M4.mul`.
