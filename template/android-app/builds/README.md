# builds/ (project-local output pointer)

Generated native binaries and APK artifacts land in the Framework repo's central folder:

```
../../builds/framework/{{projectName}}/
```

Day-to-day builds use Zig (`zig-services/`) and Gradle caches — neither is tracked.

```bash
cd zig-services
zig build -Dtarget=aarch64-linux-android
```

See `nxs_config.json` → `build.outputDir` for the resolved path.
