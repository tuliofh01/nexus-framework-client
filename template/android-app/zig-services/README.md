# zig-services — Android JNI + C-ABI sidecar

Builds `lib{{projectName}}.so` with Zig:

- C ABI exports (`nxs_alloc`, `nexus_zig_version`, …)
- Chaquopy JNI (`jni/python_bridge.zig`)
- Lua JNI stubs (`jni/lua_bridge.zig`)

Zig does **not** compile C++20 named modules. Use `../build_app.sh` for venv +
cross-compile + Gradle APK.

```bash
zig build -Dtarget=aarch64-linux-android -Dandroid-ndk=$ANDROID_NDK
```

See [BUILD.md](BUILD.md).
