# misc/scripts/

Repo automation — all scripts live flat in this directory or under `test-gen/` for platform-specific test runners.

## Layout

| Path | Purpose |
|------|---------|
| [nexus-dev.sh](nexus-dev.sh) | Local dev workflow — compile, test, generate, run client, Docker |
| [generate-in-docker.sh](generate-in-docker.sh) | Run generation inside a Docker container |
| [generate-diagrams.py](generate-diagrams.py) | Regenerate docs SVG architecture diagrams |
| [test-gen/](test-gen/) | Smoke test generation for built apps under `builds/framework/` |

**Client first-run setup** stays at [../client-setup/](../client-setup/).

## Quick reference

```bash
# Dev workflow
./misc/scripts/nexus-dev.sh compile
./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp

# Test generation (dry-run against fixture)
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture

# Regenerate all SVGs
python3 misc/scripts/generate-diagrams.py
```

See [../README.md](../README.md) and [../../AGENTS.md](../../AGENTS.md) for full repo context.
