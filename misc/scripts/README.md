# misc/scripts/

Repo automation organized by function. Each subdirectory is a script family with its own README where needed.

## Layout

| Directory | Purpose |
|-----------|---------|
| [dev/](dev/) | Local development workflow (`nexus-dev.sh`, Docker generation) |
| [test-gen/](test-gen/) | Generate smoke tests for built apps under `builds/framework/` |
| [generate-diagrams/](generate-diagrams/) | Docs SVG diagram generator |

**Client first-run setup** stays at [../client-setup/](../client-setup/) — not duplicated here.

## Quick reference

```bash
# Dev workflow
./misc/scripts/dev/nexus-dev.sh compile
./misc/scripts/dev/generate-in-docker.sh desktop MyApp builds/framework/MyApp

# Test generation (dry-run against fixture)
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture

# Diagrams
python3 misc/scripts/generate-diagrams/generate-styled-diagrams.py
```

## Backward compatibility

Thin shims remain at the old flat paths:

- `misc/scripts/generate-in-docker.sh` → `dev/generate-in-docker.sh`
- `misc/scripts/generate-styled-diagrams.py` → `generate-diagrams/generate-styled-diagrams.py`

See [../README.md](../README.md) and [../../AGENTS.md](../../AGENTS.md) for full repo context.
