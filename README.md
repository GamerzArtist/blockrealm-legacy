# BlockRealm: Legacy

An open-source, mobile-first voxel survival sandbox with deterministic procedural world generation, survival progression, crafting, configurable shaders, offline PWA support, and an Android launcher.

> [!IMPORTANT]
> BlockRealm is an original voxel sandbox project. It is not affiliated with, endorsed by, or distributed by Mojang Studios or Microsoft. Minecraft is a trademark of Microsoft.

## Features

- Deterministic seed-based procedural worlds
- Streamed voxel chunks with biomes, caves, ores, water, and vegetation
- Nine-slot hotbar and 36-slot survival inventory
- Crafting tables, furnaces, smelting, tools, durability, health, hunger, and oxygen
- Mobile-first touch controls plus desktop keyboard and mouse controls
- Configurable shader modes, including compatibility, reimagined, and cinematic presets
- Offline standalone HTML and installable PWA builds
- Android Studio launcher project

## Project status

The browser/PWA build has been confirmed to render on a Realme 13 Pro+ 5G. The earlier APK wrapper showed a black screen because the packaged launcher did not match the maintainable Android source project. The bundled APK is therefore marked **experimental** until it is rebuilt through Android Studio or CI and physically tested.

## Repository layout

- `web/` — browser and PWA game
- `android/` — Android Studio launcher project
- `docs/` — screenshots, release notes, and QA notes
- `releases/` — experimental APK artifact

## Run the web build

```bash
cd web
python -m http.server 8080
```

Open `http://localhost:8080`.

## Android build

Open `android/` in Android Studio, allow Gradle sync to complete, and build a debug APK. Never commit signing keys, keystores, passwords, or `local.properties`.

## License

Licensed under the Apache License 2.0. See `LICENSE` and `NOTICE`.

The BlockRealm name, logo, and branding are not granted for unrestricted use under the Apache License 2.0 except as necessary to identify the origin of the project.

Copyright 2026 GamerzArtist
