# BlockRealm: Legacy 4.0.0

Original mobile-first voxel survival game and Android launcher source.

## Highlights
- Offline WebGL 1 renderer for broad Android WebView compatibility
- Deterministic streamed procedural terrain
- Nine-slot hotbar, 36-slot inventory, crafting tables and furnaces
- Survival, creative, mobs, drops, tools, hunger and day/night
- Shader master toggle with Reimagined and Cinematic presets
- Terrain-aware sun, moon and godray occlusion
- Android package: `com.gamerzartist.blockr4`
- Version: `4.0.0` (`40000`)

## Android Studio
Open this directory in Android Studio, allow Gradle sync, and build the `app` module. The distributed emergency-compatible APK uses the same assets through a tiny NativeActivity launcher; this maintainable project uses a normal Java `MainActivity` with `WebViewAssetLoader`.

## Controls
Mobile: left stick moves, right-side drag looks, hold pick to mine/attack, hand to use/place, plus jump and sprint. Desktop: WASD, mouse, Space, Shift, E, number keys, left/right click.

## Licence
MIT. BlockRealm is not affiliated with Mojang or Microsoft. Do not add Minecraft textures, audio, branding or proprietary assets to this repository.
