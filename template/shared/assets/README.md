# Shared assets

Bundled resources copied into generated Desktop and Android projects at scaffold time.

| Asset | Path | Purpose |
|-------|------|---------|
| Nexus logo | [nexus-logo.png](nexus-logo.png) | About screens, splash |
| Nerd Font | [fonts/](fonts/) | Icon glyphs for ImGui labels |

The client copies `template/shared/assets/` into each project's `assets/` directory. The scaffold repo also keeps a copy at `docs/assets/nexus-logo.png` for README references.

Do not add logos to `.gitignore`.
