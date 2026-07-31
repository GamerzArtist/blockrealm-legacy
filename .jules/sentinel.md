# Sentinel security journal

## 2026-07-31 - WebView Navigation Hijacking & Backup Data Extraction
**Vulnerability:** The Android WebView container was missing navigation restrictions (`shouldOverrideUrlLoading` URL checks) and did not explicitly disable local file/universal access from file URLs. Additionally, `android:allowBackup="true"` was enabled in the Android Manifest.
**Learning:** For Android applications packaging web-based game clients, leaving WebView options at their default settings or failing to intercept URL loading can allow malicious redirects, cross-origin scripting, or file theft. Furthermore, allowing application backup enables physical/ADB extraction of private database files and game saves.
**Prevention:** Always set `allowBackup="false"` in `AndroidManifest.xml` unless backups are specifically planned and secured, restrict WebView navigation to trusted origins (e.g. `appassets.androidplatform.net`), and disable file/universal access from file URLs.
