# builds/ (project-local output pointer)

Generated binaries land in the Framework repo's central folder:

```
../../builds/framework/{{projectName}}/
```

For example, with project name `MyTradingApp`:

```bash
zig build
./zig-out/bin/MyTradingApp
```

See `nxs_config.json` → `build.outputDir` for the resolved path.
