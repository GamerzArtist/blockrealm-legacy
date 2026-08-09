## 2026-08-03 - Accessible Icon-Only Buttons
**Learning:** Icon-only buttons in retro pixel or voxel game user interfaces frequently rely on abstract Unicode symbols (like ▦, ✦, Ⅱ, ⛏, ✋) that are completely uninformative for screen-reader users, rendering critical navigation, inventory, and action elements inaccessible.
**Action:** Always verify all controls for text labels, and systematically apply descriptive `aria-label` attributes to any buttons that only use iconographic, typographic, or symbol-based content.

## 2026-08-04 - Initializing Palette Journal
**Learning:** Found that this mobile-first voxel survival game BlockRealm: Legacy runs in the browser and android wrapper. The interface has standard game elements: a 9-slot hotbar, settings menu, worlds screen, about screen, inventory overlay, etc.
**Action:** Keep UX changes extremely clean, precise, and high-impact. Avoid cluttering the UI. Keep changes under 50 lines. Keep local development files and android/app/src/main/assets in sync manually.
