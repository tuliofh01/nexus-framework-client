# Task 2 — Convert desktop FunctionRegistry → nxs.desktop.func

## Status:  Complete

### Actions
- Created `template/desktop-app/src/model/FunctionRegistry.cppm` with `export module nxs.desktop.func;`
- Deleted `template/desktop-app/src/model/FunctionRegistry.hpp`
- Deleted `template/desktop-app/src/model/FunctionRegistry.cpp`

### Verification
- [x] FunctionRegistry.cppm exists with correct module name
- [x] Originals (.hpp/.cpp) deleted
- [x] No project `#include` remains
- [x] Module exports: `FunctionSpec`, `PlotSeries`, `FunctionRegistry` with full API
- [x] Educational comments present (no ASCII-art box diagrams)
- [x] Style follows NexusBridge.cppm exemplar
