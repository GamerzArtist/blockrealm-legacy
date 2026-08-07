## 2026-08-07 - Restricting WebView Navigation and Resource Access
**Vulnerability:** Overly permissive WebView configurations in hybrid apps can allow arbitrary frame navigations to untrusted domains or expose local storage and code context to external scripts.
**Learning:** Hardening hybrid apps requires matching layers of defense on both the frontend (Content Security Policy) and the native shell (overriding shouldOverrideUrlLoading to strictly check URL prefixes).
**Prevention:** Restrict frame and general navigation to app-specific schemes or domains (like https://appassets.androidplatform.net) on the native layer, and enforce strict CSP rules in the HTML files.
