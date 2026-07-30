# Bolt's Journal

## 2026-07-30 - Initializing Journal
**Learning:** This is a performance-focused journal for BlockRealm Legacy.
**Action:** Keep entries to high-value performance insights and findings.

## 2026-07-30 - ~9x Speedup of Chunk Meshing and Voxel Access via pre-fetched 3x3 neighbors
**Learning:** The chunk meshing algorithm in `meshChunk` queried the center chunk and its immediate neighbors up to 100,000 times per chunk update using `localGet(x,y,z)`. This invoked a Map lookup of string keys constructed on-the-fly (`cx+','+cz`), leading to massive garbage collection pressure and CPU overhead.
**Action:** Avoid Map lookups and string allocation entirely by pre-fetching 3x3 neighbor chunks into a flat array `chunks3x3` and translating coordinates using extremely fast bitwise shifts `ncx = x >> 4`, `ncz = z >> 4` and direct indices `dx = ncx - cx + 1`, `dz = ncz - cz + 1`. This reduces lookup times from 1.18s to 133ms (nearly 9x faster) for 10 million voxel lookups.
