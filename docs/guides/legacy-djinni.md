# Legacy Djinni — archived documentation

This page documents the **deprecated** Djinni codegen path that was replaced by Zig JNI exports in Phase 4.

## What Djinni was

[Djinni](https://github.com/dropbox/djinni) is an IDL-to-JNI code generator by Dropbox. It took a `.djinni` interface definition and produced C++ headers, JNI C++ glue, and Java/Kotlin stubs. Nexus used it for the Android `PythonBridge` and `AppCore` JNI bridges.

## What replaced it

**Zig `export fn` JNI entries** in `template/android-app/zig-services/jni/`:

| Djinni file | Zig replacement |
|-------------|-----------------|
| `djinni/plotter.djinni` | `jni/python_bridge.zig` — `export fn Java_com_nexus_*` |
| `djinni/app.djinni` | `jni/lua_bridge.zig` (Phase 4b) |

## How to regenerate (Djinni — deprecated)

Only needed if maintaining the legacy CMake+NDK build path:

```bash
cd template/android-app
djinni --idl djinni/plotter.djinni \
       --cpp-out djinni-generated/cpp   --cpp-namespace nxs::bridge \
       --jni-out djinni-generated/jni \
       --java-out /tmp/djinni-java-staging/com/nexus/plotter \
       --java-package com.nexus.plotter
```

Then copy Kotlin stubs from `/tmp/djinni-java-staging/` to `djinni-generated/kotlin/`.

## Migration status

| Component | Status | Target Phase |
|-----------|--------|-------------|
| `plotter.djinni` → `python_bridge.zig` |   Phase 4 | `install_python_bridge` JNI export |
| `app.djinni` → `lua_bridge.zig` |   Phase 4b | Lua runtime JNI hooks |
| CMake NDK preset |   Fallback only | `legacy-cmake-android-*` |

## Files preserved

The following files remain in the template for backward compatibility and will be removed in Phase 4b:

- `template/android-app/djinni/plotter.djinni`
- `template/android-app/djinni/app.djinni`
- `template/android-app/djinni-generated/` (all 11 files)
- `template/android-app/CMakeLists.txt` (legacy NDK build)
