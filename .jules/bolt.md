## 2026-09-22 - Scalar Primitives in Voxel Chunk Meshing Vertex AO
**Learning:** Per-vertex functions during meshing (such as ambient occlusion computation) execute tens of thousands of times per chunk generation/streaming event. Creating temporary direction vectors (`[x,y,z]`) and inline lookup arrays inside per-vertex methods causes heavy GC pressure.
**Action:** Use scalar primitive variables (`s1x`, `s1y`, `s1z`) and file-level constants/lookup tables for hot meshing utility functions.
