# {{MARKER}} — Desktop smoke tests for {{PROJECT_NAME}}

## Run smoke binary

```bash
chmod +x tests/run_smoke.sh
./tests/run_smoke.sh
```

## CTest integration

Paste `tests/nexus_generated/cmake_snippet.txt` into `CMakeLists.txt`, then:

```bash
cmake --preset debug
cmake --build --preset debug
ctest --preset debug -L nexus-generated
```

Optional: install `gtest` for fuller unit tests (see `misc/scripts/test-gen/README.md`).
