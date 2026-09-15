## 2026-09-15 - Prototype Pollution Prevention in Game State Deserialization
**Vulnerability:** Restoring player state, equipment, and settings directly via `Object.assign` from JSON data stored in local storage or imported world files allowed key collision with object prototype properties (e.g. `__proto__`, `constructor`, `prototype`).
**Learning:** `Object.assign` copies all own enumerable properties from a source JSON object onto a target object without filtering special JavaScript prototype property keys.
**Prevention:** Use a safe assignment helper (`safeAssign`) that explicitly skips prototype property keys (`__proto__`, `constructor`, `prototype`) whenever restoring JSON state into existing objects.
