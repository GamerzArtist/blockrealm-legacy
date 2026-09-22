## 2026-09-22 - Fast String Coordinate Key Parsing
**Learning:** Using `k.split(',').map(Number)` to parse coordinate string keys ('x,y,z' and 'cx,cz') in hot loops (chunk meshing, chunk streaming, modifier checks, torch sources) creates high garbage collector pressure by allocating multiple arrays and intermediate substrings on every call.
**Action:** Use direct string indexing via `indexOf` and `substring` with unary `+` numeric conversion for coordinate string key parsing in hot paths to avoid array allocations.
