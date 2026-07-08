# builds/

Out-of-source build trees go here. The CMake presets in `../CMakePresets.json`
bind `binaryDir` to `builds/<preset>` (e.g. `builds/debug`, `builds/release`),
so this folder is populated by:

```bash
cmake --preset debug
cmake --build --preset debug
```

Everything under this folder except this note and `.gitkeep` is git-ignored.
