# zig-services — Android JNI + C-ABI sidecar

Produces `lib{{projectName}}.so` with:

- C ABI exports (`nxs_alloc`, `nexus_zig_version`, …) from `src/`
- Chaquopy JNI bridge from `jni/python_bridge.zig`
- Lua JNI stubs from `jni/lua_bridge.zig`

**Zig does not compile C++20 named modules** (`.cppm`). Those stay in the app
`src/` / `shared/` trees; this sidecar is only what Kotlin loads via
`System.loadLibrary`.

## Build

```bash
# via the project script (venv + Zig ABIs + Gradle)
../build_app.sh
../build_app.sh --zig-only

# or directly (requires ANDROID_NDK)
zig build -Dtarget=aarch64-linux-android -Dandroid-ndk=$ANDROID_NDK
zig build -Dtarget=x86_64-linux-android  -Dandroid-ndk=$ANDROID_NDK
```

Gradle’s `zigBuildRelease` task (see `app/build.gradle.kts`) copies the `.so`
into `jniLibs/` before APK assembly.
