# Architecture

## Overview

BlockRealm uses a shared web-game core that can run in three environments:

1. A normal web browser
2. An installable Progressive Web App
3. An Android WebView launcher

## Main components

### Rendering

The renderer uses WebGL with a mobile-compatible path. Terrain is chunked so distant geometry can be created, displayed, and unloaded independently.

### World generation

Worlds are deterministic. A seed and global coordinates produce terrain, biome, cave, ore, and vegetation data. The same seed should reproduce the same base terrain.

### Persistence

Player data, inventory state, settings, and modified blocks are stored separately from generated terrain. This avoids saving the entire world as raw voxel data.

### Gameplay systems

Inventory, crafting, survival statistics, creatures, furnaces, block interaction, and player movement are implemented in the shared JavaScript game layer.

### PWA

The service worker caches local assets for offline use. PWA installation requires HTTPS or localhost because browsers restrict service workers on normal `file://` pages.

### Android

The maintainable Android project loads bundled web assets inside a WebView. Android package builds should be produced from the Gradle project rather than by manually assembling APK archives.

## Compatibility strategy

- WebGL-compatible baseline
- Adjustable graphics quality
- Shader-disabled fallback
- Landscape-first mobile layout
- No required remote CDN assets
