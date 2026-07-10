# Shared assets

Bundled resources copied into generated Desktop and Android projects.

| Asset | Path | Purpose |
|-------|------|---------|
| Nexus logo | [nexus-logo.png](nexus-logo.png) | About screens, splash, docs (also at `docs/assets/nexus-logo.png` in the scaffold repo) |
| Nerd Font | [fonts/](fonts/) | Icon glyphs for ImGui labels (see fonts README) |

At generation time the client copies `template/shared/assets/` into each project's `assets/` directory. CMake post-build steps also mirror runtime folders next to the binary.

## Logo

The flamingo Nexus logo is committed in both locations so templates and the scaffold README reference the same file:

- Scaffold: `docs/assets/nexus-logo.png`
- Templates: `template/shared/assets/nexus-logo.png`

Do not add logos to `.gitignore`.
