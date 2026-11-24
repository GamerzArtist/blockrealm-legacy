# Wildlight engineering review

## Basis

Reviewed the Lightforge HTML and actual forest, water and launcher renders. Prior graphics used expensive fragment noise and a repeated leaf/bark pattern, with dark forest interiors, large green areas, regular wave bands and an overly translucent modern HUD. The seed world, gameplay, save schema, worker architecture, fixed tick, and existing animation systems were retained in the browser target.

## Pass 1 — Material and foliage system

- Implemented 55 material IDs × 3 faces × 32×32 RGBA texels as a generated WebGL2 texture array. Pixel tiles include separate top/side/bottom color patterns, bark rings, clustered leaf cutouts, grass lips, soil grains, sand, plank seams, ore veins, brick mortar, and emissive/electrical effects. Mipmaps and optional anisotropic filtering reduce aliasing without external artwork.
- Inventory icons reuse the same authored material tiles. Held-block geometry now uses stable local texture coordinates so textures follow the animation instead of sliding through camera space.
- Made canopy movement coherent across merged vertices and increased wind-driven grass geometry density. The trunk stays anchored. Plants flex with wind and storms, and leaf shadow cutouts use the same texture array.
- Found an incorrect texture-array depth in initial browser QA; corrected it and reran startup tests.

## Pass 2 — Color, water and composition

- Raised hemispheric ambient illumination, reduced harsh sun exposure, improved bark midtones and foliage highlights, moderated overly lime-green grass, and reduced extreme vegetation wedge sizes. Sky color and clouds use a cooler horizon/warmer illumination balance.
- Retained scene depth-based water refraction, Fresnel, SSR fallback, caustics and sun highlights. Tuned absorption/clarity, irregular multi-directional waves, shoreline tint and distance haze. Residual distant repetition and water/chunk boundary discontinuities remain visible in certain views.
- Retained shadow-map ray integration, contact shading, bloom, tone mapping and the prior post-processing pipeline. No claim of full physically based multi-scattering or true PBR. Cloud model and foliage geometry remain relatively simple.

## Pass 3 — UI and performance

- Original square pixel-style buttons, metal/stone frames, moss-green accents, pixel heart and food meters, original vector forge emblem, revised menus and touch controls.
- Added two touch movement toggles, screenshots and backup export/import from menus, name-conflict handling, and bounded import validation. Added a WebGL context-loss message and worker retry flag reset.
- Cached chunk VAOs and material thumbnails, skipped opaque-scene copies when no liquid mesh is visible, replaced much per-fragment material noise with sampled mipmapped tiles, and introduced adaptive internal resolution. Hidden tabs skip rendering. These are architectural optimizations, not measured proof of higher FPS on a real phone.
- Default adaptive resolution thresholds: 23 ms downscale, 17.4 ms upscale, 2.5 second adjustment interval, 0.60–1.0 scale. Mobile/desktop pixel budgets: 1.2/2.2 million. Render radius 2–8 chunks; simulation remains 20 TPS. Previous 1536²/1024² cascades, 3×3 PCF, 10-sample volumetric approximation and 9-step reflection march retained.

## Pass 4 — Android build system and release gating

- Separate native Java/OpenGL ES 3 renderer (not a wrapper) and native Android launcher, adaptive and fallback icon, themed system splash, landscape touch UI, chunk generation worker, local save data, creative movement and block edits, animated foliage/water and generated SFX.
- Pinned SDK 35, AGP 8.6.1, Gradle 8.7, JDK 17/21. Windows build script generates a local release key, executes unit tests/lint/build, checks APK signatures and hashes, and optionally exercises an attached device using isolated QA storage. Release QA captures real screenshots via ADB and records errors.
- Native world settings: chunk width 16, height 112, sea level 46, streaming radius 2, 96 generated-chunk cache, one mesh worker, max five queued meshes, 20 TPS. Not feature-equivalent to the web target. No web engine is packaged into the APK.

## Release blockers and honesty

The native project cannot be certified or called shipping-ready before compiling against the actual Android SDK, executing Android lint, signing, and completing physical-device rendering/input/pause-resume/thermal tests. The build automation itself has not been executed on Windows. No APK has been built in this environment. The native renderer lacks much of the survival engine and advanced web graphics. Both targets retain known visual and gameplay gaps. Full regression of every inherited game system, long-running stability, durable web IndexedDB across sessions, and 60 FPS on a particular device are not established.

## Recorded final verification

Chromium with software WebGL: 14/14 startup tests passed; launcher-to-Creative world, F5 camera cycle, 36-slot inventory, direct block edit path, save call, duplicate backup import, all four graphics presets, mobile world creation and mutually exclusive touch Sneak/Run toggles exercised. The final console error list was empty and WebGL error code was zero on every tested preset and mobile. See `qa/browser-smoke.txt` and `qa/browser-results.json`. The direct block edit was exercised but not separately asserted after save/reload. These tests do not establish long-term storage or performance. Pure Java native world test passed deterministic height generation, negative coordinates, edit restoration and mesh output; all native Java sources parsed successfully. No Android SDK integration or real native rendering was executed here.