## 2026-09-13 - Fixed Grid Array for Chunk Neighbor Lookups
**Learning:** In voxel chunk meshing, looking up neighboring chunks via a `Map` using string keys (e.g. `ckey(nx, nz)`) inside voxel query loops introduces tens of thousands of string allocations and hash lookups per chunk mesh pass.
**Action:** Use a pre-populated flat 3x3 array (`nearGrid`) indexed by fast integer arithmetic `(gx + 1) * 3 + (gz + 1)` for 3x3 neighbor queries in chunk meshing.
