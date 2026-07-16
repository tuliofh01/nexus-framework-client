# zig-services — Android native build

Replaces CMake + Djinni as the **primary** native build system for generated Android apps. Produces `lib{{projectName}}.so` via Zig cross-compilation. CMake and Djinni are fully removed — Zig is the only native build path.

## Requirements

- **Zig 0.14.x** (pinned in `misc/client-setup/env.sh`)
- **Android NDK** r26+ (API ≥ 29) — install via SDK Manager or `sdkmanager "ndk;26.3.11579264"`

## Quick build

```bash
# Build for arm64 (device)
cd template/android-app/zig-services
zig build -Dtarget=aarch64-linux-android

# Build for x86_64 (emulator)
zig build -Dtarget=x86_64-linux-android
```

Output: `zig-out/lib/{{projectName}}.so` (or `arm64-v8a`/`x86_64` subdirectory).

## Integration with Gradle

The `app/build.gradle.kts` `zigBuildRelease` task runs `zig build` and copies the `.so` into `jniLibs/` before APK assembly. See [`app/build.gradle.kts`](../app/build.gradle.kts) for the Exec task definition.

## What it builds

All C++ sources under `../src/` and `../shared/runtime/` are compiled with `zig c++`. External dependencies are fetched via `build.zig.zon`:

| Library | Source |
|---------|--------|
| Dear ImGui + ImPlot + ImNodes | `build.zig.zon` (git tarball) |
| sol2 | `build.zig.zon` (header-only) |
| Lua 5.4 | `build.zig.zon` (static) |
| SDL3 | System library (via NDK sysroot) |
| Chaquopy | JVM runtime — not compiled here |

## JNI architecture

| Layer | What | Language |
|-------|------|----------|
| Kotlin `AppCore.installPythonBridge` | JNI call → Zig `export fn` | Kotlin → Zig |
| Zig `Java_com_nexus_{{packageName}}_AppCore_installPythonBridge` | JNI entry point, delegates to C++ | Zig |
| C++ `c_install_python_bridge` | Creates `NativePythonBridge` | C++ |
| C++ `NativePythonBridge` | JNI callbacks to Kotlin `PythonBridge` | C++ |
