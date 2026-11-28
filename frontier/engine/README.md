# Frontier core — first executable slice

A dependency-free Rust library with integer-noise terrain, 16×16×384 chunk storage, sparse world edits, a canonical binary world-edit snapshot and minimal validated block transactions. The library builds as an rlib and cdylib. Two small C ABI functions expose height and block queries for the next client-adapter step.

This is a new core, not a migration of the existing Wildlight generator. It is not yet connected to the web or Android game. It is not a complete multiplayer server. Missing: collision, ray occlusion, timed mining/tool gates, player persistence, full registry, mobs, renderer integration and network transport. Snapshot format BRW1 is experimental; preserve existing legacy saves separately.

Build: `cargo test --manifest-path frontier/engine/Cargo.toml`.
