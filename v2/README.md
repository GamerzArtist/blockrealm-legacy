# VoxelForge / Wildlight 0.3

This release-candidate source bundle has **two separate targets**. The web target is the full inherited survival game with the Wildlight rendering and interface changes. The native Android target is a genuine Java/OpenGL ES 3 Creative sandbox preview, not a WebView, and not a feature-complete port of the web game. An APK was not built or signed in the authoring environment because the Android SDK, Gradle and Android build/signing tools were unavailable. Do not treat this bundle as a certified production release.

## Play the full game now

Open `web/index.html` in a WebGL2-compatible browser. It is self-contained: no libraries, network, installation, or remote art. `PLAY_WEB.bat` opens it on Windows. The game retains the previous world format and seed algorithm. Export old worlds first if the browser stores the new HTML under a different origin.

### New controls and quality-of-life

- Mobile: left joystick, right-screen drag to look, Jump, Mine, Place, Bag, Sneak, Run. Sneak and Run are mutually exclusive toggles. Creative flight: double-tap Jump.
- Desktop: the previous rebindable controls, including F5 camera, F3 debug, F2 screenshot, E inventory and F6 animation inspector, remain.
- Pause menu: save, export a `.vfworld.json` backup, capture the scene, or save and quit. The world list also has Export and Import backup. Name conflicts create a duplicate instead of replacing a saved world. Imported files have a size cap and schema checks.
- Graphics: Medium is a sensible phone starting point. Adaptive resolution adjusts internal pixel count without changing the render-distance setting. F3 displays its current scale. High/Ultra retain expensive lighting and reflections; Low disables the extra effects. No device-specific FPS guarantee.
- Browser graphics context loss freezes the render loop and shows an explicit reload/recovery message. Save storage may still be unavailable in restrictive browsers, so export important worlds.

## Build the native Android APK on Windows

The native source is in `android/`. It contains **no WebView, no browser runtime, no web HTML assets, and no runtime INTERNET permission**. It uses GLSurfaceView and OpenGL ES 3, generated Java geometry, native views and touch dispatch, and locally synthesized PCM audio.

1. Install Android Studio. In SDK Manager install Android SDK Platform 35, Android SDK Build Tools 35.0.0, Platform Tools, and Android SDK Command-line Tools (latest). Accept the Android SDK licenses in SDK Manager. Use Android Studio's JDK 17/21 or set `JAVA_HOME` to a compatible JDK. If Studio is installed in a custom directory, set `JAVA_HOME` to its `jbr` folder (when compatible). Set `ANDROID_HOME` if the SDK lives elsewhere.
2. Extract this ZIP to a writable folder. Keep the files together.
3. Open Command Prompt there and run `BUILD_ANDROID.bat`. The script uses the Android Studio SDK/JDK, pinned Gradle 8.7 and Android Gradle Plugin 8.6.1. If Gradle 8.7 is not already available it downloads the official distribution and validates its published SHA-256 checksum. The first build needs internet to resolve Gradle and Android plugin dependencies. The installed game itself is offline.
4. On first build, a local persistent signing key is created with `keytool`. The release key and password properties stay under `android/`, outside `dist/`. Keep these private, back them up securely, and never send them to anyone. Use the same key for later update installs. The generated APK is locally release-signed, not Play Store-certified.
5. The script runs release JVM tests, Android lint, release assembly, and `apksigner verify --verbose --print-certs`. The signed output is copied to `dist/VoxelForge-native-preview.apk` only after signature verification. `dist/SHA256.txt` contains the APK checksum.
6. To require a real-device/emulator launch test, enable USB debugging on a device you control and authorize the PC. Run `BUILD_ANDROID.bat -VerifyDevice`. If exactly one authorized device is connected the default build also runs this test. For multiple devices, use `BUILD_ANDROID.bat -VerifyDevice -Serial DEVICE_ID`. The script installs the signed APK, launches QA mode using isolated test saves, waits for a native GL/mesh success marker, captures two rendered screenshots, checks scene color variation, swipes the camera, scans for runtime failures and writes logcat/gfxinfo. If no device is connected, device QA is explicitly unverified. The QA mode is not intended as a benchmark or production certification.
7. Copy the signed APK to your phone and install it using Android's normal sideloading controls. Do not disable device-wide security protections. For USB installation on an authorized development device, `adb install -r dist\VoxelForge-native-preview.apk` is equivalent. If an older APK with the same package was signed with another key, Android will reject the update; back up its saves before choosing to uninstall it.

Optional: `BUILD_ANDROID.bat -NoDownload` uses cached dependencies only. `BUILD_ANDROID.bat -WebQA -VerifyDevice` additionally runs the web regression suite (requires Playwright setup below).

You can also open `android/` in Android Studio, configure the local Gradle 8.7 distribution if requested, and sync. The batch build is the reproducible route and invokes the same Android build system as Studio rather than clicking IDE buttons.

## Web QA on your own PC

Install Python 3, then `python -m pip install playwright` and `python -m playwright install chromium`. Run `RUN_WEB_QA.bat` (or `RUN_WEB_QA.bat --headed --software` for a visible software-WebGL run). The test creates a loopback origin, creates worlds, captures actual screenshots, exercises the backup import, reloads to verify IndexedDB, checks all four quality presets, and checks mobile toggles. This is separate from the native renderer test. The authoring sandbox blocked loopback/file navigation, so its executed browser smoke test used content injection and temporary storage instead; durable IndexedDB is not claimed verified here.

## Files

- `web/index.html`: complete offline survival game source.
- `android/`: Gradle project with Java source, adaptive/legacy icons, Android 12+ splash resources, native UI, manifest, and unit tests.
- `BUILD_ANDROID.bat`, `tools/Build-Android.ps1`, `tools/Verify-Device.ps1`: build/sign/verification automation.
- `RUN_WEB_QA.bat`, `tools/web_qa.py`: browser verification automation.
- `qa/`: actual browser captures, recorded smoke-test output, Java syntax/terrain test outputs and the original icon preview.
- `REVIEW.md`: changes, critiques, validation status and release blockers.

## Native preview limitations

The native game is Creative-only with a single local world, native touch navigation, block selection, mining/placement, flight, generated trees and grass, animated foliage/water, procedural textures/sky, local edit saves and four synthesized effects. It lacks the web game's survival inventory, mobs, crafting, dimensions, multiple worlds, and advanced shadows/post-processing. Its seed terrain differs from the web terrain; web backups cannot be imported into the native preview. The native rendering pipeline, Android resources, Gradle build, and Windows script are supplied as source, but Android integration, signing, and real-device stability must be validated with the batch workflow. The Java terrain generator was compiled and tested on the authoring machine; the Android-dependent classes were syntax-parsed only. No claim of complete port, 60 FPS, or crash-free shipping readiness is made.