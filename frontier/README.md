# Frontier — phase 00 baseline

Baseline commit: 25e4d52af4328dfc74955626f3890ce464c43069. Existing legacy web/Android and Wildlight V2 remain separate, unchanged except for scoped legacy hardening. No released APK or save formats are replaced.

## Engineering gates
- Legacy web/Android copies must match; boot, world creation, save reload, block edits, and touch input must be regression tested.
- Wildlight native Creative preview is not full web parity.
- Phase 01: shared deterministic simulation core (Rust + WASM/native), protocol and versioned saves. JS contract tests are reference fixtures, not a port.
- Phase 02: native launcher, original branding, accessibility and phone-first controls; evaluate open-source UI libraries on license, accessibility, native compatibility and footprint.
- Phase 03: local/LAN private parties before hosted discovery, authenticated server authority, verified mod manifests; no public account service until security review.
- Phase 04+: unique rigged species, coherent biomes, deeper survival, renderer, audio and mod importers.

## PR triage
276 open proposals indexed in PR-inventory.json, with original branch and commit SHA. Grouping uses titles and representative patch inspection; it is not a claim every patch is identical. Keep one representative per narrow recurring theme and all distinct proposals. Archive overlapping proposals without merging or deleting their branches; all may be reopened. Seed-XSS and basic save assignment protection are addressed on this branch; broader schema validation, CSP, gameplay and optimization proposals remain open for dedicated regression review. Do not run automated PR agents repeatedly against the frozen legacy target.

## Baseline verification
Run `node --test frontier/qa/contracts.test.mjs` from repository root once the test commit lands. Real browser and Android performance are separate release gates, not established by contract tests.
