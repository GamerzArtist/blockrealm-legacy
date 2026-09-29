## 2026-09-29 - Deduplicate Noise Calculations in terrainInfo
**Learning:** In procedural terrain generation (`terrainInfo`), calling `biomeAt(x, z)` computes `temp` and `moist` multi-octave FBM noise, but `terrainInfo` was separately calling `fbm()` for `temp` and `moist` again to construct its return object.
**Action:** Compute `temp` and `moist` once at the top of `terrainInfo` and pass them into `biomeFrom`, eliminating 6 redundant multi-octave noise calculations per terrain query call.
