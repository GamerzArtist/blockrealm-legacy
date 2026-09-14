## 2025-05-18 - Safe Object Assignment for Save Data
**Vulnerability:** Potential prototype pollution when parsing user-controlled JSON save files into application objects (e.g. `player`, `equipment`).
**Learning:** Standard `Object.assign` copies all enumerable own properties, including keys like `__proto__` or `constructor` if present in untrusted JSON.
**Prevention:** Use a custom `safeAssign(target, source)` helper that ignores `__proto__`, `constructor`, and `prototype` keys during state restoration.
