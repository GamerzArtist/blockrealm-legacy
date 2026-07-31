# Palette's Journal — Critical UX & Accessibility Learnings Only

This journal documents critical, high-impact UX/accessibility insights and lessons learned specific to this application's components and system constraints.

---

## 2026-07-31 - Keyboard Accessibility & Semantic Labels in Canvas Games
**Learning:** Canvas-based and mobile-first web games frequently overlook standard web accessibility, omitting interactive focus indicators and aria labels. Because touch navigation hides focus indicators, we can use CSS `:focus-visible` to introduce highly visible, retro-themed focus styles (like pixelated gold outlines) exclusively for keyboard users, preserving the design aesthetic for touch/mouse players.
**Action:** Always inspect custom game UI buttons for missing labels, and apply high-contrast, theme-appropriate `:focus-visible` styles with outline-offset to ensure the interface is perfectly navigable via keyboard.
