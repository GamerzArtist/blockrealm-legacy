## 2026-10-01 - Fast Coordinate Key Parsing
**Learning:** Using `k.split(',').map(Number)` to parse string coordinate keys ('cx,cz' and 'x,y,z') allocates multiple intermediate arrays on every invocation, causing garbage collection pressure in hot rendering loops and chunk generation.
**Action:** Parse coordinate string keys directly using `indexOf` and `substring` with unary `+` numeric conversion.
