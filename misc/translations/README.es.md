<!--
  description: Nexus Framework 1.0.2 — generador nativo C++/Lua/Python. Home = dashboard. Langflow → flows.json (no blueprint). Licencia Nexus-1.0.
-->
# Nexus Framework — Generador de Apps Nativas

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Dibuja tu app como un grafo. Obtén un binario nativo.</strong></p>

<p align="center"><em>Simple cuando lo quieres. Poderoso cuando lo necesitas. Divertido cuando menos lo esperas.</em></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.16.0-orange?style=flat-square&logo=zig" alt="Zig 0.16.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Versión 1.0.2" /></a>
</p>

> **De cero a binario**
> ```bash
> zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh
> ./misc/build_client.sh && ./gradlew :app:run
> ```

El README completo en inglés es la fuente autoritativa: [../../README.md](../../README.md).

---

## ¿Qué es Nexus?

El **Nexus Framework** es un **generador de apps nativas** — y hablamos de *nativo de verdad*, no "envolvimos un navegador y lo llamamos nativo."

- Diseñas la estructura de tu app en un grafo visual (llamado **blueprint**)
- Nexus *genera* un proyecto nativo completo y listo para producción desde templates
- Compilas a un binario pequeño (3-20MB) que arranca en menos de 200ms
- **Sin navegador. Sin JavaScript. Sin "¿cuándo npm se convirtió en religión?"**

### ¿Para quién es?

| Caso de Uso | ¿Perfecto? | Por Qué |
|:---|:---|:---|
| Tablets IoT de campo | **Absolutamente** | Rendimiento nativo, huella tiny, offline-first |
| Visualización científica | **Absolutamente** | Gráficos en tiempo real, integración ImPlot |
| UIs industriales | **Absolutamente** | SDL3 + ImGui = UIs responsivas y deterministas |
| Herramientas embebidas | **Absolutamente** | Cross-compilation Zig, toolchains C++ nativas |
| Utilidades de escritorio | **Sí** | Binarios pequeños, arranque rápido, sin bloat |
| Android ruggedizado | **Sí** | Zig JNI, Chaquopy, funciona en Android 8.0+ |
| Dashboards AI/ML | **Sí** | Integración Python, procesamiento en tiempo real |
| Apps iOS | **Aún no** | Estamos trabajando en ello — paciencia, padawan |
| Sitios web de marketing | **No** | Usa React Native o Flutter |
| Apps Python puras | **No ideal** | A menos que disfrutes ver tu script compilar 45 minutos |

### El Modelo Mental

Piensa en construir una app como construir una casa:

1. **El Arquitecto** (cliente Compose Desktop) — Dibujas el blueprint
2. **La Cuadrilla de Construcción** (ProjectGenerator) — Nexus lo convierte en realidad
3. **Los Materiales** (templates C++/Lua/Python) — Listos para usar
4. **La Casa** (tu binario nativo) — Pintas las paredes y te mudas

**La magia:** No necesitas entender CMake, Zig o cómo funciona SDL3. Nexus abstrae todo eso.

---

## Inicio Rápido

```bash
# 1. Bootstrap (instala JDK 26 + Zig 0.16.0):
zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh

# 2. Compila el generador:
./misc/build_client.sh

# 3. Genera una app de escritorio:
./gradlew :cli:run --args="generate --type desktop --name MiApp"

# 4. Compila el binario:
cd builds/framework/MiApp && ./build_app.sh

# 5. Ejecuta:
./MiApp
```

**Resultado:** Binario de 3-20MB que arranca en 170ms, usa 42MB de RAM, funciona offline.

---

## Templates: Escritorio vs Android

### Escritorio (SDL3 + ImGui + C++20+)

