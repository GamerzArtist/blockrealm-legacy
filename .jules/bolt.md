## 2026-09-08 - Eliminate per-vertex array allocations in voxel chunk meshing
**Learning:** In BlockRealm's chunk meshing pipeline, `vertexAO` is executed 6 times per visible polygon face. Allocating temporary 3D vector arrays (`s1`, `s2`, `corner`, `[1, .84, .69, .54]`) inside `vertexAO` creates tens of thousands of heap objects during chunk mesh generation, triggering frequent garbage collection pauses.
**Action:** Replace temporary array buffers in hot meshing functions with direct scalar variables (`s1x`, `s1y`, `s1z`, etc.) and hoist static lookup arrays to module scope.
