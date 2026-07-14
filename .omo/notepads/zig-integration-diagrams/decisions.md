# Decisions - Zig Integration v0.2.0

1. **Zig 0.14.0 exact pin** - Pinned in env.sh; upgrade policy via CI validation
2. **Pure Zig bootstrap (no shell scripts)** - `zig run misc/client-setup/setup.zig` replaces all .sh/.bat
3. **Gradle stays for :app/:core/:cli** - Generated apps use zero Java
4. **Phased CMake fallback** - legacy-cmake-debug/release presets; CMakeLists.txt archived, not deleted
5. **Langflow one-export→one-FlowDefinition (MVP)** - Multiple steps[] per flow; subgraphs in v1.1
6. **Pure build.zig.zon git URLs** for deps (no subtrees)
7. **No ASCII diagrams** - all rendered SVGs via generate-styled-diagrams.py
8. **v0.2.0 = Phases 0-3 + 6** - Phases 4 (Android JNI) and 5 (ArenaAllocator) deferred to v0.3.0
