## 2026-09-03 - Reuse static array buffer & fast string parsing for torch lighting

**Learning:** Calling `k.split(',').map(Number)` inside per-frame rendering routines (like `torchSource`) or per-chunk generation/meshing loops allocates multiple temporary heap arrays every frame or chunk update. Returning newly created 4-element position arrays per frame further increases garbage collection pressure.

**Action:** Reuse a static pre-allocated array buffer (`torchTmp`) for per-frame light position returns, and extract coordinate numbers from comma-delimited key strings using `indexOf` / `substring` with unary `+` conversion to eliminate heap allocations.
