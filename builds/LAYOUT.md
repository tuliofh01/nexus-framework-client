# builds/

Central output directory. **Generated apps and client deploy binaries are
git-ignored.** Tracked: `README.txt`, this file, `.gitkeep` placeholders,
and optional `framework/_fixture/`.

Plain-text overview: [README.txt](README.txt).

## Layout

```
builds/
├── README.txt / LAYOUT.md
├── clients/     Compose Desktop client deploy artifacts (preferred)
│   ├── README.txt
│   ├── .gitkeep
│   └── NexusFrameworkClient-<version>/   (ignored) runnable distribution
│       └── packages/                     (ignored) OS installers
├── client/      Legacy redirect stub → see clients/
│   └── README.txt
└── framework/   Native apps scaffolded from templates
    ├── README.txt
    ├── .gitkeep
    ├── _fixture/    (tracked) light generator/test helpers
    └── <Project>/   (ignored) e.g. MyApp, Plotter2DApp
```

## Client (`builds/clients/`)

| Task / script | Output |
|---------------|--------|
| `./build_client.sh` | License gate + `deployToBuildsClient` → `builds/clients/NexusFrameworkClient-<ver>/` |
| `./gradlew deployToBuildsClient` | Runnable distribution → `builds/clients/NexusFrameworkClient-<ver>/` |
| `./gradlew deployPackageToBuildsClient` | OS packages → `builds/clients/NexusFrameworkClient-<ver>/packages/` |

Version comes from `nexusFramework` in `gradle/libs.versions.toml` (currently **1.1.0**),
so the folder is e.g. `NexusFrameworkClient-1.1.0/`.

Gradle still writes intermediates under `build/`; deploy tasks copy finished artifacts here.

## Framework (`builds/framework/`)

Generated projects land under `builds/framework/<projectName>/` (see
`nxs_config.json` → `build.outputDir`). Build with the project’s Zig or
CMake instructions; do not commit those trees.

Related: [../README.md](../README.md) · [../template/README.md](../template/README.md) · [../build_client.sh](../build_client.sh)
