# Task 1 — Convert desktop AppModel → nxs.desktop.model

## Status:  Complete

### Actions
- Created `template/desktop-app/src/model/AppModel.cppm` with `export module nxs.desktop.model;`
- Deleted `template/desktop-app/src/model/AppModel.hpp`
- Deleted `template/desktop-app/src/model/AppModel.cpp`

### Verification
- [x] AppModel.cppm exists with correct module name
- [x] Originals (.hpp/.cpp) deleted
- [x] No project `#include` remains
- [x] Module exports: `AppModel` class with `counter()`, `setCounter()`, `greeting()`, `setGreeting()`
- [x] Educational comments present (no ASCII-art box diagrams)
- [x] Style follows NexusBridge.cppm exemplar
