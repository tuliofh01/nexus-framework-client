# builds/

Central output directory. **Generated apps and client deploy binaries are
git-ignored.** Tracked: `README.txt`, this file, `.gitkeep` placeholders,
and optional `framework/_fixture/`.

Plain-text overview: [README.txt](README.txt).

## Layout

```
builds/
├── README.txt / LAYOUT.md
├── client/      Compose Desktop client deploy artifacts
│   ├── README.txt
│   ├── .gitkeep
│   ├── app/         (ignored) runnable distribution
│   └── packages/    (ignored) OS installers
└── framework/   Native apps scaffolded from templates
    ├── README.txt
    ├── .gitkeep
    ├── _fixture/    (tracked) light generator/test helpers
    └── <Project>/   (ignored) e.g. MyApp, Plotter2DApp
```

## Client (`builds/client/`)

| Task | Output |
|------|--------|
| `./gradlew :app:deployToBuildsClient` | Runnable distribution → `builds/client/app/` |
| `./gradlew :app:deployPackageToBuildsClient` | OS packages (`.deb`, `.rpm`, …) → `builds/client/packages/` |

Gradle still writes intermediates under `app/build/`; deploy tasks copy finished artifacts here.

## Framework (`builds/framework/`)

Generated projects land under `builds/framework/<projectName>/` (see
`nxs_config.json` → `build.outputDir`). Build with the project’s Zig or
CMake instructions; do not commit those trees.

Related: [../README.md](../README.md) · [../template/README.md](../template/README.md)
