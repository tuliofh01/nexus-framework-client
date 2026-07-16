<!--
  description: Nexus Framework генерирует нативные десктопные и Android приложения на C++/Lua/Python из визуальных blueprints. Никакого Electron, облаков или браузера — только скомпилированный бинарный файл под вашим контролем. SDL3 + ImGui + Zig 0.14.
  keywords: генератор нативных приложений, C++ фреймворк, модули C++20, Lua скриптинг, встроенный Python, SDL3, ImGui, Zig сборка, Zig JNI, Compose Desktop, разработка на основе blueprint, кроссплатформенный десктоп, нативное Android приложение, без Electron, генератор проектов, графовая архитектура, arena allocator, pybind11, sol2, Chaquopy
-->
# Nexus Framework — Генератор Нативных Приложений: C++ + Lua + Python из визуальных Blueprint

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>🧩 Нарисуйте приложение как граф. Получите скомпилированный нативный бинарник. Без браузера. Без Electron. Без облака.</strong></p>

---

## Быстрый старт

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MyApp"
cd builds/framework/MyApp && zig build
```

## Документация

| Документ | Описание |
|:---------|:---------|
| [docs/hub.md](../docs/hub.md) | Центр документации |
| [docs/architecture/overview.md](../docs/architecture/overview.md) | Архитектура, языки, pipeline, шаблоны |
| [docs/guides/coding-with-nexus.md](../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, темы |
| [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md) | Справочник `blueprint.json` + `flows.json` |

## Лицензия

Apache 2.0 — см. [LICENSE](../LICENSE).

*Нарисуйте приложение как граф, сгенерируйте дерево, поставьте бинарник — затем итерируйте в реальных слоях кода.*
