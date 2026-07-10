# builds/

Central output directory. Generated artifacts are git-ignored; only this README and `.gitkeep` placeholders are tracked.

## Layout

```
builds/
├── client/      Kotlin Compose Desktop client deploy artifacts
└── framework/   Native apps scaffolded from templates — one folder per project
                 e.g. builds/framework/MyApp/debug/
```

## Client (`builds/client/`)

| Task | Output |
|------|--------|
| `./gradlew :app:deployToBuildsClient` | Runnable distribution → `builds/client/app/` |
| `./gradlew :app:deployPackageToBuildsClient` | OS packages (`.deb`, `.rpm`, …) → `builds/client/packages/` |

Gradle still writes intermediates under `app/build/`; deploy tasks copy finished artifacts here.

## Framework (`builds/framework/`)

Generated projects configure CMake presets to build into `builds/framework/<projectName>/`:

```bash
cd path/to/MyApp
cmake --preset debug
cmake --build --preset debug
./../../builds/framework/MyApp/debug/MyApp
```

See `nxs_config.json` → `build.outputDir` in generated projects for the resolved path.

Related: [../README.md](../README.md) · [../template/README.md](../template/README.md)