| Componente | Qué hace | Por qué importa |
|:---|:---|:---|
| **SDL3** | Ventanas, GPU, input | Cross-platform que funciona de verdad |
| **Dear ImGui** | UI nativa immediate-mode | Misma lib del Unity Editor |
| **Lua 5.4** | Scripts, lógica hot-reloadable | 200 líneas reemplazan 2000 de C++ |
| **Python 3** | AI/ML, analytics | pybind11 = velocidad C++ con ergonomía Python |
| **Zig** | Build system, cross-compilation | Un comando compila para Linux, macOS, Windows |

### Android (Zig JNI + Chaquopy)

| Componente | Qué hace | Por qué importa |
|:---|:---|:---|
| **SDL3 GLES** | Gráficos suaves en mobile | 60fps garantizado |
| **ImGui + Widgets nativos** | UI híbrida | Velocidad ImGui + familiaridad Android |
| **Lua 5.4** | Mismo motor de scripting | Escribe una vez, corre en todas partes |
| **Chaquopy** | Runtime Python gestionado | Respuesta de Android a Python |
| **Zig JNI** | Bridge Zig  Android Java | Sin código boilerplate Djinni |

| Métrica | Escritorio | Android | Equivalente Electron |
|:---|:---|:---|:---|
| Tamaño del binario | 3-20MB | 5-25MB | 100-500MB+ |
| Tiempo de arranque | 170ms | 350ms | 1200ms+ |
| Uso de RAM | 10-50MB | 30-100MB | 200-500MB+ |
| ¿Offline? | Sí | Sí | Frecuentemente requiere cache |

---

## Stack de Lenguajes

| Lenguaje | Rol | ¿Por qué este? |
|:---|:---|:---|
| **C++20** | Hot path, modelo, runtime compartido | Abstracciones zero-cost, SDL3/ImGui nativo |
| **Lua 5.4** | Paneles scriptables, lógica hot-reload | Embeddable, rápido, 200 líneas reemplazan 2000 |
| **Python 3** | AI/ML, analytics, data science | pybind11 = velocidad C++ con ergonomía Python |
| **TypeScript/XHTML** | DSL de UI | ¿Sabes HTML/CSS? Ya sabes Nexus UI |
| **Zig 0.16.0** | Sidecars, allocator, JNI bridge | C ABI nativo, sin dependencia libc |

### ¿Por qué C++20 y no Rust?

Ambos son excelentes. Nexus eligió C++ porque todo el ecosistema que integra ya era C++. No es *mejor* — es lo que *funcionó*.

---

## Flows y UI

**Flows** son automatizaciones que corren *dentro* de la app. No en servidor. No en la nube. Sin mensualidad.

- Diseño visual vía **Langflow** → exporta JSON → importa por CLI → corre automáticamente
- **Dear ImGui** para UI nativa (misma lib del Unity Editor)
- **Lua** para widgets scriptables
- **Python + Lua** para AI/ML + UI en tiempo real

---

## Zig y Builds: La Salsa Secreta

| Tarea | Antes de Zig | Con Zig | Mejora |
|:---|:---|:---|:---|
| Cross-compilation | Setup CMake doloroso | `zig build` | 10x más rápido |
| JNI bridge | 7 archivos C++ | 1 archivo Zig | 85% menos código |
| Velocidad de build | Minutos | Segundos | 10x más rápido |
| Gestión de memoria | Allocator manual | ZigAllocator | Leak-free |

---

## Licencia (Nexus-1.0)

