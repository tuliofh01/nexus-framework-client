# builds/ (project-local pointer)

CMake presets in `../CMakePresets.json` write out-of-source trees to the **Framework
repo** central folder:

```
../../builds/framework/{{projectName}}/<preset>/
```

For example, with project name `MyTradingApp`:

```bash
cmake --preset debug
cmake --build --preset debug
./../../builds/framework/MyTradingApp/debug/{{projectName}}
```

Or without presets:

```bash
cmake -B ../../builds/framework/{{projectName}}/debug -G Ninja -DCMAKE_BUILD_TYPE=Debug
cmake --build ../../builds/framework/{{projectName}}/debug
```

This per-project `builds/` folder is not used for CMake output — see
`nxs_config.json` → `build.outputDir` for the resolved path.
