## 2025-02-23 - Bounded Map Caching for Procedural Terrain Noise
**Learning:** In hot hot-paths such as procedural world generation and chunk meshing, repeated calls to multi-octave FBM noise functions for the same block column coordinates consume significant CPU cycles. Implementing a bounded `terrainCache` Map (capped at 10,000 entries and cleared completely via `.clear()`) eliminates duplicate computations without memory leaks or iterator overhead.
**Action:** Always cache multi-octave FBM terrain calculations and ensure the cache is cleared when changing world seeds or loading new worlds.
