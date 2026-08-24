## 2025-05-18 - Pre-allocated Matrix Multiplication Buffer
**Learning:** Re-allocating `Float32Array(16)` inside `M4.mul` during WebGL render loop execution causes continuous garbage collection overhead and minor frame stutter on low-power devices.
**Action:** Always use a static, pre-allocated temporary typed array for vector/matrix math operations called per-frame in WebGL render loops.
