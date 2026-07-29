# {{MARKER}} — Desktop smoke tests for {{PROJECT_NAME}}

## Run smoke binary

```bash
chmod +x tests/run_smoke.sh
./tests/run_smoke.sh
```

## Zig build integration

The generated project uses `zig build` instead of CMake:

```bash
zig build test              # run tests
zig build                   # build the binary
./zig-out/bin/{{MARKER}}    # run the app
```

Optional: install `gtest` for fuller unit tests (see `misc/scripts/test-gen/README.md`).
