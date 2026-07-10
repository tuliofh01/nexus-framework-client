# builds/

Central output directory for this repository. Generated artifacts are git-ignored;
only this README and the `.gitkeep` placeholders are tracked.

## Layout

```
builds/
├── client/      # Kotlin Compose Desktop client (:app) deploy artifacts
└── framework/   # Native apps scaffolded from templates, one folder per project
                 # e.g. builds/framework/MyTradingApp/debug/
```

## Client (`builds/client/`)

Populated by the Gradle `deployToBuildsClient` task (runs after `createDistributable`):

```bash
./gradlew :app:deployToBuildsClient
```

Runnable distribution lands in `builds/client/app/`. OS packages (`.deb`, `.rpm`, etc.)
from `packageDistributionForCurrentOS` are copied to `builds/client/packages/` when
you run `./gradlew :app:deployPackageToBuildsClient`.

## Framework (`builds/framework/`)

Generated desktop and Android projects configure CMake presets to build out-of-source
into `../../builds/framework/<projectName>/` relative to the project root (substituted
at scaffold time). Example for a project named `MyTradingApp`:

```bash
cd path/to/MyTradingApp
cmake --preset debug
cmake --build --preset debug
./../../builds/framework/MyTradingApp/debug/MyTradingApp
```

Or explicitly:

```bash
cmake -B ../../builds/framework/MyTradingApp/debug -G Ninja -DCMAKE_BUILD_TYPE=Debug
cmake --build ../../builds/framework/MyTradingApp/debug
```

See `nxs_config.json` → `build.outputDir` in generated projects for the resolved path.
