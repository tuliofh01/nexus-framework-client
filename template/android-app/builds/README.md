# builds/ (project-local pointer)

Standalone NDK CMake presets write to the **Framework repo** central folder:

```
../../builds/framework/{{projectName}}/<preset>/
```

(e.g. `../../builds/framework/{{projectName}}/android-arm64`).

Day-to-day APK builds use Gradle's `.cxx/` cache instead — both are git-ignored.

```bash
cmake --preset android-arm64   # requires ANDROID_NDK in the environment
cmake --build --preset android-arm64
```

See `nxs_config.json` → `build.outputDir` for the resolved path.
