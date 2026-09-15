## 2026-09-15 - Per-Frame HUD DOM Updates & Noise Computation
**Learning:** Functions called in per-frame requestAnimationFrame loops (like `updateDay`) can secretly trigger heavy multi-octave FBM noise calculations (`biomeAt`) and redundant DOM selector/mutation overhead every frame even when values (time/biome) don't change.
**Action:** Always cache DOM elements in `dom` and diff input state (e.g. player block coordinates `px, pz`) to skip both heavy noise computations and DOM writes when unchanged.
