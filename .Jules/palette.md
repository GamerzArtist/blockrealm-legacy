## 2026-08-03 - Accessible Icon-Only Buttons
**Learning:** Icon-only buttons in retro pixel or voxel game user interfaces frequently rely on abstract Unicode symbols (like ▦, ✦, Ⅱ, ⛏, ✋) that are completely uninformative for screen-reader users, rendering critical navigation, inventory, and action elements inaccessible.
**Action:** Always verify all controls for text labels, and systematically apply descriptive `aria-label` attributes to any buttons that only use iconographic, typographic, or symbol-based content.