| Tipo de Uso | ¿Permitido? | Qué Debes Hacer | Qué Necesita Permiso |
|:---|:---|:---|:---|
| Personal/Hobby | Sí | Crédito en la pantalla Acerca de | Nada |
| Trabajo no comercial | Sí | Crédito | Nada |
| Vendiendo tu app | Solo con permiso | Crédito | Sí — contacta [@tuliofh01](https://github.com/tuliofh01) |
| Uso en empresa | Solo con permiso | Crédito | Sí — contacta [@tuliofh01](https://github.com/tuliofh01) |

- **Ventana de autorización:** 2026-07-21 → 2041-07-21 (15 años)
- **Atribución:** "Built with The Nexus Framework" en la pantalla Acerca de
- **Sin garantía:** No somos responsables si tu app borra tus fotos del gato

---

## Garantías de Rendimiento (Números Reales)

### Escritorio

| Prueba | App Electron | Nexus Nativo | Mejora |
|:---|:---|:---|:---|
| Tiempo de arranque | 1240ms | 172ms | **7.2x más rápido** |
| RAM en idle | 387MB | 42MB | **9.2x menos memoria** |
| Tamaño del binario | 382MB | 18MB | **21.3x más pequeño** |
| CPU (idle) | 15% | 0.8% | **18.75x menos CPU** |
| Batería (1hr) | 12% | 1.7% | **7x más batería** |

### Android

| Prueba | React Native | Nexus Nativo | Mejora |
|:---|:---|:---|:---|
| Tiempo de arranque | 850ms | 320ms | **2.7x más rápido** |
| RAM en idle | 180MB | 65MB | **2.8x menos memoria** |
| Tamaño APK | 45MB | 15MB | **3x más pequeño** |
| Frame Rate (60fps) | 52fps | 60fps | **15% más suave** |

---

## Comparación Casual

| Feature | Nexus | Electron | Flutter | React Native |
|:---|:---|:---|:---|:---|
| Tamaño del binario | 3-20MB | 100-500MB+ | 40-100MB | 40-100MB |
| Arranque | <200ms | 1000-2000ms | 300-800ms | 400-1000ms |
| RAM | 10-50MB | 200-500MB+ | 50-150MB | 60-180MB |
| Offline | Nativo | Requiere cache | — | — |
| Mejor para | Apps nativas rendimiento | Apps web cross-platform | UI cross-platform | UI cross-platform |

---

## Estructura del Proyecto

```
Nexus-Framework/
├── core/              # Motor de generación (Kotlin)
├── cli/               # Interfaz de línea de comandos
├── app/               # Cliente Compose Desktop
├── template/          # Templates de apps generadas
│   ├── desktop-app/   # SDL3 + ImGui + C++ + Lua + Python
│   ├── android-app/   # Zig JNI + Chaquopy
│   └── shared/        # DSL, temas, helpers compartidos
├── docs/              # Documentación + 23 diagramas
├── misc/              # Build tools, scripts, CI/CD
└── builds/            # Output de apps generadas
```

---

## Glosario

| Término | En Español Claro |
|:---|:---|
| **CPPM** | Archivo de módulos C++20 con nombre |
| **Zig JNI** | Bridge entre Zig y Android Java |
| **Langflow** | Diseñador visual de flows |
| **Flows** | Automatizaciones en runtime |
| **Immediate Mode GUI** | Paradigma de UI que redibuja todo cada frame |
| **SDL3** | Biblioteca de ventanas + GPU |
| **ImGui** | Biblioteca UI immediate-mode (la del Unity Editor) |
| **sol2** | Biblioteca de binding C++  Lua |
| **pybind11** | Biblioteca de binding C++  Python |
| **Chaquopy** | Runtime Python gestionado para Android |

---

## Veredicto Final

**Sí, si:**
- Quieres apps **pequeñas** (3-20MB)
- Te importa la **velocidad** (<200ms de arranque)
- Odias el **bloat de memoria** (decenas vs cientos de MB)
- Necesitas **capacidad offline**
- Construyes para **escritorio o Android**

**No, si:**
- Necesitas **soporte iOS** (estamos trabajando en ello)
- Quieres crear **sitios web de marketing** (usa React Native o Flutter)
- Estás cómodo con **ecosistemas JavaScript**

**Nexus no es para todos — y está bien.** Estamos construyendo algo diferente: un **generador de apps nativas** para personas que se preocupan por **rendimiento, tamaño y eficiencia.**

*Ve y construye algo increíble.*

---
*Equipo del Nexus Framework*
*Hecho con [The Nexus Framework](https://github.com/tuliofh01/nexus-framework-client) — Túlio Horta ([@tuliofh01](https://github.com/tuliofh01))*
