## 2026-03-31 - Bounded Map clearing in performance hot-paths
**Learning:** In JS performance hot-paths like procedural terrain calculation, using `Map.prototype.keys().next().value` for FIFO eviction allocates iterator objects and degrades JS engine inline optimization. Clearing the map via `.clear()` when bounds are exceeded is significantly faster and allocation-free.
**Action:** Use `.clear()` for bounded Map caches in hot-path loops, and ensure cache resets occur across world seed changes.
