<!--
  description: Nexus Framework genera aplicaciones nativas de escritorio y Android en C++/Lua/Python desde blueprints visuales. Sin Electron, sin nube, sin navegador — solo un binario compilado que tú controlas. SDL3 + ImGui + Zig 0.16.
  keywords: generador de apps nativas, framework C++, módulos C++20, scripting Lua, Python embebido, SDL3, ImGui, Zig build, Zig JNI, Compose Desktop, desarrollo basado en blueprint, escritorio multiplataforma, app Android nativa, sin Electron, generador de proyectos, arquitectura basada en grafos, arena allocator, pybind11, sol2, Chaquopy
-->
# Nexus Framework — Generador de Apps Nativas: C++ + Lua + Python desde Blueprints Visuales

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>🧩 Dibuja tu app como un grafo. Obtén un binario nativo compilado. Sin navegador. Sin Electron. Sin nube.</strong></p>

<p align="center"><em>Simple cuando quieres. Poderoso cuando necesitas.</em></p>

---

## Inicio rápido

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MiApp"
cd builds/framework/MiApp && zig build
```

## Docs

| Doc | Cubre |
|:----|:-------|
| [docs/hub.md](../docs/hub.md) | Hub de documentación |
| [docs/architecture/overview.md](../docs/architecture/overview.md) | Arquitectura, lenguajes, pipeline, templates |
| [docs/guides/coding-with-nexus.md](../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, temas |
| [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md) | Referencia `blueprint.json` + `flows.json` |

## Licencia

Apache 2.0 — ver [LICENSE](../LICENSE).

*Dibuja tu app como un grafo, genera el árbol, entrega el binario — luego itera en capas de código reales.*
