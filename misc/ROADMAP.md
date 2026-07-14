# v0.4.0 — Road to MVP

When every row is , Nexus Framework is **MVP-ready**: generate native apps, edit blueprints/flows, write out projects, and ship a documented desktop/Android build.

## Client & project generator

| Item | Status |
|------|--------|
| Generate desktop + Android from templates |  |
| Blueprint editor (Compose) |  |
| Flows editor UI (list, enable/disable, JSON preview) |  |
| ProjectGenerator + validators |  |
| Compose 6-step wizard *(v1 ships 2-screen Generate + editors)* |  |

## Templates

| Item | Status |
|------|--------|
| General-purpose desktop + Android templates |  |
| `blueprint.json` + optional `flows.json` structure |  |
| TS/XHTML DSL stubs, Lua, Python paths |  |
| End-to-end desktop app build verified in CI |  |
| End-to-end Android APK build verified in CI |  |

## Runtime / generated apps

| Item | Status |
|------|--------|
| `python.dat` / `lua.dat` pack parity |  |
| Desktop pybind11 fully wired in generated app (Phase 2) |  |
| Android Chaquopy bridge E2E tested on device |  |
| TS/XHTML → Lua lowering compiler *(manual `panels.lua` documented)* |  |

## Docs & developer experience

| Item | Status |
|------|--------|
| README architecture + comparison sections |  |
| Template `AGENTS.md` guides |  |
| Multi-language [coding styles](../docs/guides/coding-styles.md) |  |
| `client-setup` scripts (JDK 26 + Zig bootstrap) |  |
| CLI `debug validate --all` or equivalent in CI |  |

## Release

| Item | Status |
|------|--------|
| CI build green on `main` |  |
| Published client binary (`builds/client/`) |  |
| Version tag `v1.0.0` |  |

---

## Post-MVP roadmap (v1.1+)

| Item | Notes |
|------|-------|
| imnodes native blueprint panel | Same `blueprint.json` schema |
| Visual flows canvas editor | — |
| Langflow JSON importer | Manual translation in v1 |
| Remote template catalog · iOS template | — |
| HTTP/webhook step types in `flows.json` | — |
