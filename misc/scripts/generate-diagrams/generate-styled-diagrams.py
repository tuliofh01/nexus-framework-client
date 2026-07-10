#!/usr/bin/env python3
"""Generate styled architecture/example SVG diagrams for docs/assets/."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
DIAGRAMS = ROOT / "docs/assets/diagrams"
EXAMPLES = ROOT / "docs/assets/examples"

FONT = '"JetBrainsMono Nerd Font", "Symbols Nerd Font", Verdana, sans-serif'
MONO = '"JetBrainsMono Nerd Font", "Symbols Nerd Font", Consolas, monospace'
BG = "#f8f9fa"
ARROW_STROKE = "1.5"  # relationship linkers (not module borders)
ARROW_STROKE_ACCENT = "2"  # highlighted codegen arrow

# Nerd Font codepoints (Private Use Area)
NF = {
    "gear": "&#xf013;",
    "desktop": "&#xf108;",
    "phone": "&#xf3cd;",
    "python": "&#xe73c;",
    "chart": "&#xf080;",
    "package": "&#xf487;",
    "file": "&#xf15b;",
    "code": "&#xf121;",
    "terminal": "&#xf120;",
    "database": "&#xf1c0;",
    "android": "&#xf17b;",
    "box": "&#xf466;",
    "cog": "&#xf013;",
    "branch": "&#xf126;",
    "rocket": "&#xf135;",
    "wrench": "&#xf0ad;",
    "book": "&#xf02d;",
    "cloud": "&#xf0c2;",
    "robot": "&#xf544;",
    "search": "&#xf002;",
    "comment": "&#xf075;",
    "plug": "&#xf1e6;",
    "layer": "&#xf5fd;",
}


def defs(logo_href: str) -> str:
    return f"""  <defs>
    <marker id="arrow" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M0,0 L10,5 L0,10 Z" fill="#37474F"/>
    </marker>
    <marker id="arrow-blue" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M0,0 L10,5 L0,10 Z" fill="#1565C0"/>
    </marker>
    <style>
      .title {{ font: bold 22px {FONT}; fill: #1a1a2e; }}
      .subtitle {{ font: 13px {FONT}; fill: #455A64; }}
      .label {{ font: bold 13px {FONT}; fill: #263238; font-weight: 700; }}
      .desc {{ font: italic 11px {FONT}; fill: #78909C; }}
      .small {{ font: 11px {FONT}; fill: #546E7A; }}
      .mono {{ font: bold 12px {MONO}; fill: #263238; font-weight: 700; }}
      .legend-title {{ font: bold 12px {FONT}; fill: #37474F; }}
      .layer-label {{ font: bold 14px {FONT}; fill: #263238; }}
      .badge {{ font: bold 11px {FONT}; fill: #1565C0; }}
      .icon {{ font: 14px {MONO}; fill: #263238; }}
      .node {{ rx: 10; stroke-width: 2.5; }}
      .panel {{ rx: 12; stroke-width: 2.5; }}
      .legend-box {{ rx: 6; stroke-width: 2; }}
    </style>
  </defs>"""


def header(title: str, subtitle: str, logo_href: str, w: int) -> str:
    cx = w // 2
    return f"""  <rect width="{w}" height="100%" fill="{BG}"/>
  <image href="{logo_href}" x="24" y="16" width="40" height="40" preserveAspectRatio="xMidYMid meet"/>
  <text x="{cx}" y="38" text-anchor="middle" class="title">{title}</text>
  <text x="{cx}" y="58" text-anchor="middle" class="subtitle">{subtitle}</text>"""


def module(x, y, w, h, fill, stroke, icon, label, desc, mono=False) -> str:
    cls = "mono" if mono else "label"
    label_x = x + 34 if icon else x + 12
    icon_el = f'\n  <text x="{x + 12}" y="{y + 22}" class="icon">{icon}</text>' if icon else ""
    return f"""  <rect x="{x}" y="{y}" width="{w}" height="{h}" class="node" fill="{fill}" stroke="{stroke}"/>{icon_el}
  <text x="{label_x}" y="{y + 22}" class="{cls}">{label}</text>
  <text x="{x + 12}" y="{y + h - 10}" class="desc">{desc}</text>"""


def layer_box(x, y, w, h, fill, stroke, label, icon="") -> str:
    return f"""  <rect x="{x}" y="{y}" width="{w}" height="{h}" class="panel" fill="{fill}" stroke="{stroke}" fill-opacity="0.35"/>
  <text x="{x + 14}" y="{y + 24}" class="layer-label">{icon} {label}</text>"""


def arrow(x1, y1, x2, y2, label="", dashed=False, color="#37474F") -> str:
    dash = ' stroke-dasharray="8,5"' if dashed else ""
    mid = ""
    if label:
        mx, my = (x1 + x2) // 2, (y1 + y2) // 2 - 6
        mid = f'\n  <text x="{mx}" y="{my}" text-anchor="middle" class="small">{label}</text>'
    return f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"{dash}/>{mid}'


def legend_box(x, y, w, h, items: list[tuple[str, str, str]], title="Legend") -> str:
    rows = ""
    col_w = w // 2
    for i, (fill, stroke, text) in enumerate(items):
        col = i % 2
        row = i // 2
        ix = x + 16 + col * col_w
        iy = y + 36 + row * 22
        rows += f"""
  <rect x="{ix}" y="{iy - 12}" width="16" height="16" class="legend-box" fill="{fill}" stroke="{stroke}"/>
  <text x="{ix + 22}" y="{iy}" class="small">{text}</text>"""
    lh = 36 + ((len(items) + 1) // 2) * 22 + 8
    return f"""  <rect x="{x}" y="{y}" width="{w}" height="{lh}" fill="#FFFFFF" stroke="#B0BEC5" stroke-width="2" rx="10"/>
  <text x="{x + 16}" y="{y + 22}" class="legend-title">{title}</text>{rows}"""


def full_stack_architecture() -> str:
    w, h = 1580, 920
    logo = "../nexus-logo.png"
    s = f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Nexus Framework — Full Stack + Blueprint Authoring</title>
{defs(logo)}
{header("Nexus Framework — Full Stack + Blueprint Authoring", "Scaffold client, blueprint graph, and generated native app", logo, w)}

{layer_box(620, 78, 360, 400, "#E8F4FD", "#1565C0", "Scaffold client (:app)", NF["desktop"])}
{module(640, 118, 320, 58, "#FFFFFF", "#1565C0", NF["rocket"], "Generate Project", "Compose UI entry for project scaffolding")}
{module(640, 192, 320, 58, "#FFFFFF", "#1565C0", NF["code"], "Blueprint Editor", "Visual canvas + JSON sync for blueprint.json")}
{module(640, 266, 320, 58, "#FFFFFF", "#1565C0", NF["gear"], ":core ProjectGenerator", "Reads graph and emits native project tree")}

{layer_box(24, 500, 760, 380, "#FFF8E1", "#F57F17", "Authoring — blueprint.json", NF["file"])}
{module(44, 540, 180, 58, "#FFFFFF", "#F57F17", NF["python"], "python.module", "NumPy/analytics hooks evaluated at runtime")}
{module(44, 618, 180, 58, "#FFFFFF", "#F57F17", NF["code"], "cpp.controller", "Routes commands between UI and model")}
{module(44, 696, 180, 58, "#FFFFFF", "#F57F17", NF["database"], "cpp.model", "Holds domain state and sample cache")}
{module(44, 774, 180, 58, "#FFFFFF", "#F57F17", NF["layer"], "ui.page", "ImGui screens wired via TS/XHTML DSL")}
{module(260, 540, 180, 58, "#FFFFFF", "#F57F17", NF["terminal"], "lua.script", "sol2 panels and hot-reload scripts")}
{module(480, 560, 280, 100, "#FFFDE7", "#F9A825", NF["branch"], "Langflow-style graph", "Edit blueprint in :app; edges wire data flow", mono=True)}

{layer_box(820, 500, 380, 380, "#F3E5F5", "#6A1B9A", "Generated app — C++ MVC", NF["box"])}
{module(840, 540, 160, 58, "#FFFFFF", "#6A1B9A", NF["database"], "model/", "Generated C++ domain types and state")}
{module(840, 618, 160, 58, "#FFFFFF", "#6A1B9A", NF["gear"], "controller/", "Command handlers from blueprint ports")}
{module(840, 696, 340, 58, "#FFFFFF", "#6A1B9A", NF["chart"], "view/ ImGui + ImPlot", "Renders plots and UI from blueprint layout")}

{layer_box(1220, 500, 340, 200, "#E8F5E9", "#2E7D32", "Scripting &amp; UI", NF["code"])}
{module(1240, 540, 140, 58, "#FFFFFF", "#2E7D32", NF["terminal"], "Lua 5.4 + sol2", "Embeds Lua runtime for scripting panels")}
{module(1400, 540, 140, 58, "#FFFFFF", "#2E7D32", NF["file"], "TS/XHTML DSL", "Declarative UI markup compiled to ImGui")}

{layer_box(820, 720, 380, 160, "#ECEFF1", "#455A64", "Runtime", NF["desktop"])}
{module(840, 760, 160, 58, "#FFFFFF", "#455A64", NF["python"], "Python bridge", "Embeds CPython via pybind11 / Chaquopy")}
{module(1020, 760, 160, 58, "#FFFFFF", "#455A64", NF["desktop"], "SDL3", "Cross-platform render loop and input")}

{arrow(800, 147, 800, 192, "Edit blueprint")}
{arrow(800, 250, 800, 266, "validated graph")}
{arrow(960, 147, 960, 266, "Generate")}
{arrow(780, 324, 404, 500, "emit blueprint.json", dashed=True)}
{arrow(960, 324, 1010, 500, "emit src/")}
{arrow(224, 598, 224, 618, "evaluate")}
{arrow(134, 676, 134, 696, "sampleCache")}
{arrow(134, 754, 134, 774, "activeCurves")}
{arrow(224, 774, 224, 676, "commands")}
{arrow(350, 569, 224, 618, "commands")}
{arrow(760, 520, 920, 540, "declares", dashed=True)}
{arrow(760, 647, 920, 647, "declares", dashed=True)}
{arrow(760, 725, 920, 725, "declares", dashed=True)}
{arrow(1240, 569, 1000, 647)}
{arrow(1470, 569, 1010, 725)}
{arrow(1010, 754, 1100, 789)}
{arrow(920, 676, 920, 696)}
{arrow(920, 754, 1100, 789)}

{legend_box(1220, 720, 340, 160, [
    ("#E8F4FD", "#1565C0", "Client — Compose scaffold UI"),
    ("#FFF8E1", "#F57F17", "Authoring — blueprint.json graph"),
    ("#F3E5F5", "#6A1B9A", "Generated — C++ MVC output"),
    ("#ECEFF1", "#455A64", "Runtime — SDL3 + Python bridge"),
], "Layer colors")}
</svg>"""
    return s


def generation_builds_flow() -> str:
    w, h = 2100, 900
    logo = "../nexus-logo.png"
    steps = [
        (80, 140, "Run misc/client-setup/", "Installs JDK 26 and Git before first build", "#E8F5E9", "#2E7D32"),
        (80, 230, "source env.sh", "Activates toolchain for Gradle and CMake", "#E8F5E9", "#2E7D32"),
        (340, 140, "./gradlew :app:run", "Launches Compose Desktop scaffold client", "#E3F2FD", "#1565C0"),
        (340, 230, "Generate Project screen", "Collects name, type, and output path", "#E3F2FD", "#1565C0"),
        (340, 340, "Blueprint Editor?", "Optional visual edit of blueprint.json", "#FFF3E0", "#EF6C00"),
        (340, 450, "BlueprintValidator", "Schema check before codegen proceeds", "#FFF3E0", "#EF6C00"),
        (340, 540, "ProjectGenerator", "Orchestrates template copy and emit", "#FFF3E0", "#EF6C00"),
        (620, 140, "Read template/", "desktop-app or android-app skeleton", "#F3E5F5", "#6A1B9A"),
        (620, 230, "TemplateEngine", "Substitutes {{placeholders}} in files", "#F3E5F5", "#6A1B9A"),
        (620, 320, "Validate blueprint.json", "Ensures graph nodes and edges are valid", "#F3E5F5", "#6A1B9A"),
        (620, 410, "Copy template/shared/", "DSL, themes, and runtime helpers", "#F3E5F5", "#6A1B9A"),
        (620, 500, "Write nxs_config.json", "Schema v2 project metadata on disk", "#F3E5F5", "#6A1B9A"),
        (900, 320, "Emit builds/framework/", "Out-of-source native project tree", "#E0F7FA", "#00838F"),
        (1180, 230, "Desktop?", "Branch on project type selection", "#ECEFF1", "#455A64"),
        (1400, 140, "cmake --preset debug", "Configure CMake + Ninja build files", "#E3F2FD", "#1565C0"),
        (1400, 230, "cmake --build", "Compile C++ sources and link binary", "#E3F2FD", "#1565C0"),
        (1400, 320, "Run native binary", "SDL3 desktop app with pybind11", "#E3F2FD", "#1565C0"),
        (1620, 140, "assembleDebug", "Gradle builds Android APK via NDK", "#FCE4EC", "#C2185B"),
        (1620, 230, "Djinni + Chaquopy", "JNI bridge and embedded Python", "#FCE4EC", "#C2185B"),
        (1620, 320, "Install APK", "Deploy to device or emulator", "#FCE4EC", "#C2185B"),
        (1860, 230, "deployToBuildsClient", "Copy client distro to builds/client/", "#E8EAF6", "#3949AB"),
    ]
    icons = [NF["wrench"], NF["terminal"], NF["desktop"], NF["layer"], NF["branch"], NF["gear"],
             NF["rocket"], NF["package"], NF["code"], NF["file"], NF["box"], NF["file"],
             NF["box"], NF["branch"], NF["desktop"], NF["gear"], NF["rocket"],
             NF["android"], NF["plug"], NF["phone"], NF["cloud"]]
    body = ""
    for i, (x, y, label, desc, fill, stroke) in enumerate(steps):
        body += module(x, y, 220, 62, fill, stroke, icons[i], label, desc) + "\n"
    # swimlane labels
    lanes = """
  <text x="190" y="110" text-anchor="middle" class="layer-label">First run</text>
  <text x="450" y="110" text-anchor="middle" class="layer-label">Scaffold client</text>
  <text x="730" y="110" text-anchor="middle" class="layer-label">:core / :cli</text>
  <text x="990" y="110" text-anchor="middle" class="layer-label">Output</text>
  <text x="1300" y="110" text-anchor="middle" class="layer-label">Native build</text>
  <text x="1750" y="110" text-anchor="middle" class="layer-label">Optional</text>
  <line x1="280" y1="120" x2="280" y2="620" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>
  <line x1="560" y1="120" x2="560" y2="620" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>
  <line x1="840" y1="120" x2="840" y2="620" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>
  <line x1="1120" y1="120" x2="1120" y2="620" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>
  <line x1="1360" y1="120" x2="1360" y2="620" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>
  <line x1="1780" y1="120" x2="1780" y2="620" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>
"""
    flows = f"""
  <line x1="190" y1="202" x2="190" y2="230" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="300" y1="171" x2="340" y2="171" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="450" y1="202" x2="450" y2="230" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="450" y1="292" x2="450" y2="340" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="450" y1="402" x2="450" y2="450" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="450" y1="512" x2="450" y2="540" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="560" y1="571" x2="620" y2="171" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="730" y1="202" x2="730" y2="230" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="730" y1="292" x2="730" y2="320" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="730" y1="382" x2="730" y2="410" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="730" y1="472" x2="730" y2="500" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="840" y1="351" x2="900" y2="351" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1120" y1="351" x2="1180" y2="261" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1290" y1="261" x2="1400" y2="171" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1290" y1="261" x2="1620" y2="171" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1510" y1="202" x2="1510" y2="230" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1510" y1="292" x2="1510" y2="320" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1730" y1="202" x2="1730" y2="230" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1730" y1="292" x2="1730" y2="320" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1680" y1="351" x2="1860" y2="261" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <rect x="600" y="580" width="320" height="70" class="panel" fill="#FFFDE7" stroke="#F9A825"/>
  <text x="620" y="608" class="small">Headless: ./gradlew :cli:run --args="generate …"</text>
  <text x="620" y="628" class="desc">Optional Docker path via misc/docker/</text>
"""
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Nexus — Generation &amp; Builds Flow</title>
{defs(logo)}
{header("Nexus — Generation &amp; Builds Flow", "From first-run setup through native build and optional client deploy", logo, w)}
{lanes}
{body}
{flows}
{legend_box(24, 680, 500, 120, [
    ("#E8F5E9", "#2E7D32", "Setup — JDK 26 + Git bootstrap"),
    ("#E3F2FD", "#1565C0", "Client — Compose Generate UI"),
    ("#F3E5F5", "#6A1B9A", "Core — template engine + emit"),
    ("#FCE4EC", "#C2185B", "Android — Gradle + Chaquopy path"),
], "Pipeline stages")}
</svg>"""


def desktop_vs_android() -> str:
    w, h = 1500, 820
    logo = "../nexus-logo.png"
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Nexus Generated App — Desktop vs Android Runtime</title>
{defs(logo)}
{header("Nexus Generated App — Desktop vs Android Runtime", "Shared blueprint graph; platform-specific runtime stacks", logo, w)}

{layer_box(420, 78, 480, 340, "#F3E5F5", "#6A1B9A", "Shared (both templates)", NF["layer"])}
{module(440, 118, 200, 62, "#FFFFFF", "#6A1B9A", NF["file"], "blueprint.json", "Loads blueprint.json at generate time")}
{module(660, 118, 220, 62, "#FFFFFF", "#6A1B9A", NF["terminal"], "Lua 5.4 + sol2", "Scripting layer shared across targets")}
{module(440, 200, 440, 62, "#FFFFFF", "#6A1B9A", NF["code"], "C++20 MVC", "model · controller · view from graph")}
{module(440, 282, 200, 62, "#FFFFFF", "#6A1B9A", NF["chart"], "ImGui + ImPlot", "Immediate-mode UI and plotting")}
{module(660, 282, 220, 62, "#FFFFFF", "#6A1B9A", NF["desktop"], "SDL3 render loop", "Cross-platform graphics and input")}

{layer_box(24, 450, 520, 340, "#E3F2FD", "#1565C0", "Desktop runtime", NF["desktop"])}
{module(44, 490, 220, 62, "#FFFFFF", "#1565C0", NF["gear"], "CMake + Ninja", "OpenGL 3.3 native build pipeline")}
{module(280, 490, 240, 62, "#FFFFFF", "#1565C0", NF["python"], "pybind11 embed", "Embeds CPython via pybind11")}
{module(44, 580, 220, 62, "#FFFFFF", "#1565C0", NF["file"], "python/functions.py", "In-process NumPy analytics module")}
{module(280, 580, 240, 62, "#FFFFFF", "#1565C0", NF["box"], "Native binary", "Win · macOS · Linux executable")}

{layer_box(760, 450, 520, 340, "#FCE4EC", "#C2185B", "Android runtime", NF["phone"])}
{module(780, 490, 220, 62, "#FFFFFF", "#C2185B", NF["android"], "Gradle + NDK", "APK build with native C++ libs")}
{module(1020, 490, 240, 62, "#FFFFFF", "#C2185B", NF["desktop"], "SDL3 GLES", "Full-screen touch UI on GLES")}
{module(780, 580, 220, 62, "#FFFFFF", "#C2185B", NF["plug"], "Djinni IDL", "C++ ↔ Kotlin JNI codegen")}
{module(1020, 580, 240, 62, "#FFFFFF", "#C2185B", NF["python"], "Chaquopy", "app/src/main/python/ embedding")}

{arrow(540, 180, 540, 200, "wires nodes")}
{arrow(770, 149, 660, 200)}
{arrow(540, 262, 540, 282)}
{arrow(540, 344, 154, 450, "desktop")}
{arrow(540, 344, 900, 450, "Android NDK")}
{arrow(154, 552, 154, 580)}
{arrow(400, 552, 280, 580)}
{arrow(900, 552, 900, 580)}
{arrow(1020, 552, 890, 580)}

  <rect x="960" y="118" width="300" height="70" class="panel" fill="#FFFDE7" stroke="#F9A825"/>
  <text x="980" y="148" class="small">Same plotter template on both targets.</text>
  <text x="980" y="168" class="desc">blueprint.json declares the same node graph.</text>

  <rect x="560, 680" width="360" height="50" class="panel" fill="#E8F5E9" stroke="#2E7D32"/>
  <text x="580" y="710" class="small">Lowest latency: Python runs in-process via pybind11.</text>

{legend_box(24, 720, 480, 80, [
    ("#F3E5F5", "#6A1B9A", "Shared — blueprint + MVC + SDL3"),
    ("#E3F2FD", "#1565C0", "Desktop — CMake + pybind11"),
    ("#FCE4EC", "#C2185B", "Android — Gradle + Djinni + Chaquopy"),
], "Runtime layers")}
</svg>"""


def langflow_vs_n8n() -> str:
    w, h = 1240, 580
    logo = "../nexus-logo.png"
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow vs n8n vs Nexus blueprint.json</title>
{defs(logo)}
{header("Blueprint paradigms — Langflow · n8n · Nexus", "Same visual wiring metaphor; different purpose and execution timing", logo, w)}

{layer_box(24, 88, 360, 420, "#E8F5E9", "#2E7D32", "Langflow", NF["robot"])}
  <text x="204" y="130" text-anchor="middle" class="desc">ML / LLM flow DAG · Runtime execution</text>
{module(64, 150, 120, 58, "#FFFFFF", "#2E7D32", NF["comment"], "Prompt", "User text input to the flow")}
{module(224, 150, 120, 58, "#FFFFFF", "#1565C0", NF["robot"], "LLM", "Inference node calls model API")}
{module(144, 240, 120, 58, "#FFFFFF", "#EF6C00", NF["code"], "Parser", "Structured output extraction")}
  <line x1="184" y1="208" x2="224" y2="179" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="284" y1="208" x2="204" y2="240" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <text x="44" y="330" class="small">• Typed nodes (model, tool, memory)</text>
  <text x="44" y="350" class="small">• Runs when user invokes the flow</text>
  <text x="44" y="370" class="desc">Output: inference / chat response</text>

{layer_box(420, 88, 360, 420, "#FCE4EC", "#C2185B", "n8n", NF["plug"])}
  <text x="600" y="130" text-anchor="middle" class="desc">Workflow automation · Runtime execution</text>
{module(460, 150, 120, 58, "#FFFFFF", "#C2185B", NF["cloud"], "Webhook", "HTTP trigger starts workflow")}
{module(620, 150, 120, 58, "#FFFFFF", "#3949AB", NF["plug"], "HTTP Request", "Calls external REST APIs")}
{module(540, 240, 120, 58, "#FFFFFF", "#00695C", NF["comment"], "Slack", "Posts message to channel")}
  <line x1="580" y1="179" x2="620" y2="179" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="680" y1="208" x2="600" y2="240" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <text x="440" y="330" class="small">• Triggers + integration steps</text>
  <text x="440" y="350" class="small">• Runs on schedule or webhook</text>
  <text x="440" y="370" class="desc">Output: side effects (API calls)</text>

{layer_box(816, 88, 400, 420, "#F3E5F5", "#6A1B9A", "Nexus blueprint.json", NF["file"])}
  <text x="1016" y="130" text-anchor="middle" class="badge">Design-time codegen</text>
{module(836, 150, 110, 52, "#FFFFFF", "#2E7D32", NF["python"], "python.module", "Analytics hooks in blueprint")}
{module(966, 150, 110, 52, "#FFFFFF", "#1565C0", NF["database"], "cpp.model", "Domain state node type")}
{module(896, 218, 110, 52, "#FFFFFF", "#6A1B9A", NF["gear"], "cpp.controller", "Command routing layer")}
{module(1026, 218, 110, 52, "#FFFFFF", "#EF6C00", NF["layer"], "ui.page", "ImGui screen definition")}
{module(966, 286, 110, 52, "#FFFFFF", "#00838F", NF["terminal"], "lua.script", "sol2 panel bindings")}
  <line x1="891" y1="176" x2="951" y2="218" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="951" y1="270" x2="1021" y2="202" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="1081" y1="244" x2="1026" y2="218" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <text x="836" y="360" class="small">• Langflow-style typed DAG (not n8n triggers)</text>
  <text x="836" y="380" class="small">• Consumed at generate time (:core)</text>
  <text x="836" y="400" class="desc">Output: C++/Lua/Python/UI project tree</text>

  <rect x="24" y="520" width="1192" height="44" fill="#FFFFFF" stroke="#B0BEC5" stroke-width="2" rx="10"/>
  <text x="620" y="548" text-anchor="middle" class="small">Nexus maps Langflow mental model (typed nodes + edges) to native app codegen — not runtime webhook automation.</text>
</svg>"""


def langflow_rag_chatbot() -> str:
    w, h = 1520, 460
    logo = "../nexus-logo.png"
    nodes = [
        (40, 120, "Chat Input", "User-facing message entry point", "#E8F5E9", "#2E7D32", NF["comment"]),
        (200, 120, "PDF Loader", "Ingests documents into pipeline", "#E3F2FD", "#1565C0", NF["file"]),
        (360, 120, "Text Splitter", "Chunks text for embedding", "#F3E5F5", "#7B1FA2", NF["code"]),
        (520, 120, "Embeddings", "Vectorizes text chunks", "#FFF3E0", "#EF6C00", NF["chart"]),
        (680, 120, "Vector Store", "Persists vectors for retrieval", "#FCE4EC", "#C2185B", NF["database"]),
        (840, 120, "Retriever", "Similarity search at query time", "#E0F7FA", "#00838F", NF["search"]),
        (1000, 120, "LLM", "Generates answer from context", "#E8EAF6", "#3949AB", NF["robot"]),
        (1160, 120, "Chat Output", "Returns response to user", "#E8F5E9", "#2E7D32", NF["comment"]),
    ]
    body = ""
    for x, y, label, desc, fill, stroke, icon in nodes:
        body += module(x, y, 140, 64, fill, stroke, icon, label, desc) + "\n"
    edges = ""
    xs = [180, 340, 500, 660, 820, 980, 1140, 1300]
    for i in range(len(xs) - 1):
        edges += f'  <line x1="{xs[i]}" y1="152" x2="{xs[i+1]-140}" y2="152" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>\n'
    edges += f'  <path d="M110,184 Q110,240 910,240 910,184" fill="none" stroke="#90A4AE" stroke-width="{ARROW_STROKE}" stroke-dasharray="8,5" marker-end="url(#arrow)"/>\n'
    edges += '  <text x="510" y="258" text-anchor="middle" class="desc">user query shortcut at chat time</text>\n'
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow Example — RAG Chatbot</title>
{defs(logo)}
{header("Langflow Example — RAG Chatbot", "Classic retrieval-augmented generation pipeline (runtime LLM flow)", logo, w)}
{body}
{edges}
{legend_box(40, 300, 1440, 120, [
    ("#E8F5E9", "#2E7D32", "I/O — user-facing input and output"),
    ("#E3F2FD", "#1565C0", "Loader — ingest documents"),
    ("#FFF3E0", "#EF6C00", "Model — embeddings and LLM inference"),
    ("#FCE4EC", "#C2185B", "Memory — vector database persistence"),
    ("#E0F7FA", "#00838F", "Retriever — similarity search"),
], "Node categories")}
</svg>"""


def langflow_agent_tools() -> str:
    w, h = 1000, 600
    logo = "../nexus-logo.png"
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow Example — Agent with Tools</title>
{defs(logo)}
{header("Langflow Example — Agent with Tools", "LLM agent selects tools at runtime based on user intent", logo, w)}

{module(80, 200, 150, 64, "#E8F5E9", "#2E7D32", NF["comment"], "User Input", "Natural language task from user")}
{module(380, 190, 220, 84, "#E8EAF6", "#3949AB", NF["robot"], "Agent", "ReAct / tool-calling LLM orchestrator")}
{module(740, 200, 150, 64, "#E8F5E9", "#2E7D32", NF["comment"], "Output", "Synthesized final answer to user")}

{module(120, 400, 160, 64, "#FFF3E0", "#EF6C00", NF["gear"], "Calculator", "Arithmetic tool invocation")}
{module(420, 400, 160, 64, "#E3F2FD", "#1565C0", NF["search"], "Web Search", "Live web lookup capability")}
{module(720, 400, 160, 64, "#F3E5F5", "#7B1FA2", NF["python"], "Python REPL", "Execute code snippets safely")}

  <line x1="230" y1="232" x2="380" y2="232" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="600" y1="232" x2="740" y2="232" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="420" y1="274" x2="200" y2="400" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="490" y1="274" x2="500" y2="400" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <line x1="560" y1="274" x2="800" y2="400" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <path d="M200,400 Q200,330 420,274" fill="none" stroke="#90A4AE" stroke-width="{ARROW_STROKE}" stroke-dasharray="8,5" marker-end="url(#arrow)"/>
  <path d="M500,400 Q500,340 490,274" fill="none" stroke="#90A4AE" stroke-width="{ARROW_STROKE}" stroke-dasharray="8,5" marker-end="url(#arrow)"/>
  <path d="M800,400 Q800,330 560,274" fill="none" stroke="#90A4AE" stroke-width="{ARROW_STROKE}" stroke-dasharray="8,5" marker-end="url(#arrow)"/>
  <text x="300" y="340" class="desc">tool result</text>

{legend_box(40, 490, 920, 90, [
    ("#E8F5E9", "#2E7D32", "I/O — message in, answer out"),
    ("#E8EAF6", "#3949AB", "Agent — orchestrates tool calls"),
    ("#FFF3E0", "#EF6C00", "Tool — callable capability"),
], "Legend — solid = request, dashed = tool result")}
</svg>"""


def nexus_blueprint_app_structure() -> str:
    w, h = 1140, 600
    logo = "../nexus-logo.png"
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Nexus Blueprint - App Structure (Design-Time)</title>
{defs(logo)}
{header("Nexus Blueprint — App Structure", "Langflow mental model translated to design-time MVC wiring", logo, w)}

  <rect x="380" y="72" width="380" height="28" fill="#E3F2FD" stroke="#1565C0" stroke-width="2" rx="14"/>
  <text x="570" y="91" text-anchor="middle" class="badge">DESIGN-TIME — consumed at generation, not runtime webhooks</text>

{layer_box(60, 110, 1020, 280, "#FAFAFA", "#B0BEC5", "blueprint.json graph (author in :app Blueprint Editor)", NF["branch"])}

{module(100, 170, 160, 68, "#E8F5E9", "#2E7D32", NF["layer"], "ui.page", "ImGui screens via TS/XHTML DSL", mono=True)}
{module(310, 170, 180, 68, "#E3F2FD", "#1565C0", NF["gear"], "cpp.controller", "Routes commands and data ports", mono=True)}
{module(540, 170, 160, 68, "#FFF3E0", "#EF6C00", NF["database"], "cpp.model", "Domain state and business logic", mono=True)}
{module(760, 140, 180, 68, "#F3E5F5", "#7B1FA2", NF["python"], "python.module", "ML samples and glue code", mono=True)}
{module(760, 230, 180, 68, "#E0F7FA", "#00838F", NF["terminal"], "lua.script", "sol2 panel bindings", mono=True)}

  <line x1="260" y1="204" x2="310" y2="204" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <text x="285" y="194" text-anchor="middle" class="desc">events</text>
  <line x1="490" y1="204" x2="540" y2="204" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <text x="515" y="194" text-anchor="middle" class="desc">commands</text>
  <line x1="700" y1="190" x2="760" y2="174" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>
  <text x="730" y="168" class="desc">data</text>
  <line x1="700" y1="218" x2="760" y2="264" stroke="#37474F" stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"/>

  <rect x="100" y="320" width="360" height="40" fill="#FFFFFF" stroke="#90A4AE" stroke-width="2" stroke-dasharray="6,4" rx="8"/>
  <text x="280" y="345" text-anchor="middle" class="desc">Not shown: external n8n webhooks (ops glue at app edge)</text>

  <line x1="570" y1="390" x2="570" y2="420" stroke="#1565C0" stroke-width="{ARROW_STROKE_ACCENT}" marker-end="url(#arrow-blue)"/>
  <text x="570" y="412" text-anchor="middle" class="badge">:core ProjectGenerator</text>

{module(380, 430, 380, 58, "#E8EAF6", "#3949AB", NF["box"], "builds/framework/&lt;name&gt;/", "Compiled SDL3 desktop or Android APK")}

{legend_box(60, 510, 1020, 70, [
    ("#E8F5E9", "#2E7D32", "ui.page — screens and DSL layout"),
    ("#E3F2FD", "#1565C0", "cpp.controller — MVC command layer"),
    ("#FFF3E0", "#EF6C00", "cpp.model — domain state"),
    ("#F3E5F5", "#7B1FA2", "python.module — pybind11 / Chaquopy"),
    ("#E0F7FA", "#00838F", "lua.script — sol2 panels"),
], "Nexus node types (blueprint.json)")}
</svg>"""


def main() -> None:
    outputs = {
        DIAGRAMS / "full-stack-architecture.svg": full_stack_architecture(),
        DIAGRAMS / "generation-builds-flow.svg": generation_builds_flow(),
        DIAGRAMS / "desktop-vs-android-runtime.svg": desktop_vs_android(),
        DIAGRAMS / "langflow-vs-n8n-blueprint.svg": langflow_vs_n8n(),
        EXAMPLES / "langflow-rag-chatbot.svg": langflow_rag_chatbot(),
        EXAMPLES / "langflow-agent-tools.svg": langflow_agent_tools(),
        EXAMPLES / "nexus-blueprint-app-structure.svg": nexus_blueprint_app_structure(),
    }
    for path, content in outputs.items():
        path.write_text(content, encoding="utf-8")
        size_kb = path.stat().st_size / 1024
        print(f"Wrote {path.name} ({size_kb:.1f} KB)")


if __name__ == "__main__":
    main()
