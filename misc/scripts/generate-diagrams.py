#!/usr/bin/env python3
"""Generate styled architecture/example SVG diagrams for docs/assets/.

INTEGRITY RULE (for AI coding assistants):
  Every time you modify architecture, template layout, build system, or any
  component represented in docs/assets/diagrams/*.svg, you MUST regenerate
  the affected SVGs by running this script. The SVGs are the SINGLE SOURCE
  OF TRUTH for visual documentation — ASCII art is NEVER allowed in committed
  files (see .omo/plans/zig-integration-diagrams.md § Phase 6).

  To regenerate all SVGs (architecture + UML activity diagrams):
    $ python3 misc/scripts/generate-diagrams.py

  New diagram functions: add a def new_diagram_name() that returns SVG string,
  then call it from __main__ and write the output to docs/assets/diagrams/.

  UML activity diagrams: activity_*() helpers under the "UML activity diagrams"
  section; indexed by docs/assets/diagrams/activity-diagrams.md.

  Palette reference:
    bg="#f8fafc", blue="#2563eb", green="#059669", orange="#d97706",
    purple="#7c3aed", slate="#475569", teal="#0d9488", red="#dc2626"
"""

import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]  # misc/scripts/ → repo root
DIAGRAMS = ROOT / "docs/assets/diagrams"
EXAMPLES = ROOT / "docs/assets/examples"

FONT = '"JetBrainsMono Nerd Font", "Symbols Nerd Font", Verdana, sans-serif'
MONO = '"JetBrainsMono Nerd Font", "Symbols Nerd Font", Consolas, monospace'
BG = "#f8fafc"
ARROW_STROKE = "2"
ARROW_STROKE_DASHED = "2"
ARROW_STROKE_ACCENT = "2"
ARROW_COLOR = "#475569"
ARROW_COLOR_DASHED = "#64748b"
ARROW_COLOR_ACCENT = "#2563eb"
LOGO_SIZE = 36

# Node layout — min box 180×72, 16px padding, 15/12px type
MIN_NODE_W = 180
MIN_NODE_H = 72
PAD_X = 16
PAD_Y = 16
ICON_COL = 22
LABEL_SIZE = 15
DESC_SIZE = 12
LABEL_LINE = 18
DESC_LINE = 15
LABEL_GAP = 8
CHAR_W_LABEL = 7.6
CHAR_W_DESC = 6.4

# Layout — minimum edge-to-edge gap between node boxes (px)
GAP_H = 56
GAP_V = 56
LANE = 12  # offset parallel arrows to avoid overlap

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
    <filter id="shadow" x="-4%" y="-4%" width="108%" height="112%">
      <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#94a3b8" flood-opacity="0.18"/>
    </filter>
    <marker id="arrow" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="{ARROW_COLOR}"/>
    </marker>
    <marker id="arrow-blue" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="{ARROW_COLOR_ACCENT}"/>
    </marker>
    <marker id="arrow-dashed" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="{ARROW_COLOR_DASHED}"/>
    </marker>
    <style>
      .label {{ font: bold 15px {FONT}; fill: #1e293b; font-weight: 700; }}
      .desc {{ font: italic 12px {FONT}; fill: #64748b; }}
      .small {{ font: 12px {FONT}; fill: #475569; }}
      .mono {{ font: bold 12px {MONO}; fill: #1e293b; font-weight: 700; }}
      .legend-title {{ font: bold 13px {FONT}; fill: #334155; }}
      .layer-label {{ font: bold 15px {FONT}; fill: #1e293b; }}
      .badge {{ font: bold 12px {FONT}; fill: {ARROW_COLOR_ACCENT}; }}
      .icon {{ font: 14px {MONO}; fill: #1e293b; }}
      .node {{ rx: 10; stroke-width: 2; filter: url(#shadow); }}
      .panel {{ rx: 12; stroke-width: 2; }}
      .legend-box {{ rx: 6; stroke-width: 2; }}
    </style>
  </defs>"""


def header(logo_href: str, w: int) -> str:
    return f"""  <rect width="{w}" height="100%" fill="{BG}"/>
  <image href="{logo_href}" x="24" y="16" width="{LOGO_SIZE}" height="{LOGO_SIZE}" preserveAspectRatio="xMidYMid meet"/>"""


def _escape_xml(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def _wrap_lines(text: str, max_width: float, char_width: float) -> list[str]:
    max_chars = max(1, int(max_width / char_width))
    words = text.split()
    if not words:
        return [""]
    lines: list[str] = []
    current = ""
    for word in words:
        trial = f"{current} {word}".strip() if current else word
        if len(trial) <= max_chars:
            current = trial
            continue
        if current:
            lines.append(current)
        while len(word) > max_chars:
            lines.append(word[:max_chars])
            word = word[max_chars:]
        current = word
    if current:
        lines.append(current)
    return lines


def _tspan_block(x: int, y: int, lines: list[str], cls: str, line_height: int) -> str:
    if not lines:
        return ""
    inner = ""
    for i, line in enumerate(lines):
        dy = 0 if i == 0 else line_height
        inner += f'\n    <tspan x="{x}" dy="{dy}">{_escape_xml(line)}</tspan>'
    return f'  <text x="{x}" y="{y}" class="{cls}">{inner}\n  </text>'


def _module_size(label: str, desc: str, w: int, h: int, has_icon: bool) -> tuple[int, int, list[str], list[str], int, int]:
    label_x_offset = PAD_X + (ICON_COL if has_icon else 0)
    desc_x_offset = PAD_X

    # Grow width until label fits on one line when possible, else wrap within box.
    natural_w = max(
        w,
        MIN_NODE_W,
        int(label_x_offset + len(label) * CHAR_W_LABEL + PAD_X),
        int(desc_x_offset + len(desc) * CHAR_W_DESC + PAD_X),
    )
    label_inner = natural_w - label_x_offset - PAD_X
    desc_inner = natural_w - 2 * PAD_X
    label_lines = _wrap_lines(label, label_inner, CHAR_W_LABEL)
    desc_lines = _wrap_lines(desc, desc_inner, CHAR_W_DESC)

    # Widen if wrapped lines still exceed inner width estimates.
    for line in label_lines:
        natural_w = max(natural_w, int(label_x_offset + len(line) * CHAR_W_LABEL + PAD_X))
    for line in desc_lines:
        natural_w = max(natural_w, int(desc_x_offset + len(line) * CHAR_W_DESC + PAD_X))

    label_inner = natural_w - label_x_offset - PAD_X
    desc_inner = natural_w - 2 * PAD_X
    label_lines = _wrap_lines(label, label_inner, CHAR_W_LABEL)
    desc_lines = _wrap_lines(desc, desc_inner, CHAR_W_DESC)

    content_h = (
        LABEL_SIZE
        + max(0, len(label_lines) - 1) * LABEL_LINE
        + LABEL_GAP
        + len(desc_lines) * DESC_LINE
    )
    natural_h = max(h, MIN_NODE_H, PAD_Y + content_h + PAD_Y)
    return natural_w, natural_h, label_lines, desc_lines, label_x_offset, desc_x_offset


def module(x, y, w, h, fill, stroke, icon, label, desc, mono=False) -> str:
    cls = "mono" if mono else "label"
    has_icon = bool(icon)
    box_w, box_h, label_lines, desc_lines, label_x_off, desc_x_off = _module_size(
        label, desc, w, h, has_icon
    )
    label_x = x + label_x_off
    desc_x = x + desc_x_off
    label_y = y + PAD_Y + LABEL_SIZE
    desc_y = label_y + max(0, len(label_lines) - 1) * LABEL_LINE + LABEL_GAP + DESC_SIZE

    icon_y = y + PAD_Y + LABEL_SIZE - 2
    icon_el = f'\n  <text x="{x + PAD_X}" y="{icon_y}" class="icon">{icon}</text>' if has_icon else ""
    label_el = _tspan_block(label_x, label_y, label_lines, cls, LABEL_LINE)
    desc_el = _tspan_block(desc_x, desc_y, desc_lines, "desc", DESC_LINE)

    return f"""  <rect x="{x}" y="{y}" width="{box_w}" height="{box_h}" class="node" fill="{fill}" stroke="{stroke}"/>{icon_el}
{label_el}
{desc_el}"""


def layer_box(x, y, w, h, fill, stroke, label, icon="") -> str:
    return f"""  <rect x="{x}" y="{y}" width="{w}" height="{h}" class="panel" fill="{fill}" stroke="{stroke}" fill-opacity="0.35"/>
  <text x="{x + 14}" y="{y + 24}" class="layer-label">{icon} {label}</text>"""


def _anchor(box: dict, side: str) -> tuple[int, int]:
    x, y, bw, bh = box["x"], box["y"], box["w"], box["h"]
    if side == "top":
        return x + bw // 2, y
    if side == "bottom":
        return x + bw // 2, y + bh
    if side == "left":
        return x, y + bh // 2
    if side == "right":
        return x + bw, y + bh // 2
    if side == "tl":
        return x, y
    if side == "tr":
        return x + bw, y
    if side == "bl":
        return x, y + bh
    if side == "br":
        return x + bw, y + bh
    return x + bw // 2, y + bh // 2


def _measure(label: str, desc: str, w: int, h: int, has_icon: bool) -> tuple[int, int]:
    bw, bh, *_ = _module_size(label, desc, w, h, has_icon)
    return bw, bh


def vstack(
    x: int,
    y: int,
    items: list[tuple],
    gap: int = GAP_V,
) -> tuple[str, list[dict], int]:
    parts: list[str] = []
    boxes: list[dict] = []
    cy = y
    for item in items:
        w, h, fill, stroke, icon, label, desc = item[:7]
        mono = item[7] if len(item) > 7 else False
        bw, bh = _measure(label, desc, w, h, bool(icon))
        parts.append(module(x, cy, w, h, fill, stroke, icon, label, desc, mono))
        boxes.append({"x": x, "y": cy, "w": bw, "h": bh})
        cy += bh + gap
    return "\n".join(parts), boxes, cy - gap


def hstack(
    x: int,
    y: int,
    items: list[tuple],
    gap: int = GAP_H,
) -> tuple[str, list[dict], int]:
    parts: list[str] = []
    boxes: list[dict] = []
    cx = x
    for item in items:
        w, h, fill, stroke, icon, label, desc = item[:7]
        mono = item[7] if len(item) > 7 else False
        bw, bh = _measure(label, desc, w, h, bool(icon))
        parts.append(module(cx, y, w, h, fill, stroke, icon, label, desc, mono))
        boxes.append({"x": cx, "y": y, "w": bw, "h": bh})
        cx += bw + gap
    return "\n".join(parts), boxes, cx - gap


def _arrow_style(dashed: bool, color: str | None = None, accent: bool = False) -> tuple[str, str, str]:
    if dashed:
        return (
            ' stroke-dasharray="6,4"',
            color or ARROW_COLOR_DASHED,
            f' stroke-width="{ARROW_STROKE_DASHED}" marker-end="url(#arrow-dashed)"',
        )
    if accent or color == ARROW_COLOR_ACCENT:
        return ("", color or ARROW_COLOR_ACCENT, f' stroke-width="{ARROW_STROKE_ACCENT}" marker-end="url(#arrow-blue)"')
    return ("", color or ARROW_COLOR, f' stroke-width="{ARROW_STROKE}" marker-end="url(#arrow)"')


def arrow(x1, y1, x2, y2, label="", dashed=False, color: str | None = None, lane: int = 0, accent: bool = False) -> str:
    dash, stroke, marker = _arrow_style(dashed, color, accent)
    mid = ""
    if lane:
        x1 += lane
        x2 += lane
    if label:
        mx, my = (x1 + x2) // 2, (y1 + y2) // 2 - 6
        mid = f'\n  <text x="{mx}" y="{my}" text-anchor="middle" class="small">{label}</text>'
    return f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{stroke}"{marker}{dash}/>{mid}'


def arrow_curve(
    x1: int,
    y1: int,
    x2: int,
    y2: int,
    label: str = "",
    dashed: bool = False,
    color: str | None = None,
    bend: int = 0,
) -> str:
    dash, stroke, marker = _arrow_style(dashed, color)
    cx = (x1 + x2) // 2 + bend
    cy = (y1 + y2) // 2
    mid = ""
    if label:
        mid = f'\n  <text x="{cx}" y="{cy - 8}" text-anchor="middle" class="small">{label}</text>'
    return (
        f'  <path d="M{x1},{y1} Q{cx},{cy} {x2},{y2}" fill="none" '
        f'stroke="{stroke}"{marker}{dash}/>{mid}'
    )


def arrow_curve_cubic(
    x1: int,
    y1: int,
    x2: int,
    y2: int,
    label: str = "",
    dashed: bool = False,
    color: str | None = None,
    bend1: int = 0,
    bend2: int = 0,
) -> str:
    """Cubic bezier — smoother S-curves for long diagonals."""
    dash, stroke, marker = _arrow_style(dashed, color)
    c1x = x1 + bend1
    c1y = y1
    c2x = x2 + bend2
    c2y = y2
    mid_x = (x1 + x2) // 2 + (bend1 + bend2) // 4
    mid_y = (y1 + y2) // 2
    mid = ""
    if label:
        mid = f'\n  <text x="{mid_x}" y="{mid_y - 8}" text-anchor="middle" class="small">{label}</text>'
    return (
        f'  <path d="M{x1},{y1} C{c1x},{c1y} {c2x},{c2y} {x2},{y2}" fill="none" '
        f'stroke="{stroke}"{marker}{dash}/>{mid}'
    )


def arrow_curve_via(
    x1: int,
    y1: int,
    x2: int,
    y2: int,
    c1x: int,
    c1y: int,
    c2x: int,
    c2y: int,
    label: str = "",
    dashed: bool = False,
    color: str | None = None,
) -> str:
    """Cubic bezier with explicit control points."""
    dash, stroke, marker = _arrow_style(dashed, color)
    mid_x = (c1x + c2x) // 2
    mid_y = (c1y + c2y) // 2
    mid = ""
    if label:
        mid = f'\n  <text x="{mid_x}" y="{mid_y - 8}" text-anchor="middle" class="desc">{label}</text>'
    return (
        f'  <path d="M{x1},{y1} C{c1x},{c1y} {c2x},{c2y} {x2},{y2}" fill="none" '
        f'stroke="{stroke}"{marker}{dash}/>{mid}'
    )


def arrow_ortho(
    x1: int,
    y1: int,
    x2: int,
    y2: int,
    via_y: int | None = None,
    via_x: int | None = None,
    label: str = "",
    dashed: bool = False,
    color: str | None = None,
    lane: int = 0,
    accent: bool = False,
) -> str:
    dash, stroke, marker = _arrow_style(dashed, color, accent)
    if via_y is not None:
        path = f"M{x1},{y1} L{x1 + lane},{via_y} L{x2 + lane},{via_y} L{x2},{y2}"
        lx, ly = (x1 + x2) // 2 + lane, via_y - 8
    elif via_x is not None:
        path = f"M{x1},{y1} L{via_x},{y1 + lane} L{via_x},{y2 + lane} L{x2},{y2}"
        lx, ly = via_x - 8, (y1 + y2) // 2 + lane
    else:
        mid_x = (x1 + x2) // 2 + lane
        path = f"M{x1},{y1} L{mid_x},{y1} L{mid_x},{y2} L{x2},{y2}"
        lx, ly = mid_x, (y1 + y2) // 2 - 8
    mid = ""
    if label:
        mid = f'\n  <text x="{lx}" y="{ly}" text-anchor="middle" class="small">{label}</text>'
    return (
        f'  <path d="{path}" fill="none" stroke="{stroke}" '
        f'{marker}{dash}/>{mid}'
    )


def arrow_between(
    src: dict,
    dst: dict,
    src_side: str = "bottom",
    dst_side: str = "top",
    label: str = "",
    dashed: bool = False,
    curve_bend: int | None = None,
    via_y: int | None = None,
    via_x: int | None = None,
    lane: int = 0,
    color: str | None = None,
    accent: bool = False,
) -> str:
    x1, y1 = _anchor(src, src_side)
    x2, y2 = _anchor(dst, dst_side)
    if curve_bend is not None:
        return arrow_curve(x1, y1, x2, y2, label, dashed, color, bend=curve_bend + lane)
    if via_y is not None or via_x is not None:
        return arrow_ortho(x1, y1, x2, y2, via_y, via_x, label, dashed, color, lane=lane, accent=accent)
    return arrow(x1, y1, x2, y2, label, dashed, color, lane=lane, accent=accent)


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
    logo = "../nexus-logo.png"

    client_x, client_y = 700, 96
    client_svg, client_boxes, _ = vstack(client_x + 20, client_y + 40, [
        (340, 72, "#FFFFFF", "#1565C0", NF["rocket"], "Generate Project", "Compose UI entry for project scaffolding"),
        (340, 72, "#FFFFFF", "#1565C0", NF["code"], "Blueprint Editor", "Visual canvas + JSON sync for blueprint.json"),
        (340, 72, "#FFFFFF", "#1565C0", NF["gear"], ":core ProjectGenerator", "Reads graph and emits native project tree"),
    ])
    client_w = max(b["w"] for b in client_boxes) + 40
    client_h = client_boxes[-1]["y"] + client_boxes[-1]["h"] - client_y + 24

    auth_x, auth_y = 32, 540
    left_svg, left_boxes, left_end = vstack(auth_x + 20, auth_y + 40, [
        (220, 72, "#FFFFFF", "#F57F17", NF["python"], "python.module", "NumPy/analytics hooks evaluated at runtime"),
        (220, 72, "#FFFFFF", "#F57F17", NF["code"], "cpp.controller", "Routes commands between UI and model"),
        (220, 72, "#FFFFFF", "#F57F17", NF["database"], "cpp.model", "Holds domain state and sample cache"),
        (220, 72, "#FFFFFF", "#F57F17", NF["layer"], "ui.page", "ImGui screens wired via TS/XHTML DSL"),
    ])
    lua_x = left_boxes[0]["x"] + left_boxes[0]["w"] + GAP_H
    lua_svg, lua_boxes, _ = vstack(lua_x, auth_y + 40, [
        (220, 72, "#FFFFFF", "#F57F17", NF["terminal"], "lua.script", "sol2 panels and hot-reload scripts"),
    ])
    graph_x = lua_boxes[0]["x"] + lua_boxes[0]["w"] + GAP_H
    graph_y = auth_y + 60
    graph_bw, graph_bh = _measure(
        "Langflow-style graph", "Edit blueprint in :app; edges wire data flow", 300, 110, True
    )
    graph_svg = module(
        graph_x, graph_y, 300, 110, "#FFFDE7", "#F9A825", NF["branch"],
        "Langflow-style graph", "Edit blueprint in :app; edges wire data flow", mono=True,
    )
    auth_w = graph_x + graph_bw - auth_x + 24
    auth_h = max(left_end, lua_boxes[0]["y"] + lua_boxes[0]["h"], graph_y + graph_bh) - auth_y + 24

    gen_x = graph_x + graph_bw + GAP_H
    gen_y = 540
    gen_svg, gen_boxes, gen_end = vstack(gen_x + 20, gen_y + 40, [
        (200, 72, "#FFFFFF", "#6A1B9A", NF["database"], "model/", "Generated C++ domain types and state"),
        (200, 72, "#FFFFFF", "#6A1B9A", NF["gear"], "controller/", "Command handlers from blueprint ports"),
        (380, 72, "#FFFFFF", "#6A1B9A", NF["chart"], "view/ ImGui + ImPlot", "Renders plots and UI from blueprint layout"),
    ])
    gen_w = max(b["w"] for b in gen_boxes) + 40
    gen_h = gen_end - gen_y + 24

    # zig-services + Scripting & UI layer
    scr_x = gen_x + gen_w + GAP_H
    zig_svg, zig_boxes, zig_end = hstack(scr_x + 20, gen_y + 40, [
        (240, 72, "#FFFFFF", "#2E7D32", NF["terminal"], "zig-services sidecar", "zig build · zig c++ compile · build.zig.zon deps"),
    ])
    sc2_x = zig_end + 20
    scr_svg, scr_boxes, scr_end = hstack(sc2_x + 20, gen_y + 40, [
        (180, 72, "#FFFFFF", "#2E7D32", NF["terminal"], "Lua 5.4 + sol2", "Embeds Lua runtime for scripting panels"),
        (180, 72, "#FFFFFF", "#2E7D32", NF["file"], "TS/XHTML DSL", "Declarative UI markup compiled to ImGui"),
    ])
    scr_w = scr_end - scr_x + 24
    scr_h = max(max(b["h"] for b in zig_boxes), max(b["h"] for b in scr_boxes)) + 60

    rt_y = gen_y + gen_h + GAP_V
    rt_svg, rt_boxes, rt_end = hstack(gen_x + 20, rt_y + 40, [
        (200, 72, "#FFFFFF", "#455A64", NF["python"], "Python bridge", "Embeds CPython via pybind11 / Chaquopy"),
        (200, 72, "#FFFFFF", "#455A64", NF["desktop"], "SDL3", "Cross-platform render loop and input"),
    ])
    rt_w = max(gen_w, rt_end - gen_x + 24)
    rt_h = max(b["h"] for b in rt_boxes) + 60

    arrows = [
        arrow_between(client_boxes[0], client_boxes[1], label="Edit blueprint"),
        arrow_between(client_boxes[1], client_boxes[2], label="validated graph"),
        arrow_between(client_boxes[0], client_boxes[2], "right", "right", "Generate", lane=LANE * 2),
        arrow_between(client_boxes[2], left_boxes[0], "bottom", "top", "emit blueprint.json", dashed=True, via_y=500),
        arrow_between(client_boxes[2], gen_boxes[0], "bottom", "top", "emit src/", via_x=gen_x - 40),
        arrow_between(left_boxes[0], left_boxes[1], label="evaluate"),
        arrow_between(left_boxes[1], left_boxes[2], label="sampleCache"),
        arrow_between(left_boxes[2], left_boxes[3], label="activeCurves"),
        arrow_curve(*_anchor(left_boxes[3], "left"), *_anchor(left_boxes[1], "left"), label="commands", bend=-60),
        arrow_between(lua_boxes[0], left_boxes[1], "left", "right", "commands", curve_bend=40),
        arrow_between(left_boxes[0], gen_boxes[0], "right", "left", "declares", dashed=True, lane=0),
        arrow_between(left_boxes[1], gen_boxes[1], "right", "left", "declares", dashed=True, lane=LANE),
        arrow_between(left_boxes[2], gen_boxes[2], "right", "left", "declares", dashed=True, lane=LANE * 2),
        arrow_between(scr_boxes[0], gen_boxes[1], "left", "right", via_y=gen_boxes[1]["y"] + gen_boxes[1]["h"] // 2 - 20),
        arrow_between(scr_boxes[1], gen_boxes[2], "left", "right", via_y=gen_boxes[2]["y"] + gen_boxes[2]["h"] // 2 + 20),
        arrow_between(gen_boxes[1], gen_boxes[2]),
        arrow_between(rt_boxes[0], rt_boxes[1]),
        arrow_between(gen_boxes[2], rt_boxes[1], "bottom", "top", via_x=rt_boxes[1]["x"] + rt_boxes[1]["w"] // 2),
        arrow_between(gen_boxes[1], rt_boxes[0], "bottom", "top", via_x=rt_boxes[0]["x"] + rt_boxes[0]["w"] // 2),
    ]

    legend_y = max(rt_y + rt_h, auth_y + auth_h) + GAP_V
    w = max(scr_x + scr_w, gen_x + gen_w, client_x + client_w) + 40
    h = legend_y + 200

    # Add zig arrow
    zig_box = zig_boxes[0]

    arrows.append(arrow_between(zig_box, gen_boxes[1], "left", "right", "zig c++", via_y=gen_boxes[1]["y"] + gen_boxes[1]["h"] // 2 - 40))
    arrows.append(arrow_between(gen_boxes[1], gen_boxes[2]))

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Nexus full-stack architecture</title>
{defs(logo)}
{header(logo, w)}

{layer_box(client_x, client_y, client_w, client_h, "#E8F4FD", "#1565C0", "Scaffold client (:app)", NF["desktop"])}
{client_svg}

{layer_box(auth_x, auth_y, auth_w, auth_h, "#FFF8E1", "#F57F17", "Authoring — blueprint.json", NF["file"])}
{left_svg}
{lua_svg}
{graph_svg}

{layer_box(gen_x, gen_y, gen_w, gen_h, "#F3E5F5", "#6A1B9A", "Generated app — C++ MVC", NF["box"])}
{gen_svg}

{layer_box(scr_x, gen_y, scr_w, scr_h, "#E8F5E9", "#2E7D32", "Build &amp; Scripting — Zig + Lua + DSL", NF["code"])}
{zig_svg}
{scr_svg}

{layer_box(gen_x, rt_y, rt_w, rt_h, "#ECEFF1", "#455A64", "Runtime", NF["desktop"])}
{rt_svg}

{chr(10).join(arrows)}

{legend_box(scr_x, legend_y, scr_w, 180, [
    ("#E8F4FD", "#1565C0", "Client — Compose scaffold UI"),
    ("#FFF8E1", "#F57F17", "Authoring — blueprint.json graph"),
    ("#F3E5F5", "#6A1B9A", "Generated — C++ MVC output"),
    ("#E8F5E9", "#2E7D32", "Build — Zig services sidecar"),
    ("#ECEFF1", "#455A64", "Runtime — SDL3 + Python bridge"),
], "Layer colors")}
</svg>"""


def generation_builds_flow() -> str:
    logo = "../nexus-logo.png"
    y0 = 140
    nw, nh = 260, 72

    def layout_columns(col_data: list[list[tuple]]) -> tuple[list[str], list[list[dict]], list[int]]:
        x = 40
        svgs: list[str] = []
        all_boxes: list[list[dict]] = []
        centers: list[int] = []
        for items in col_data:
            specs = [(nw, nh, f, s, ic, lb, ds) for lb, ds, f, s, ic in items]
            svg, boxes, _ = vstack(x, y0, specs)
            svgs.append(svg)
            all_boxes.append(boxes)
            centers.append(x + max(b["w"] for b in boxes) // 2)
            x += max(b["w"] for b in boxes) + GAP_H
        return svgs, all_boxes, centers

    col_data = [
        [
            ("Run misc/client-setup/", "Installs JDK 26 + Zig 0.14.0 before first build", "#E8F5E9", "#2E7D32", NF["wrench"]),
            ("source env.sh", "Activates toolchain for Gradle and Zig", "#E8F5E9", "#2E7D32", NF["terminal"]),
        ],
        [
            ("./gradlew :app:run", "Launches Compose Desktop scaffold client", "#E3F2FD", "#1565C0", NF["desktop"]),
            ("Generate Project screen", "Collects name, type, and output path", "#E3F2FD", "#1565C0", NF["layer"]),
            ("Blueprint Editor?", "Optional visual edit of blueprint.json", "#FFF3E0", "#EF6C00", NF["branch"]),
            ("BlueprintValidator", "Schema check before codegen proceeds", "#FFF3E0", "#EF6C00", NF["gear"]),
            ("ProjectGenerator", "Orchestrates template copy and emit", "#FFF3E0", "#EF6C00", NF["rocket"]),
        ],
        [
            ("Read template/", "desktop-app or android-app skeleton", "#F3E5F5", "#6A1B9A", NF["package"]),
            ("TemplateEngine", "Substitutes {{placeholders}} in files", "#F3E5F5", "#6A1B9A", NF["code"]),
            ("Validate blueprint.json", "Ensures graph nodes and edges are valid", "#F3E5F5", "#6A1B9A", NF["file"]),
            ("Copy template/shared/", "DSL, themes, and runtime helpers", "#F3E5F5", "#6A1B9A", NF["box"]),
            ("Write nxs_config.json", "Schema v2 project metadata on disk", "#F3E5F5", "#6A1B9A", NF["file"]),
        ],
        [
            ("Emit builds/framework/", "Out-of-source native project tree", "#E0F7FA", "#00838F", NF["box"]),
        ],
        [
            ("Desktop?", "Branch on project type selection", "#ECEFF1", "#455A64", NF["branch"]),
        ],
        [
            ("zig build", "Compile C++ and link native binary", "#E3F2FD", "#1565C0", NF["gear"]),
            ("Run native binary", "SDL3 desktop app with pybind11", "#E3F2FD", "#1565C0", NF["rocket"]),
        ],
        [
            ("assembleDebug", "Gradle builds Android APK", "#FCE4EC", "#C2185B", NF["android"]),
            ("Zig JNI + Chaquopy", "Native .so via zig build + Python embed", "#FCE4EC", "#C2185B", NF["plug"]),
            ("Install APK", "Deploy to device or emulator", "#FCE4EC", "#C2185B", NF["phone"]),
        ],
        [
            ("deployToBuildsClient", "Copy client distro to builds/client/", "#E8EAF6", "#3949AB", NF["cloud"]),
        ],
    ]
    col_svgs, cols, lane_centers = layout_columns(col_data)
    c0, c1, c2, c3, c4, c5, c6, c7 = cols
    max_y = max(
        c1[-1]["y"] + c1[-1]["h"],
        c2[-1]["y"] + c2[-1]["h"],
        c5[-1]["y"] + c5[-1]["h"],
        c6[-1]["y"] + c6[-1]["h"],
    )

    flows = [
        arrow_between(c0[0], c0[1]),
        arrow_between(c0[1], c1[0], "right", "left"),
        arrow_between(c1[0], c1[1]),
        arrow_between(c1[1], c1[2]),
        arrow_between(c1[2], c1[3]),
        arrow_between(c1[3], c1[4]),
        arrow_between(c1[4], c2[0], "right", "left", via_y=c2[0]["y"] + c2[0]["h"] // 2),
        arrow_between(c2[0], c2[1]),
        arrow_between(c2[1], c2[2]),
        arrow_between(c2[2], c2[3]),
        arrow_between(c2[3], c2[4]),
        arrow_between(c2[4], c3[0], "right", "left"),
        arrow_between(c3[0], c4[0], "right", "left"),
        arrow_between(c4[0], c5[0], "right", "left", via_y=100, lane=0),
        arrow_between(c4[0], c6[0], "right", "left", via_y=120, lane=LANE),
        arrow_between(c5[0], c5[1]),
        arrow_between(c6[0], c6[1]),
        arrow_between(c6[1], c6[2]),
        arrow_between(c6[2], c7[0], "right", "left", via_y=c7[0]["y"] + c7[0]["h"] // 2),
    ]

    note_y = max_y + GAP_V
    legend_y = note_y + 90
    w = c7[0]["x"] + c7[0]["w"] + 80
    h = legend_y + 160

    lane_lines = "\n".join(
        f'  <line x1="{x}" y1="120" x2="{x}" y2="{note_y}" stroke="#CFD8DC" stroke-width="2" stroke-dasharray="6,4"/>'
        for x in [320, 600, 920, 1180, 1400, 1760, 2080]
    )
    lane_labels = f"""
  <text x="{lane_centers[0]}" y="110" text-anchor="middle" class="layer-label">First run</text>
  <text x="{lane_centers[1]}" y="110" text-anchor="middle" class="layer-label">Scaffold client</text>
  <text x="{lane_centers[2]}" y="110" text-anchor="middle" class="layer-label">:core / :cli</text>
  <text x="{lane_centers[3]}" y="110" text-anchor="middle" class="layer-label">Output</text>
  <text x="{(lane_centers[4]+lane_centers[5])//2}" y="110" text-anchor="middle" class="layer-label">Native build</text>
  <text x="{lane_centers[7]}" y="110" text-anchor="middle" class="layer-label">Optional</text>
{lane_lines}"""

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Generation &amp; builds flow</title>
{defs(logo)}
{header(logo, w)}
{lane_labels}
{chr(10).join(col_svgs)}
{chr(10).join(flows)}
  <rect x="680" y="{note_y}" width="380" height="70" class="panel" fill="#FFFDE7" stroke="#F9A825"/>
  <text x="700" y="{note_y + 28}" class="small">Headless: ./gradlew :cli:run --args="generate …"</text>
  <text x="700" y="{note_y + 48}" class="desc">Optional Docker path via misc/docker/</text>
{legend_box(24, legend_y, 520, 120, [
    ("#E8F5E9", "#2E7D32", "Setup — JDK 26 + Git bootstrap"),
    ("#E3F2FD", "#1565C0", "Client — Compose Generate UI"),
    ("#F3E5F5", "#6A1B9A", "Core — template engine + emit"),
    ("#FCE4EC", "#C2185B", "Android — Gradle + Chaquopy path"),
], "Pipeline stages")}
</svg>"""


def desktop_vs_android() -> str:
    logo = "../nexus-logo.png"
    shared_x, shared_y = 400, 88
    row1, r1_boxes, _ = hstack(shared_x + 20, shared_y + 40, [
        (220, 72, "#FFFFFF", "#6A1B9A", NF["file"], "blueprint.json", "Loads blueprint.json at generate time"),
        (240, 72, "#FFFFFF", "#6A1B9A", NF["terminal"], "Lua 5.4 + sol2", "Scripting layer shared across targets"),
    ])
    mvc_y = r1_boxes[0]["y"] + r1_boxes[0]["h"] + GAP_V
    mvc_svg, mvc_boxes, mvc_end = vstack(shared_x + 20, mvc_y, [
        (480, 72, "#FFFFFF", "#6A1B9A", NF["code"], "C++20 MVC", "model · controller · view from graph"),
    ])
    ui_y = mvc_boxes[0]["y"] + mvc_boxes[0]["h"] + GAP_V
    ui_svg, ui_boxes, _ = hstack(shared_x + 20, ui_y, [
        (220, 72, "#FFFFFF", "#6A1B9A", NF["chart"], "ImGui + ImPlot", "Immediate-mode UI and plotting"),
        (240, 72, "#FFFFFF", "#6A1B9A", NF["desktop"], "SDL3 render loop", "Cross-platform graphics and input"),
    ])
    shared_w = max(r1_boxes[-1]["x"] + r1_boxes[-1]["w"], ui_boxes[-1]["x"] + ui_boxes[-1]["w"]) - shared_x + 24
    shared_h = ui_boxes[0]["y"] + ui_boxes[0]["h"] - shared_y + 24

    desk_x, desk_y = 32, shared_y + shared_h + GAP_V
    desk_r1, dr1, _ = hstack(desk_x + 20, desk_y + 40, [
        (240, 72, "#FFFFFF", "#1565C0", NF["gear"], "Zig build (primary)", "Single zig c++ binary compiles all C++ TUs"),
        (260, 72, "#FFFFFF", "#1565C0", NF["python"], "pybind11 embed", "Embeds CPython via pybind11"),
    ])
    desk_r2, dr2, _ = hstack(desk_x + 20, dr1[0]["y"] + dr1[0]["h"] + GAP_V, [
        (240, 72, "#FFFFFF", "#1565C0", NF["file"], "python/functions.py", "In-process NumPy analytics module"),
        (260, 72, "#FFFFFF", "#1565C0", NF["box"], "Native binary", "Win · macOS · Linux executable"),
    ])
    # CMake fallback note
    desk_note_x = dr1[0]["x"]
    desk_note_y = dr2[0]["y"] + dr2[0]["h"] + 12
    desk_note_w = dr2[-1]["x"] + dr2[-1]["w"] - dr1[0]["x"]
    desk_note = f'  <rect x="{desk_note_x}" y="{desk_note_y}" width="{desk_note_w}" height="24" fill="#FFFFFF" stroke="#64748b" stroke-dasharray="4,3" rx="6"/>\n  <text x="{desk_note_x + desk_note_w // 2}" y="{desk_note_y + 17}" text-anchor="middle" class="desc">CMake fallback: legacy-cmake-debug / release presets</text>'
    desk_w = max(dr1[-1]["x"] + dr1[-1]["w"], dr2[-1]["x"] + dr2[-1]["w"]) - desk_x + 24
    desk_h = dr2[0]["y"] + dr2[0]["h"] - desk_y + 24

    and_x = desk_x + desk_w + GAP_H
    and_r1, ar1, _ = hstack(and_x + 20, desk_y + 40, [
        (240, 72, "#FFFFFF", "#C2185B", NF["android"], "Zig JNI build", "zig build --target aarch64-linux-android .so"),
        (260, 72, "#FFFFFF", "#C2185B", NF["desktop"], "SDL3 GLES", "Full-screen touch UI on GLES"),
    ])
    and_r2, ar2, _ = hstack(and_x + 20, ar1[0]["y"] + ar1[0]["h"] + GAP_V, [
        (240, 72, "#FFFFFF", "#C2185B", NF["plug"], "JNI bridge (C++ zig-services)", "Hand-authored jni/ files — no IDL codegen"),
        (260, 72, "#FFFFFF", "#C2185B", NF["python"], "Chaquopy", "app/src/main/python/ embedding"),
    ])
    and_w = max(ar1[-1]["x"] + ar1[-1]["w"], ar2[-1]["x"] + ar2[-1]["w"]) - and_x + 24
    and_h = desk_h

    arrows = [
        arrow_between(r1_boxes[0], mvc_boxes[0], label="wires nodes"),
        arrow_between(r1_boxes[1], mvc_boxes[0], "left", "right"),
        arrow_between(mvc_boxes[0], ui_boxes[0]),
        arrow_between(mvc_boxes[0], ui_boxes[1], "bottom", "top", via_x=ui_boxes[1]["x"] + ui_boxes[1]["w"] // 2),
        arrow_between(ui_boxes[0], dr1[0], "bottom", "top", "desktop", via_x=dr1[0]["x"] + dr1[0]["w"] // 2),
        arrow_between(ui_boxes[1], ar1[0], "bottom", "top", "Android", via_x=ar1[0]["x"] + ar1[0]["w"] // 2),
        arrow_between(dr1[0], dr2[0]),
        arrow_between(dr1[1], dr2[1], "bottom", "top"),
        arrow_between(ar1[0], ar2[0]),
        arrow_between(ar1[1], ar2[1], "bottom", "top"),
    ]

    note_x = shared_x + shared_w + GAP_H
    legend_y = desk_y + desk_h + GAP_V
    w = and_x + and_w + 40
    h = legend_y + 120

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Desktop vs Android runtime</title>
{defs(logo)}
{header(logo, w)}

{layer_box(shared_x, shared_y, shared_w, shared_h, "#F3E5F5", "#6A1B9A", "Shared (both templates)", NF["layer"])}
{row1}
{mvc_svg}
{ui_svg}

{layer_box(desk_x, desk_y, desk_w, desk_h, "#E3F2FD", "#1565C0", "Desktop runtime", NF["desktop"])}
{desk_r1}
{desk_r2}

{layer_box(and_x, desk_y, and_w, and_h, "#FCE4EC", "#C2185B", "Android runtime", NF["phone"])}
{and_r1}
{and_r2}

{chr(10).join(arrows)}

  <rect x="{note_x}" y="{shared_y + 30}" width="300" height="70" class="panel" fill="#FFFDE7" stroke="#F9A825"/>
  <text x="{note_x + 20}" y="{shared_y + 60}" class="small">Same plotter template on both targets.</text>
  <text x="{note_x + 20}" y="{shared_y + 80}" class="desc">blueprint.json declares the same node graph.</text>

  <rect x="{(desk_x + and_x + and_w) // 2 - 220}" y="{legend_y - 56}" width="440" height="50" class="panel" fill="#E8F5E9" stroke="#2E7D32"/>
  <text x="{(desk_x + and_x + and_w) // 2 - 200}" y="{legend_y - 26}" class="small">Zig is the only native build path — CMake fully removed from both templates.</text>

{legend_box(32, legend_y, 500, 80, [
    ("#F3E5F5", "#6A1B9A", "Shared — blueprint + MVC + SDL3"),
    ("#E3F2FD", "#1565C0", "Desktop — Zig build + pybind11"),
    ("#FCE4EC", "#C2185B", "Android — Zig JNI + Chaquopy"),
], "Runtime layers")}
</svg>"""


def langflow_vs_n8n() -> str:
    logo = "../nexus-logo.png"
    y0 = 150
    col_w = 360  # unused — width computed per column

    def column(x: int, title: str, fill: str, stroke: str, icon: str, subtitle: str,
               row1: list, row2: list, bullets: list[str]) -> tuple[str, list, int]:
        r1_svg, r1, _ = hstack(x + 20, y0, row1)
        r2_y = r1[0]["y"] + r1[0]["h"] + GAP_V
        r2_svg, r2, _ = vstack(x + 20 + (max(b["w"] for b in r1) - row2[0][0]) // 2, r2_y, row2)
        all_boxes = r1 + r2
        col_w = max(b["x"] + b["w"] for b in all_boxes) - x + 24
        arrows = [
            arrow_between(r1[0], r1[1], "right", "left"),
            arrow_between(r1[1], r2[0], curve_bend=30),
        ]
        notes = "\n".join(
            f'  <text x="{x + 20}" y="{r2[0]["y"] + r2[0]["h"] + 40 + i * 20}" class="{"desc" if i == 2 else "small"}">{t}</text>'
            for i, t in enumerate(bullets)
        )
        panel = layer_box(x, 88, col_w, 420, fill, stroke, title, icon)
        sub = f'  <text x="{x + col_w // 2}" y="130" text-anchor="middle" class="desc">{subtitle}</text>'
        return f"{panel}\n{sub}\n{r1_svg}\n{r2_svg}\n{chr(10).join(arrows)}\n{notes}", all_boxes, x + col_w

    lf, _, x1 = column(24, "Langflow", "#E8F5E9", "#2E7D32", NF["robot"],
        "ML / LLM flow DAG · Runtime execution",
        [(140, 72, "#FFFFFF", "#2E7D32", NF["comment"], "Prompt", "User text input to the flow"),
         (140, 72, "#FFFFFF", "#1565C0", NF["robot"], "LLM", "Inference node calls model API")],
        [(140, 72, "#FFFFFF", "#EF6C00", NF["code"], "Parser", "Structured output extraction")],
        ["• Typed nodes (model, tool, memory)", "• Runs when user invokes the flow", "Output: inference / chat response"])

    n8, _, x2 = column(x1 + GAP_H, "n8n", "#FCE4EC", "#C2185B", NF["plug"],
        "Workflow automation · Runtime execution",
        [(140, 72, "#FFFFFF", "#C2185B", NF["cloud"], "Webhook", "HTTP trigger starts workflow"),
         (140, 72, "#FFFFFF", "#3949AB", NF["plug"], "HTTP Request", "Calls external REST APIs")],
        [(140, 72, "#FFFFFF", "#00695C", NF["comment"], "Slack", "Posts message to channel")],
        ["• Triggers + integration steps", "• Runs on schedule or webhook", "Output: side effects (API calls)"])

    nx_x = x2 + GAP_H
    r1_svg, r1, _ = hstack(nx_x + 20, y0, [
        (140, 72, "#FFFFFF", "#2E7D32", NF["python"], "python.module", "Analytics hooks in blueprint"),
        (140, 72, "#FFFFFF", "#1565C0", NF["database"], "cpp.model", "Domain state node type"),
    ])
    r2_y = r1[0]["y"] + r1[0]["h"] + GAP_V
    r2_svg, r2, _ = hstack(nx_x + 20, r2_y, [
        (140, 72, "#FFFFFF", "#6A1B9A", NF["gear"], "cpp.controller", "Command routing layer"),
        (140, 72, "#FFFFFF", "#EF6C00", NF["layer"], "ui.page", "ImGui screen definition"),
    ])
    r3_y = r2[0]["y"] + r2[0]["h"] + GAP_V
    r3_svg, r3, _ = vstack(nx_x + 20 + (max(b["w"] for b in r2) - 140) // 2, r3_y, [
        (140, 72, "#FFFFFF", "#00838F", NF["terminal"], "lua.script", "sol2 panel bindings"),
    ])
    nx_boxes = r1 + r2 + r3
    nx_col_w = max(b["x"] + b["w"] for b in nx_boxes) - nx_x + 24
    nx_arrows = [
        arrow_between(r1[0], r2[0], curve_bend=-20),
        arrow_between(r2[0], r3[0], curve_bend=20),
        arrow_between(r1[1], r2[1], curve_bend=20),
    ]
    nx_panel = layer_box(nx_x, 88, nx_col_w, 420, "#F3E5F5", "#6A1B9A", "Nexus blueprint.json", NF["file"])
    nx_sub = f'  <text x="{nx_x + nx_col_w // 2}" y="130" text-anchor="middle" class="badge">Design-time codegen</text>'
    nx_notes = "\n".join(
        f'  <text x="{nx_x + 20}" y="{r3[0]["y"] + r3[0]["h"] + 40 + i * 20}" class="{"desc" if i == 2 else "small"}">{t}</text>'
        for i, t in enumerate([
            "• Langflow-style typed DAG (not n8n triggers)",
            "• Consumed at generate time (:core)",
            "Output: C++/Lua/Python/UI project tree",
        ])
    )
    nx = f"{nx_panel}\n{nx_sub}\n{r1_svg}\n{r2_svg}\n{r3_svg}\n{chr(10).join(nx_arrows)}\n{nx_notes}"

    foot_y = 520
    w = nx_x + nx_col_w + 24
    h = 580
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow, n8n, and Nexus blueprints</title>
{defs(logo)}
{header(logo, w)}

{lf}
{n8}
{nx}

  <rect x="24" y="{foot_y}" width="{w - 48}" height="44" fill="#FFFFFF" stroke="#B0BEC5" stroke-width="2" rx="10"/>
  <text x="{w // 2}" y="{foot_y + 28}" text-anchor="middle" class="small">Nexus maps Langflow mental model (typed nodes + edges) to native app codegen — not runtime webhook automation.</text>
</svg>"""


def langflow_rag_chatbot() -> str:
    logo = "../nexus-logo.png"
    y = 120
    specs = [
        (180, 72, "#E8F5E9", "#2E7D32", NF["comment"], "Chat Input", "User-facing message entry point"),
        (180, 72, "#E3F2FD", "#1565C0", NF["file"], "PDF Loader", "Ingests documents into pipeline"),
        (180, 72, "#F3E5F5", "#7B1FA2", NF["code"], "Text Splitter", "Chunks text for embedding"),
        (180, 72, "#FFF3E0", "#EF6C00", NF["chart"], "Embeddings", "Vectorizes text chunks"),
        (180, 72, "#FCE4EC", "#C2185B", NF["database"], "Vector Store", "Persists vectors for retrieval"),
        (180, 72, "#E0F7FA", "#00838F", NF["search"], "Retriever", "Similarity search at query time"),
        (180, 72, "#E8EAF6", "#3949AB", NF["robot"], "LLM", "Generates answer from context"),
        (180, 72, "#E8F5E9", "#2E7D32", NF["comment"], "Chat Output", "Returns response to user"),
    ]
    body, boxes, end_x = hstack(48, y, specs, gap=56)
    edges = [arrow_between(boxes[i], boxes[i + 1], "right", "left") for i in range(len(boxes) - 1)]
    x1, y1 = _anchor(boxes[0], "bottom")
    x2, y2 = _anchor(boxes[5], "bottom")
    curve_y = y1 + 100
    edges.append(
        arrow_curve_via(
            x1, y1, x2, y2,
            x1, curve_y, x2, curve_y,
            label="user query shortcut at chat time",
            dashed=True,
        )
    )
    legend_y = y + max(b["h"] for b in boxes) + GAP_V + 120
    w = end_x + 48
    h = legend_y + 170
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow: RAG chatbot flow</title>
{defs(logo)}
{header(logo, w)}
{body}
{chr(10).join(edges)}
{legend_box(48, legend_y, w - 96, 120, [
    ("#E8F5E9", "#2E7D32", "I/O — user-facing input and output"),
    ("#E3F2FD", "#1565C0", "Loader — ingest documents"),
    ("#FFF3E0", "#EF6C00", "Model — embeddings and LLM inference"),
    ("#FCE4EC", "#C2185B", "Memory — vector database persistence"),
    ("#E0F7FA", "#00838F", "Retriever — similarity search"),
], "Node categories")}
</svg>"""


def langflow_agent_tools() -> str:
    logo = "../nexus-logo.png"
    y_top = 120
    gap = 64

    top_svg, top_boxes, top_end = hstack(48, y_top, [
        (180, 72, "#E8F5E9", "#2E7D32", NF["comment"], "User Input", "Natural language task from user"),
        (220, 88, "#E8EAF6", "#3949AB", NF["robot"], "Agent", "ReAct / tool-calling LLM orchestrator"),
        (180, 72, "#E8F5E9", "#2E7D32", NF["comment"], "Output", "Synthesized final answer to user"),
    ], gap=gap)

    tool_y = top_boxes[0]["y"] + top_boxes[0]["h"] + GAP_V + 80
    tool_svg, tool_boxes, _ = hstack(120, tool_y, [
        (180, 72, "#FFF3E0", "#EF6C00", NF["gear"], "Calculator", "Arithmetic tool invocation"),
        (180, 72, "#E3F2FD", "#1565C0", NF["search"], "Web Search", "Live web lookup capability"),
        (180, 72, "#F3E5F5", "#7B1FA2", NF["python"], "Python REPL", "Execute code snippets safely"),
    ], gap=gap)

    agent = top_boxes[1]
    arrows = [
        arrow_between(top_boxes[0], agent, "right", "left"),
        arrow_between(agent, top_boxes[2], "right", "left"),
        arrow_between(agent, tool_boxes[0], "bottom", "top", via_x=tool_boxes[0]["x"] + tool_boxes[0]["w"] // 2, lane=-LANE),
        arrow_between(agent, tool_boxes[1], "bottom", "top", via_x=tool_boxes[1]["x"] + tool_boxes[1]["w"] // 2),
        arrow_between(agent, tool_boxes[2], "bottom", "top", via_x=tool_boxes[2]["x"] + tool_boxes[2]["w"] // 2, lane=LANE),
        arrow_between(tool_boxes[0], agent, "top", "bottom", dashed=True, curve_bend=-50, lane=-LANE * 2),
        arrow_between(tool_boxes[1], agent, "top", "bottom", dashed=True, curve_bend=0),
        arrow_between(tool_boxes[2], agent, "top", "bottom", dashed=True, curve_bend=50, lane=LANE * 2),
    ]
    result_label = f'  <text x="{agent["x"] + agent["w"] // 2}" y="{tool_y - 24}" text-anchor="middle" class="desc">tool result (dashed)</text>'

    legend_y = tool_boxes[0]["y"] + tool_boxes[0]["h"] + GAP_V
    w = top_end + 48
    h = legend_y + 120

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow: agent with tools</title>
{defs(logo)}
{header(logo, w)}

{top_svg}
{tool_svg}

{chr(10).join(arrows)}
{result_label}

{legend_box(48, legend_y, w - 96, 90, [
    ("#E8F5E9", "#2E7D32", "I/O — message in, answer out"),
    ("#E8EAF6", "#3949AB", "Agent — orchestrates tool calls"),
    ("#FFF3E0", "#EF6C00", "Tool — callable capability"),
], "Legend — solid = request, dashed = tool result")}
</svg>"""


def nexus_blueprint_app_structure() -> str:
    logo = "../nexus-logo.png"
    panel_x, panel_y = 48, 110
    gap = 56

    row1_svg, row1, _ = hstack(panel_x + 32, panel_y + 56, [
        (180, 80, "#E8F5E9", "#2E7D32", NF["layer"], "ui.page", "ImGui screens via TS/XHTML DSL", True),
        (200, 80, "#E3F2FD", "#1565C0", NF["gear"], "cpp.controller", "Routes commands and data ports", True),
        (180, 80, "#FFF3E0", "#EF6C00", NF["database"], "cpp.model", "Domain state and business logic", True),
    ], gap=gap)

    py_x = row1[-1]["x"] + row1[-1]["w"] + gap
    py_svg, py_box_list, _ = vstack(py_x, panel_y + 56, [
        (200, 80, "#F3E5F5", "#7B1FA2", NF["python"], "python.module", "ML samples and glue code", True),
        (200, 80, "#E0F7FA", "#00838F", NF["terminal"], "lua.script", "sol2 panel bindings", True),
    ], gap=gap)

    panel_w = py_x + py_box_list[0]["w"] - panel_x + 32
    panel_h = py_box_list[-1]["y"] + py_box_list[-1]["h"] - panel_y + 32

    arrows = [
        arrow_between(row1[0], row1[1], "right", "left", label="events"),
        arrow_between(row1[1], row1[2], "right", "left", label="commands"),
        arrow_between(row1[2], py_box_list[0], "right", "left", label="data", lane=-LANE),
        arrow_between(row1[2], py_box_list[1], "right", "left", curve_bend=40, lane=LANE),
    ]

    note_y = panel_y + panel_h + GAP_V
    gen_y = note_y + 56
    gen_bw, gen_bh = _measure("builds/framework/&lt;name&gt;/", "Compiled SDL3 desktop or Android APK", 400, 72, True)
    gen_x = panel_x + (panel_w - gen_bw) // 2
    gen_svg = module(
        gen_x, gen_y, 400, 72, "#E8EAF6", "#3949AB", NF["box"],
        "builds/framework/&lt;name&gt;/", "Compiled SDL3 desktop or Android APK", mono=True,
    )
    gen_box = {"x": gen_x, "y": gen_y, "w": gen_bw, "h": gen_bh}
    mid_x = panel_x + panel_w // 2
    codegen_arrow = (
        f'  <line x1="{mid_x}" y1="{note_y}" x2="{mid_x}" y2="{gen_y}" '
        f'stroke="{ARROW_COLOR_ACCENT}" stroke-width="{ARROW_STROKE_ACCENT}" marker-end="url(#arrow-blue)"/>'
        f'\n  <text x="{mid_x}" y="{note_y + 28}" text-anchor="middle" class="badge">:core ProjectGenerator</text>'
    )
    note_box = f"""  <rect x="{panel_x + 32}" y="{note_y - 40}" width="{panel_w - 64}" height="40" fill="#FFFFFF" stroke="#64748b" stroke-width="2" stroke-dasharray="6,4" rx="8"/>
  <text x="{panel_x + panel_w // 2}" y="{note_y - 16}" text-anchor="middle" class="desc">Not shown: external n8n webhooks (ops glue at app edge)</text>"""

    legend_y = gen_y + gen_bh + GAP_V
    w = panel_x + panel_w + 48
    h = legend_y + 100

    badge = f"""  <rect x="{panel_x + (panel_w - 420) // 2}" y="72" width="420" height="28" fill="#E3F2FD" stroke="#1565C0" stroke-width="2" rx="14"/>
  <text x="{panel_x + panel_w // 2}" y="91" text-anchor="middle" class="badge">DESIGN-TIME — consumed at generation, not runtime webhooks</text>"""

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Nexus blueprint app structure</title>
{defs(logo)}
{header(logo, w)}

{badge}

{layer_box(panel_x, panel_y, panel_w, panel_h, "#FAFAFA", "#B0BEC5", "blueprint.json graph (author in :app Blueprint Editor)", NF["branch"])}
{row1_svg}
{py_svg}

{chr(10).join(arrows)}
{note_box}
{codegen_arrow}
{gen_svg}

{legend_box(panel_x, legend_y, panel_w, 70, [
    ("#E8F5E9", "#2E7D32", "ui.page — screens and DSL layout"),
    ("#E3F2FD", "#1565C0", "cpp.controller — MVC command layer"),
    ("#FFF3E0", "#EF6C00", "cpp.model — domain state"),
    ("#F3E5F5", "#7B1FA2", "python.module — pybind11 / Chaquopy"),
    ("#E0F7FA", "#00838F", "lua.script — sol2 panels"),
], "Nexus node types (blueprint.json)")}
</svg>"""


def python_desktop_vs_android_flow() -> str:
    logo = "../nexus-logo.png"
    gap = 64
    col_gap = GAP_H + 24

    bp_x = 48
    bp_y = 72
    bp_bw, bp_bh = _measure("blueprint.json", "python.module port evaluate", 420, 72, True)
    bp_svg = module(
        bp_x + 200, bp_y, 420, 72, "#FFF8E1", "#F57F17", NF["file"],
        "blueprint.json", "python.module port evaluate", mono=True,
    )
    bp_box = {"x": bp_x + 200, "y": bp_y, "w": bp_bw, "h": bp_bh}

    desk_x, desk_y = 48, bp_y + bp_bh + GAP_V
    desk_r1, dr1, _ = hstack(desk_x + 32, desk_y + 48, [
        (260, 72, "#FFFFFF", "#1565C0", NF["python"], "python/functions.py", "NumPy curve sampling source"),
        (240, 72, "#FFFFFF", "#1565C0", NF["gear"], "zig build: pack python dat", "PYAC archive defined in build.zig"),
    ], gap=gap)
    desk_r2, dr2, _ = hstack(desk_x + 32, dr1[0]["y"] + dr1[0]["h"] + GAP_V, [
        (260, 72, "#FFFFFF", "#1565C0", NF["box"], "misc/python.dat (PYAC)", "Encrypted script pack in build step"),
        (240, 72, "#FFFFFF", "#1565C0", NF["python"], "PythonEngine", "pybind11 embed in controller/"),
    ], gap=gap)
    desk_w = max(dr1[-1]["x"] + dr1[-1]["w"], dr2[-1]["x"] + dr2[-1]["w"]) - desk_x + 32
    desk_h = dr2[0]["y"] + dr2[0]["h"] - desk_y + 32

    and_x = desk_x + desk_w + col_gap
    and_y = desk_y
    and_r1, ar1, _ = hstack(and_x + 32, and_y + 48, [
        (300, 72, "#FFFFFF", "#C2185B", NF["python"], "app/src/main/python/", "functions.py in APK tree"),
        (200, 72, "#FFFFFF", "#C2185B", NF["android"], "Gradle + Chaquopy", "No python.dat — sources in APK"),
    ], gap=gap)
    and_r2, ar2, _ = hstack(and_x + 32, ar1[0]["y"] + ar1[0]["h"] + GAP_V, [
        (520, 80, "#FFFFFF", "#C2185B", NF["plug"], "ChaquopyPythonBridge (hand-authored C++)", "JNI methods call PythonBridge.kt via zig c++"),
    ], gap=gap)
    and_w = max(ar1[-1]["x"] + ar1[-1]["w"], ar2[-1]["x"] + ar2[-1]["w"]) - and_x + 32
    and_h = desk_h

    shared_y = desk_y + desk_h + GAP_V
    shared_x = desk_x
    shared_w = and_x + and_w - desk_x
    shared_svg, shared_boxes, _ = hstack(shared_x + 32, shared_y + 48, [
        (240, 72, "#FFFFFF", "#6A1B9A", NF["gear"], "PlotController", "Routes evaluate to model cache"),
        (240, 72, "#FFFFFF", "#6A1B9A", NF["database"], "FunctionRegistry", "Active curves and samples"),
        (260, 72, "#FFFFFF", "#6A1B9A", NF["chart"], "ImPlot draw", "Renders curves each frame"),
    ], gap=gap)
    shared_h = shared_boxes[0]["h"] + 80

    arrows = [
        arrow_between(bp_box, dr1[0], "bottom", "top", "Desktop", via_x=dr1[0]["x"] + dr1[0]["w"] // 2),
        arrow_between(bp_box, ar1[0], "bottom", "top", "Android", via_x=ar1[0]["x"] + ar1[0]["w"] // 2),
        arrow_between(dr1[0], dr2[0]),
        arrow_between(dr1[1], dr2[1], "bottom", "top"),
        arrow_between(dr2[0], shared_boxes[0], "bottom", "top", via_x=shared_boxes[0]["x"] + shared_boxes[0]["w"] // 2),
        arrow_between(dr2[1], shared_boxes[0], "bottom", "top", via_x=shared_boxes[0]["x"] + shared_boxes[0]["w"] // 2, lane=LANE),
        arrow_between(ar1[0], ar2[0]),
        arrow_between(ar2[0], shared_boxes[2], "bottom", "top", via_x=shared_boxes[2]["x"] + shared_boxes[2]["w"] // 2),
        arrow_between(shared_boxes[0], shared_boxes[1], "right", "left"),
        arrow_between(shared_boxes[1], shared_boxes[2], "right", "left"),
    ]

    legend_y = shared_y + shared_h + GAP_V
    w = and_x + and_w + 48
    h = legend_y + 100

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Python desktop vs Android embedding flow</title>
{defs(logo)}
{header(logo, w)}

{bp_svg}

{layer_box(desk_x, desk_y, desk_w, desk_h, "#E3F2FD", "#1565C0", "Desktop path", NF["desktop"])}
{desk_r1}
{desk_r2}

{layer_box(and_x, and_y, and_w, and_h, "#FCE4EC", "#C2185B", "Android path", NF["phone"])}
{and_r1}
{and_r2}

{layer_box(shared_x, shared_y, shared_w, shared_h, "#F3E5F5", "#6A1B9A", "Shared MVC output (both templates)", NF["chart"])}
{shared_svg}

{chr(10).join(arrows)}

{legend_box(48, legend_y, 520, 70, [
    ("#E3F2FD", "#1565C0", "Desktop — pybind11 + python.dat"),
    ("#FCE4EC", "#C2185B", "Android — Chaquopy + hand-authored JNI"),
    ("#F3E5F5", "#6A1B9A", "Shared — controller → ImPlot"),
], "Python embed layers")}
</svg>"""


def tsxhtml_lowering_pipeline() -> str:
    logo = "../nexus-logo.png"
    src_svg, src_boxes, src_end = vstack(40, 120, [
        (220, 72, "#E8F5E9", "#2E7D32", NF["file"], "ui/ui.xhtml", "Declarative markup — panels, plots, sliders"),
        (220, 72, "#E8F5E9", "#2E7D32", NF["code"], "ui/ui.ts", "state(), native(), invoke() bindings"),
    ])
    dsl_x = src_boxes[0]["x"] + max(b["w"] for b in src_boxes) + GAP_H
    mid_y = (src_boxes[0]["y"] + src_boxes[-1]["y"] + src_boxes[-1]["h"]) // 2 - 36
    dsl_bw, dsl_bh = _measure("shared/dsl/", "tags.ts · components.ts · core.ts", 220, 72, True)
    dsl_svg = module(dsl_x, mid_y, 220, 72, "#E3F2FD", "#1565C0", NF["layer"], "shared/dsl/", "tags.ts · components.ts · core.ts")
    dsl_box = {"x": dsl_x, "y": mid_y, "w": dsl_bw, "h": dsl_bh}
    pipe_svg, pipe_boxes, pipe_end = hstack(dsl_x + dsl_bw + GAP_H, mid_y, [
        (220, 72, "#FFF3E0", "#EF6C00", NF["gear"], "Lowering pass", "Maps ComponentTag → draw calls"),
        (220, 72, "#F3E5F5", "#7B1FA2", NF["terminal"], "panels.lua equiv.", "nxs.register_panel definitions"),
        (220, 72, "#E0F7FA", "#00838F", NF["terminal"], "sol2 runtime", "LuaPanels walks tree each frame"),
    ])
    out_x = pipe_boxes[-1]["x"] + pipe_boxes[-1]["w"] + GAP_H
    out_svg, out_boxes, _ = vstack(out_x, 120, [
        (200, 72, "#ECEFF1", "#455A64", NF["desktop"], "Dear ImGui", "window, button, slider, …"),
        (200, 72, "#ECEFF1", "#455A64", NF["chart"], "ImPlot / imnodes", "plot-line, node-editor tags"),
    ])
    edges = [
        arrow_between(src_boxes[0], dsl_box, "right", "left"),
        arrow_between(src_boxes[1], dsl_box, "right", "left"),
        arrow_between(dsl_box, pipe_boxes[0], "right", "left"),
        arrow_between(pipe_boxes[0], pipe_boxes[1], "right", "left"),
        arrow_between(pipe_boxes[1], pipe_boxes[2], "right", "left"),
        arrow_between(pipe_boxes[2], out_boxes[0], "right", "left"),
        arrow_between(pipe_boxes[2], out_boxes[1], "right", "left", curve_bend=30),
    ]
    note_y = max(src_end, mid_y + dsl_bh, out_boxes[-1]["y"] + out_boxes[-1]["h"]) + GAP_V
    legend_y = note_y + 60
    w = out_boxes[-1]["x"] + out_boxes[-1]["w"] + 40
    h = legend_y + 120
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>TS/XHTML lowering pipeline</title>
{defs(logo)}
{header(logo, w)}
{src_svg}
{dsl_svg}
{pipe_svg}
{out_svg}
{chr(10).join(edges)}
  <rect x="40" y="{note_y}" width="{w - 80}" height="44" fill="#FFFFFF" stroke="#B0BEC5" stroke-width="2" rx="10"/>
  <text x="{w // 2}" y="{note_y + 28}" text-anchor="middle" class="small">No browser engine — same nxs.* commands as hand-written panels.lua; hot-reload via lua.dat optional</text>
{legend_box(40, legend_y, w - 80, 80, [
    ("#E8F5E9", "#2E7D32", "Authoring — ui.xhtml + ui.ts"),
    ("#E3F2FD", "#1565C0", "DSL — shared tag registry"),
    ("#F3E5F5", "#7B1FA2", "Lua — register_panel output"),
    ("#ECEFF1", "#455A64", "Native — ImGui / ImPlot / imnodes"),
], "Lowering stages")}
</svg>"""


def blueprint_vs_flows_layers() -> str:
    logo = "../nexus-logo.png"
    gap = 56
    col_gap = GAP_H + 32

    left_x, left_y = 48, 88
    left_r1, lr1, _ = hstack(left_x + 32, left_y + 72, [
        (220, 72, "#FFFFFF", "#F57F17", NF["python"], "python.module", "Analytics and glue modules"),
        (220, 72, "#FFFFFF", "#F57F17", NF["database"], "cpp.model", "Domain state and caches"),
    ], gap=gap)
    left_r2, lr2, _ = hstack(left_x + 32, lr1[0]["y"] + lr1[0]["h"] + GAP_V, [
        (220, 72, "#FFFFFF", "#F57F17", NF["gear"], "cpp.controller", "Command routing layer"),
        (220, 72, "#FFFFFF", "#F57F17", NF["layer"], "ui.page", "ImGui screens and DSL layout"),
    ], gap=gap)
    gen_y = lr2[0]["y"] + lr2[0]["h"] + GAP_V
    gen_svg, gen_boxes, _ = vstack(left_x + 32 + (max(b["w"] for b in lr2) - 280) // 2, gen_y, [
        (280, 72, "#FFFFFF", "#6A1B9A", NF["rocket"], ":core ProjectGenerator", "Validates graph · emits src/ tree"),
    ])
    left_w = max(
        lr1[-1]["x"] + lr1[-1]["w"],
        lr2[-1]["x"] + lr2[-1]["w"],
        gen_boxes[0]["x"] + gen_boxes[0]["w"],
    ) - left_x + 32
    left_h = gen_boxes[0]["y"] + gen_boxes[0]["h"] - left_y + 32

    right_x = left_x + left_w + col_gap
    right_y = left_y
    right_r1, rr1, _ = hstack(right_x + 32, right_y + 72, [
        (220, 72, "#FFFFFF", "#2E7D32", NF["cog"], "background flows", "interval loops while app is alive"),
        (240, 72, "#FFFFFF", "#2E7D32", NF["comment"], "triggered flows", "event · startup · manual · hotkey"),
    ], gap=gap)
    right_r2, rr2, _ = hstack(right_x + 32, rr1[0]["y"] + rr1[0]["h"] + GAP_V, [
        (480, 72, "#FFFFFF", "#2E7D32", NF["terminal"], "steps[] invoke", "nxs.* · python.* · lua.* targets"),
    ], gap=gap)
    runner_y = rr2[0]["y"] + rr2[0]["h"] + GAP_V
    runner_svg, runner_boxes, _ = vstack(right_x + 32 + (max(b["w"] for b in rr2) - 280) // 2, runner_y, [
        (280, 72, "#FFFFFF", "#00838F", NF["gear"], "FlowRunner", "Registers triggers from flows.json"),
    ])
    right_w = max(
        rr1[-1]["x"] + rr1[-1]["w"],
        rr2[-1]["x"] + rr2[-1]["w"],
        runner_boxes[0]["x"] + runner_boxes[0]["w"],
    ) - right_x + 32
    right_h = runner_boxes[0]["y"] + runner_boxes[0]["h"] - right_y + 32

    left_arrows = [
        arrow_between(lr1[0], lr1[1], "right", "left"),
        arrow_between(lr1[0], lr2[0], "bottom", "top", via_x=lr2[0]["x"] + lr2[0]["w"] // 2, lane=-LANE),
        arrow_between(lr1[1], lr2[1], "bottom", "top", via_x=lr2[1]["x"] + lr2[1]["w"] // 2, lane=LANE),
        arrow_between(lr2[0], gen_boxes[0], "bottom", "top", accent=True),
    ]
    right_arrows = [
        arrow_between(rr1[0], rr1[1], "right", "left"),
        arrow_between(rr1[0], rr2[0], "bottom", "top", via_x=rr2[0]["x"] + rr2[0]["w"] // 2, lane=-LANE),
        arrow_between(rr1[1], rr2[0], "bottom", "top", via_x=rr2[0]["x"] + rr2[0]["w"] // 2, lane=LANE),
        arrow_between(rr2[0], runner_boxes[0], "bottom", "top"),
    ]

    foot_y = max(left_y + left_h, right_y + right_h) + GAP_V
    legend_y = foot_y + 56
    w = right_x + right_w + 48
    h = legend_y + 110

    left_sub = f'  <text x="{left_x + left_w // 2}" y="{left_y + 42}" text-anchor="middle" class="desc">Langflow-style MVC wiring · consumed at generation</text>'
    right_sub = f'  <text x="{right_x + right_w // 2}" y="{right_y + 42}" text-anchor="middle" class="desc">Optional in-app services · loaded at app startup</text>'
    foot = f"""  <rect x="48" y="{foot_y}" width="{w - 96}" height="44" fill="#FFFFFF" stroke="#B0BEC5" stroke-width="2" rx="10"/>
  <text x="{w // 2}" y="{foot_y + 28}" text-anchor="middle" class="small">Same Langflow canvas may split: structure → blueprint.json · automation → flows.json</text>"""

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>blueprint.json vs flows.json layers</title>
{defs(logo)}
{header(logo, w)}

{layer_box(left_x, left_y, left_w, left_h, "#FFF8E1", "#F57F17", "Design-time — blueprint.json", NF["file"])}
{left_sub}
{left_r1}
{left_r2}
{gen_svg}

{layer_box(right_x, right_y, right_w, right_h, "#E8F5E9", "#2E7D32", "Runtime — flows/flows.json", NF["branch"])}
{right_sub}
{right_r1}
{right_r2}
{runner_svg}

{chr(10).join(left_arrows)}
{chr(10).join(right_arrows)}
{foot}

{legend_box(48, legend_y, w - 96, 90, [
    ("#FFF8E1", "#F57F17", "blueprint.json — design-time codegen"),
    ("#E8F5E9", "#2E7D32", "flows.json — runtime automation"),
    ("#6A1B9A", "#6A1B9A", "ProjectGenerator — one-time emit"),
    ("#00838F", "#00838F", "FlowRunner — in-process triggers"),
], "Two-layer model")}
</svg>"""


def langflow_adoption_workflow() -> str:
    logo = "../nexus-logo.png"
    y = 120
    specs = [
        (210, 72, "#E8F5E9", "#2E7D32", NF["robot"], "Langflow canvas", "Design DAG in external tool"),
        (210, 72, "#E3F2FD", "#1565C0", NF["file"], "Export JSON", "API or Export flow button"),
        (230, 88, "#E8EAF6", "#3949AB", NF["gear"], "LangflowTransformationEngine", "Automatic import · all flows enabled: false (v0.3.0)"),
        (210, 72, "#F3E5F5", "#7B1FA2", NF["box"], "flows/flows.json", "Place in generated project"),
        (210, 72, "#E0F7FA", "#00838F", NF["cog"], "nxs_config.json", "flows.enabled = true"),
        (210, 72, "#E8EAF6", "#3949AB", NF["rocket"], "FlowRunner", "Startup triggers registered"),
    ]
    body, boxes, end_x = hstack(40, y, specs)
    edges = [arrow_between(boxes[i], boxes[i + 1], "right", "left") for i in range(len(boxes) - 1)]
    note_y = y + max(b["h"] for b in boxes) + GAP_V
    legend_y = note_y + 70
    w = end_x + 40
    h = legend_y + 130
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow adoption workflow</title>
{defs(logo)}
{header(logo, w)}
{body}
{chr(10).join(edges)}
  <rect x="40" y="{note_y}" width="{w - 80}" height="50" fill="#FFFDE7" stroke="#F9A825" stroke-width="2" rx="10"/>
  <text x="{w // 2}" y="{note_y + 22}" text-anchor="middle" class="small">App structure nodes → blueprint.json instead · LLM components become invoke stubs in python.module</text>
  <text x="{w // 2}" y="{note_y + 40}" text-anchor="middle" class="desc">LangflowTransformationEngine alive in v0.3.0 — all imported flows default to enabled: false</text>
{legend_box(40, legend_y, w - 80, 90, [
    ("#E8F5E9", "#2E7D32", "External — Langflow authoring"),
    ("#E8EAF6", "#3949AB", "Transform — LangflowTransformationEngine (v0.3.0)"),
    ("#F3E5F5", "#7B1FA2", "Ship — flows.json in project"),
    ("#3949AB", "#3949AB", "Runtime — FlowRunner at startup"),
], "Adoption path")}
</svg>"""


def zig_orchestration_layer() -> str:
    logo = "../nexus-logo.png"
    gap = GAP_V

    # Layer 1: Client-setup / Bootstrap
    setup_x, setup_y = 40, 120
    setup_svg, setup_boxes, setup_end = hstack(setup_x, setup_y, [
        (220, 72, "#E8F5E9", "#2E7D32", NF["wrench"], "setup.zig", "Entry point — orchestrates bootstrap"),
        (220, 72, "#E8F5E9", "#2E7D32", NF["terminal"], "bootstrap.zig", "Downloads + extracts Zig 0.14.0 tarball"),
        (220, 72, "#E8F5E9", "#2E7D32", NF["file"], "env.sh / env.bat", "ZIG_HOME, PATH prepend exports"),
    ], gap=GAP_H)
    setup_w = setup_end - setup_x + 24
    setup_h = setup_boxes[0]["h"] + 48

    # Layer 2: zig-services build
    svc_x, svc_y = setup_x, setup_y + setup_h + gap
    svc_svg, svc_boxes, svc_end = hstack(svc_x, svc_y + 40, [
        (220, 72, "#E3F2FD", "#1565C0", NF["file"], "build.zig", "Build graph — C++ TUs, C-ABI, .cppm"),
        (240, 72, "#E3F2FD", "#1565C0", NF["package"], "build.zig.zon", "Pinned deps — SDL3, ImGui, sol2"),
        (220, 72, "#E3F2FD", "#1565C0", NF["gear"], "zig c++ compile", "Single compiler for all target triples"),
    ], gap=GAP_H)
    svc_w = svc_end - svc_x + 24
    svc_h = svc_boxes[0]["h"] + 60

    # Layer 3: Output
    out_x, out_y = setup_x, svc_y + svc_h + gap
    out_svg, out_boxes, out_end = hstack(out_x, out_y + 40, [
        (220, 72, "#F3E5F5", "#7B1FA2", NF["box"], "zig-out/bin/", "Desktop SDL3 app binary"),
        (220, 72, "#F3E5F5", "#7B1FA2", NF["chart"], "lua.dat / python.dat", "Packed script archives staged"),
    ], gap=GAP_H)
    out_w = out_end - out_x + 24
    out_h = out_boxes[0]["h"] + 60

    # Layer 4: Optional — Android Zig JNI
    and_x, and_y = setup_x, out_y + out_h + gap
    and_svg, and_boxes, and_end = hstack(and_x, and_y + 40, [
        (240, 72, "#FCE4EC", "#C2185B", NF["android"], "Android Zig JNI", "aarch64-linux-android .so target"),
        (240, 72, "#FCE4EC", "#C2185B", NF["plug"], "C++ JNI bridge", "Hand-authored jni/ files"),
    ], gap=GAP_H)
    and_w = and_end - and_x + 24
    and_h = and_boxes[0]["h"] + 60

    arrows = [
        arrow_between(setup_boxes[0], setup_boxes[1], "right", "left"),
        arrow_between(setup_boxes[1], setup_boxes[2], "right", "left"),
        arrow_between(setup_boxes[2], svc_boxes[0], "bottom", "top", "provides zig 0.14.0", via_x=svc_boxes[0]["x"] + svc_boxes[0]["w"] // 2, dashed=True),
        arrow_between(svc_boxes[0], svc_boxes[1], "right", "left"),
        arrow_between(svc_boxes[1], svc_boxes[2], "right", "left"),
        arrow_between(svc_boxes[2], out_boxes[0], "bottom", "top", "links binary", via_x=out_boxes[0]["x"] + out_boxes[0]["w"] // 2),
        arrow_between(svc_boxes[1], out_boxes[1], "bottom", "top", "packs archives", via_x=out_boxes[1]["x"] + out_boxes[1]["w"] // 2),
        arrow_between(svc_boxes[2], and_boxes[0], "bottom", "top", "cross-compile .so", via_x=and_boxes[0]["x"] + and_boxes[0]["w"] // 2, dashed=True, lane=-LANE),
    ]

    legend_y = and_y + and_h + GAP_V
    w = svc_end + 40
    h = legend_y + 120

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Zig orchestration layer</title>
{defs(logo)}
{header(logo, w)}

{layer_box(setup_x, setup_y, setup_w, setup_h, "#E8F5E9", "#2E7D32", "1. Services Layer — client-setup (Zig bootstrap)", NF["wrench"])}
{setup_svg}

{layer_box(svc_x, svc_y, svc_w, svc_h, "#E3F2FD", "#1565C0", "2. Build Layer — zig-services (C++ compilation)", NF["gear"])}
{svc_svg}

{layer_box(out_x, out_y, out_w, out_h, "#F3E5F5", "#7B1FA2", "3. Desktop output — native binary + archives", NF["box"])}
{out_svg}

{layer_box(and_x, and_y, and_w, and_h, "#FCE4EC", "#C2185B", "4. Android output (Phase 4) — Zig JNI .so", NF["phone"])}
{and_svg}

{chr(10).join(arrows)}

{legend_box(40, legend_y, 520, 90, [
    ("#E8F5E9", "#2E7D32", "Bootstrap — setup.zig → env.sh"),
    ("#E3F2FD", "#1565C0", "Build — zig c++ compile"),
    ("#F3E5F5", "#7B1FA2", "Output — desktop native binary"),
    ("#FCE4EC", "#C2185B", "Android — Zig JNI (Phase 4)"),
], "Orchestration layers — solid = done, dashed = Phase 4")}
</svg>"""


def cmake_to_zig_migration() -> str:
    logo = "../nexus-logo.png"
    box_w, box_h = 220, 100
    gap = 40

    phases = [
        ("Phase 0", "Zig install", "setup.zig + env.sh", "#E8F5E9", "#2E7D32", NF["wrench"]),
        ("Phase 1", "zig-services", "C++ TUs via zig c++", "#E3F2FD", "#1565C0", NF["gear"]),
        ("Phase 2", "Langflow imp.", "Kotlin service (parallel)", "#FFF3E0", "#EF6C00", NF["branch"]),
        ("Phase 3", "Desktop Zig default", "Zig primary + CMake fallback", "#F3E5F5", "#7B1FA2", NF["desktop"]),
        ("Phase 4", "Android Zig JNI", "Retire Djinni — C++ jni/ files", "#FCE4EC", "#C2185B", NF["phone"]),
        ("Phase 5", "ArenaAllocator", "Opt-in C-ABI hotspots", "#E0F7FA", "#00838F", NF["database"]),
    ]
    done_indicator = ["✅", "✅", "✅", "✅", "✅", "✅"]

    x, y = 60, 180
    parts = []
    boxes = []
    cx = x
    for i, (phase, label, desc, fill, stroke, icon) in enumerate(phases):
        phase_box = module(cx, y, box_w, box_h, fill, stroke, icon, f"{phase}: {label}", desc)
        status_y = y + box_h + 16
        status = f'  <text x="{cx + box_w // 2}" y="{status_y}" text-anchor="middle" class="badge">{done_indicator[i]}</text>'
        parts.append(f"{phase_box}\n{status}")
        boxes.append({"x": cx, "y": y, "w": box_w, "h": box_h})
        cx += box_w + gap

    # Timeline arrow
    timeline_y = y + box_h + 56
    timeline_arrow = f'  <line x1="{x - 20}" y1="{timeline_y}" x2="{cx - gap + 20}" y2="{timeline_y}" stroke="{ARROW_COLOR}" stroke-width="3" marker-end="url(#arrow)"/>'
    left_label = f'  <text x="{x + box_w // 2}" y="{timeline_y - 10}" text-anchor="middle" class="small">CMake dominant</text>'
    right_label = f'  <text x="{cx - gap - box_w // 2}" y="{timeline_y - 10}" text-anchor="middle" class="small">Zig dominant</text>'
    mid_label = f'  <text x="{(boxes[1]["x"] + boxes[3]["x"] + boxes[3]["w"]) // 2}" y="{timeline_y - 10}" text-anchor="middle" class="small">Phased migration</text>'

    # Legend
    legend_y = timeline_y + 60
    w = cx + 40
    h = legend_y + 120

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>CMake to Zig migration timeline</title>
{defs(logo)}
{header(logo, w)}

  <rect x="{x - 20}" y="{y - 20}" width="{cx - x - gap + 40}" height="36" fill="#FFFFFF" stroke="#B0BEC5" stroke-width="2" rx="8"/>
  <text x="{x + (cx - x - gap) // 2}" y="{y + 4}" text-anchor="middle" class="layer-label">CMake → Zig phased migration</text>

{chr(10).join(parts)}
{timeline_arrow}
{left_label}
{mid_label}
{right_label}

{legend_box(x, legend_y, 520, 70, [
    ("#E8F5E9", "#2E7D32", "✅ Done — Phase 0 (Zig install)"),
    ("#E3F2FD", "#1565C0", "✅ Done — Phases 1, 3 (zig-services, desktop)"),
    ("#FCE4EC", "#C2185B", "Phase 4 (Android Zig JNI)"),
    ("#E0F7FA", "#00838F", "Phase 5 (ArenaAllocator)"),
], "Phase status")}
</svg>"""


def langflow_import_pipeline() -> str:
    logo = "../nexus-logo.png"

    # Input: Langflow JSON
    inp_x, inp_y = 40, 180
    inp_svg, inp_boxes, inp_end = hstack(inp_x, inp_y, [
        (220, 72, "#E8F5E9", "#2E7D32", NF["robot"], "Langflow JSON", "Export from Langflow API or UI"),
        (220, 72, "#E3F2FD", "#1565C0", NF["file"], "JSON parser", "Deserialize ReactFlow nodes/edges"),
    ], gap=GAP_H)

    # Engine: LangflowTransformationEngine
    eng_x = inp_end + 20
    eng_y = inp_y
    eng_w = 520
    eng_inner_y = eng_y + 40
    eng_r1, er1, _ = hstack(eng_x + 20, eng_inner_y, [
        (220, 72, "#FFF3E0", "#EF6C00", NF["gear"], "Trigger inference", "ChatInput→manual, Webhook→event, Schedule→interval"),
        (220, 72, "#FFF3E0", "#EF6C00", NF["branch"], "Topological sort", "Resolve edge order → ordered steps[]"),
    ], gap=30)
    eng_r2, er2, _ = hstack(eng_x + 20, er1[0]["y"] + er1[0]["h"] + GAP_V, [
        (220, 72, "#FFF3E0", "#EF6C00", NF["code"], "Dedup + validate", "Remove duplicates · validate step types"),
        (220, 72, "#FFF3E0", "#EF6C00", NF["database"], "Emit flows.json", "All imported flows enabled: false"),
    ], gap=30)
    eng_h = er2[0]["y"] + er2[0]["h"] - eng_y + 24

    # Output: flows.json + nxs_config.json
    out_x = eng_x + eng_w + GAP_H
    out_y = inp_y
    out_svg, out_boxes, out_end = vstack(out_x, out_y, [
        (240, 72, "#F3E5F5", "#7B1FA2", NF["file"], "flows/flows.json", "Automation definitions with enabled: false"),
        (240, 72, "#F3E5F5", "#7B1FA2", NF["gear"], "nxs_config.json", "flows.enabled = true at user's choice"),
    ], gap=GAP_V)
    out_w = out_end - out_x + 24
    out_h = out_boxes[-1]["y"] + out_boxes[-1]["h"] - out_y + 24

    # FlowRunner
    runner_x = out_x + (out_w - 240) // 2
    runner_y = out_y + max(out_boxes[-1]["y"] + out_boxes[-1]["h"] - out_y, 260) + GAP_V
    runner_box = {"x": runner_x, "y": runner_y, "w": 240, "h": 72}
    runner_svg = module(runner_x, runner_y, 240, 72, "#E0F7FA", "#00838F", NF["cog"], "FlowRunner", "Registers triggers at startup")

    arrows = [
        arrow_between(inp_boxes[0], inp_boxes[1], "right", "left"),
        arrow_between(inp_boxes[1], er1[0], "right", "left", "parse", via_x=eng_x + 20),
        arrow_between(er1[0], er1[1], "right", "left"),
        arrow_between(er1[1], er2[0], "bottom", "top", via_x=er2[0]["x"] + er2[0]["w"] // 2, lane=-LANE),
        arrow_between(er1[0], er2[1], "bottom", "top", via_x=er2[1]["x"] + er2[1]["w"] // 2, lane=LANE),
        arrow_between(er2[0], er2[1], "right", "left"),
        arrow_between(er2[1], out_boxes[0], "right", "left"),
        arrow_between(out_boxes[0], out_boxes[1]),
        arrow_between(out_boxes[1], runner_box, "bottom", "top", via_x=runner_x + 120, dashed=True),
    ]

    legend_y = runner_y + 72 + GAP_V
    w = out_end + 40
    h = legend_y + 100

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>Langflow import pipeline</title>
{defs(logo)}
{header(logo, w)}

{inp_svg}

{layer_box(eng_x, eng_y, eng_w, eng_h, "#FFF8E1", "#F57F17", "LangflowTransformationEngine (v0.3.0)", NF["gear"])}
{eng_r1}
{eng_r2}

{layer_box(out_x, out_y, out_w, out_h, "#F3E5F5", "#7B1FA2", "Nexus output files", NF["box"])}
{out_svg}
{runner_svg}

{chr(10).join(arrows)}

  <rect x="{inp_x}" y="{runner_y + 72 + 20}" width="{out_end - inp_x}" height="36" fill="#FFFDE7" stroke="#F9A825" stroke-width="2" rx="8"/>
  <text x="{(inp_x + out_end) // 2}" y="{runner_y + 72 + 44}" text-anchor="middle" class="small">All imported flows default to enabled: false — review before enable</text>

{legend_box(inp_x, legend_y, 520, 70, [
    ("#E8F5E9", "#2E7D32", "Input — Langflow export JSON"),
    ("#FFF3E0", "#EF6C00", "Transform — LangflowTransformationEngine"),
    ("#F3E5F5", "#7B1FA2", "Output — flows.json + nxs_config.json"),
    ("#E0F7FA", "#00838F", "Runtime — FlowRunner triggers at startup"),
    ], "Pipeline stages")}
</svg>"""


# ──────────────────────────────────────────────
# UML activity diagrams (docs/assets/diagrams/activity-*.svg)
# ──────────────────────────────────────────────

ACT_W = 320
ACT_H = 64
ACT_GAP = 36
ACT_FILL = "#FFFFFF"
ACT_STROKE = "#1565C0"
DEC_FILL = "#FFFDE7"
DEC_STROKE = "#F9A825"
NOTE_FILL = "#FFFDE7"
NOTE_STROKE = "#F9A825"
START_R = 14
BAR_H = 8


def _act_start(cx: int, cy: int) -> tuple[str, dict]:
    box = {"x": cx - START_R, "y": cy - START_R, "w": START_R * 2, "h": START_R * 2}
    svg = f'  <circle cx="{cx}" cy="{cy}" r="{START_R}" fill="#1e293b"/>'
    return svg, box


def _act_stop(cx: int, cy: int) -> tuple[str, dict]:
    box = {"x": cx - START_R, "y": cy - START_R, "w": START_R * 2, "h": START_R * 2}
    svg = (
        f'  <circle cx="{cx}" cy="{cy}" r="{START_R}" fill="none" stroke="#1e293b" stroke-width="3"/>\n'
        f'  <circle cx="{cx}" cy="{cy}" r="{START_R - 6}" fill="#1e293b"/>'
    )
    return svg, box


def _act_action(
    x: int,
    y: int,
    label: str,
    desc: str = "",
    fill: str = ACT_FILL,
    stroke: str = ACT_STROKE,
    w: int = ACT_W,
    icon: str = "",
) -> tuple[str, dict]:
    has_icon = bool(icon)
    bw, bh = _measure(label, desc or " ", w, ACT_H, has_icon)
    svg = module(x, y, w, ACT_H, fill, stroke, icon, label, desc or " ", mono=False)
    return svg, {"x": x, "y": y, "w": bw, "h": bh}


def _act_decision(cx: int, cy: int, label: str, size: int = 88) -> tuple[str, dict]:
    half = size // 2
    pts = f"{cx},{cy - half} {cx + half},{cy} {cx},{cy + half} {cx - half},{cy}"
    box = {"x": cx - half, "y": cy - half, "w": size, "h": size}
    svg = (
        f'  <polygon points="{pts}" fill="{DEC_FILL}" stroke="{DEC_STROKE}" '
        f'stroke-width="2" filter="url(#shadow)"/>\n'
        f'  <text x="{cx}" y="{cy + 4}" text-anchor="middle" class="small">{_escape_xml(label)}</text>'
    )
    return svg, box


def _act_sync_bar(x: int, y: int, w: int) -> tuple[str, dict]:
    box = {"x": x, "y": y, "w": w, "h": BAR_H}
    svg = f'  <rect x="{x}" y="{y}" width="{w}" height="{BAR_H}" fill="#1e293b" rx="2"/>'
    return svg, box


def _act_title(title: str, w: int) -> str:
    return f'  <text x="{w // 2}" y="56" text-anchor="middle" class="layer-label">{_escape_xml(title)}</text>'


def _act_chain_arrows(boxes: list[dict], labels: list[str] | None = None) -> list[str]:
    arrows: list[str] = []
    for i in range(len(boxes) - 1):
        lab = labels[i] if labels and i < len(labels) else ""
        arrows.append(arrow_between(boxes[i], boxes[i + 1], "bottom", "top", label=lab))
    return arrows


def _activity_svg(title: str, w: int, h: int, body: str) -> str:
    logo = "../nexus-logo.png"
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <title>{_escape_xml(title)}</title>
{defs(logo)}
{header(logo, w)}
{_act_title(title, w)}
{body}
</svg>"""


def _vertical_actions(
    x: int,
    y: int,
    steps: list[tuple],
    gap: int = ACT_GAP,
) -> tuple[list[str], list[dict], int]:
    """Place a vertical list of action nodes. Each step is (label, desc[, fill, stroke, icon])."""
    parts: list[str] = []
    boxes: list[dict] = []
    cy = y
    for step in steps:
        label, desc = step[0], step[1]
        fill = step[2] if len(step) > 2 else ACT_FILL
        stroke = step[3] if len(step) > 3 else ACT_STROKE
        icon = step[4] if len(step) > 4 else ""
        svg, box = _act_action(x, cy, label, desc, fill, stroke, icon=icon)
        parts.append(svg)
        boxes.append(box)
        cy = box["y"] + box["h"] + gap
    return parts, boxes, cy - gap


def activity_first_run_bootstrap() -> str:
    """Framework — first-run bootstrap."""
    cx = 280
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    act_parts, act_boxes, y = _vertical_actions(x, y, [
        ("Clone nexus-framework-client", "Obtain the Toolkit repository", "#E8F5E9", "#2E7D32", NF["branch"]),
        ("Run setup.zig", "zig run misc/client-setup/setup.zig", "#E8F5E9", "#2E7D32", NF["wrench"]),
        ("Write env.sh / env.bat", "JDK 26 + Zig 0.16.0 toolchain exports", "#E8F5E9", "#2E7D32", NF["file"]),
        ("source misc/client-setup/env.sh", "Activate PATH / JAVA_HOME / ZIG_HOME", "#E3F2FD", "#1565C0", NF["terminal"]),
        ("./misc/build_client.sh", "Compile :core :cli :app", "#E3F2FD", "#1565C0", NF["gear"]),
    ])
    parts.extend(act_parts)

    dec_cy = y + 50
    d_svg, d_box = _act_decision(cx, dec_cy, "Compile OK?")
    parts.append(d_svg)

    yes_x = cx + 130
    no_x = cx - ACT_W - 40
    yes_y = d_box["y"] + d_box["h"] + ACT_GAP
    yes_svg, yes_box = _act_action(
        yes_x - ACT_W // 2, yes_y,
        "Ready for :app:run / :cli:generate",
        "Toolchain verified — proceed to client or CLI",
        "#E8F5E9", "#2E7D32", icon=NF["rocket"],
    )
    no_svg, no_box = _act_action(
        no_x, yes_y,
        "Fix JAVA_HOME / deps",
        "Then rebuild until compile succeeds",
        "#FCE4EC", "#C2185B", icon=NF["wrench"],
    )
    parts.extend([yes_svg, no_svg])

    stop_y = max(yes_box["y"] + yes_box["h"], no_box["y"] + no_box["h"]) + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    flow_boxes = [s_box] + act_boxes + [d_box]
    arrows = _act_chain_arrows(flow_boxes)
    arrows.append(arrow_between(d_box, yes_box, "right", "top", label="yes", via_x=yes_box["x"] + yes_box["w"] // 2))
    arrows.append(arrow_between(d_box, no_box, "left", "top", label="no", via_x=no_box["x"] + no_box["w"] // 2))
    arrows.append(arrow_between(yes_box, stop_box, "bottom", "top", via_x=cx))
    arrows.append(arrow_between(no_box, stop_box, "bottom", "top", via_x=cx, dashed=True, lane=-LANE))

    legend_y = stop_y + START_R + ACT_GAP
    w = max(yes_box["x"] + yes_box["w"], act_boxes[0]["x"] + act_boxes[0]["w"]) + 48
    w = max(w, 720)
    h = legend_y + 110
    body = f"""{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 520, 70, [
    ("#E8F5E9", "#2E7D32", "Setup — Zig bootstrap + env"),
    ("#E3F2FD", "#1565C0", "Build — client compile"),
    ("#FFFDE7", "#F9A825", "Decision — compile gate"),
], "Activity colors")}"""
    return _activity_svg("UML activity — first-run bootstrap", w, h, body)


def activity_client_navigation() -> str:
    """Framework — Compose Desktop client session."""
    cx = 400
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    top_parts, top_boxes, y = _vertical_actions(x, y, [
        ("Launch ./gradlew :app:run", "Compose Desktop process starts", "#E3F2FD", "#1565C0", NF["desktop"]),
        ("LoadingScreen", "Flamingo animation splash", "#E3F2FD", "#1565C0", NF["rocket"]),
        ("Navigate to Home", "Primary hub after load", "#E3F2FD", "#1565C0", NF["layer"]),
    ])
    parts.extend(top_parts)

    # fork bar → parallel browse / about
    bar_w = 480
    bar_x = cx - bar_w // 2
    fork_svg, fork_box = _act_sync_bar(bar_x, y + 8, bar_w)
    parts.append(fork_svg)
    y = fork_box["y"] + fork_box["h"] + ACT_GAP

    left_x = cx - ACT_W - 40
    right_x = cx + 40
    b1_svg, b1 = _act_action(left_x, y, "Browse recent creations", "Open previously generated apps", "#FFF3E0", "#EF6C00", icon=NF["file"])
    b2_svg, b2 = _act_action(right_x, y, "Open What's New / About", "Release notes and branding", "#FFF3E0", "#EF6C00", icon=NF["book"])
    parts.extend([b1_svg, b2_svg])

    join_y = max(b1["y"] + b1["h"], b2["y"] + b2["h"]) + ACT_GAP
    join_svg, join_box = _act_sync_bar(bar_x, join_y, bar_w)
    parts.append(join_svg)

    choose_y = join_box["y"] + join_box["h"] + ACT_GAP
    ch_svg, ch_box = _act_action(x, choose_y, "Choose primary action", "Create · Analyze · Edit blueprint", "#E8EAF6", "#3949AB", icon=NF["search"])
    parts.append(ch_svg)

    # switch cases in a row
    case_y = ch_box["y"] + ch_box["h"] + ACT_GAP + 20
    cases = [
        (cx - 520, "Create project", "GenerateProjectView → ProjectGenerator → NOTICE", "#E8F5E9", "#2E7D32", NF["rocket"]),
        (cx - 170, "Analyze — Debugger", "DebuggerPanel · paste / scan logs", "#FFF3E0", "#EF6C00", NF["search"]),
        (cx + 180, "Analyze — Flows / Tests", "FlowsEditor or TestRunner", "#E0F7FA", "#00838F", NF["cog"]),
        (cx + 530, "Edit blueprint", "BlueprintEditorView (skeleton)", "#F3E5F5", "#7B1FA2", NF["branch"]),
    ]
    case_boxes: list[dict] = []
    for cx_i, label, desc, fill, stroke, icon in cases:
        svg, box = _act_action(cx_i, case_y, label, desc, fill, stroke, w=280, icon=icon)
        parts.append(svg)
        case_boxes.append(box)

    overlay_y = case_boxes[0]["y"] + max(b["h"] for b in case_boxes) + ACT_GAP
    ov_svg, ov_box = _act_action(
        x, overlay_y, "Optional flamingo transition", "Overlay before next screen",
        "#FCE4EC", "#C2185B", icon=NF["layer"],
    )
    parts.append(ov_svg)

    stop_y = ov_box["y"] + ov_box["h"] + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    arrows = [
        arrow_between(s_box, top_boxes[0]),
        *_act_chain_arrows(top_boxes),
        arrow_between(top_boxes[-1], fork_box),
        arrow_between(fork_box, b1, "bottom", "top", via_x=b1["x"] + b1["w"] // 2),
        arrow_between(fork_box, b2, "bottom", "top", via_x=b2["x"] + b2["w"] // 2),
        arrow_between(b1, join_box, "bottom", "top", via_x=b1["x"] + b1["w"] // 2),
        arrow_between(b2, join_box, "bottom", "top", via_x=b2["x"] + b2["w"] // 2),
        arrow_between(join_box, ch_box),
    ]
    for cb in case_boxes:
        arrows.append(arrow_between(ch_box, cb, "bottom", "top", via_x=cb["x"] + cb["w"] // 2))
        arrows.append(arrow_between(cb, ov_box, "bottom", "top", via_x=cx, dashed=True))
    arrows.append(arrow_between(ov_box, stop_box))

    legend_y = stop_y + START_R + ACT_GAP
    w = case_boxes[-1]["x"] + case_boxes[-1]["w"] + 48
    h = legend_y + 110
    body = f"""{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 640, 70, [
    ("#E3F2FD", "#1565C0", "Session — load & home"),
    ("#FFF3E0", "#EF6C00", "Side paths — recent / about"),
    ("#E8EAF6", "#3949AB", "Switch — primary tool action"),
], "Client navigation")}"""
    return _activity_svg("UML activity — Compose client navigation", w, h, body)


def activity_generate_pipeline() -> str:
    """Framework — project generation pipeline (:core)."""
    cx = 300
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    head_parts, head_boxes, y = _vertical_actions(x, y, [
        ("Validate ProjectSpec", "name · type · output path", "#E3F2FD", "#1565C0", NF["gear"]),
        ("Resolve template/ + shared/", "desktop-app or android-app skeleton", "#F3E5F5", "#7B1FA2", NF["package"]),
    ])
    parts.extend(head_parts)

    dec_cy = y + 50
    d_svg, d_box = _act_decision(cx, dec_cy, "dry-run?")
    parts.append(d_svg)

    dry_x = cx + 160
    dry_svg, dry_box = _act_action(
        dry_x, d_box["y"], "List rendered paths", "No files written — preview only",
        "#ECEFF1", "#455A64", icon=NF["search"],
    )
    parts.append(dry_svg)
    dry_stop_svg, dry_stop = _act_stop(dry_box["x"] + dry_box["w"] // 2, dry_box["y"] + dry_box["h"] + ACT_GAP + START_R)
    parts.append(dry_stop_svg)

    main_y = d_box["y"] + d_box["h"] + ACT_GAP
    main_parts, main_boxes, y = _vertical_actions(x, main_y, [
        ("Ensure output dir", "force overwrite if requested", "#FFF3E0", "#EF6C00", NF["box"]),
        ("Copy template + render", "Substitute {{placeholders}}", "#FFF3E0", "#EF6C00", NF["code"]),
        ("Copy shared/ helpers", "DSL, themes, runtime modules", "#FFF3E0", "#EF6C00", NF["layer"]),
        ("Write ScriptProtectionConfig", "If script protection enabled", "#E0F7FA", "#00838F", NF["file"]),
        ("Write custom blueprint.json", "If provided by client/CLI", "#E0F7FA", "#00838F", NF["file"]),
        ("Write custom flows.json", "If provided by client/CLI", "#E0F7FA", "#00838F", NF["cog"]),
        ("Write NOTICE", "Nexus License attribution", "#E8F5E9", "#2E7D32", NF["book"]),
        ("Validate nxs_config.json", "Schema v2 check", "#E8EAF6", "#3949AB", NF["gear"]),
        ("Validate blueprint.json", "Graph nodes and edges", "#E8EAF6", "#3949AB", NF["branch"]),
        ("Validate flows.json", "Triggers and steps", "#E8EAF6", "#3949AB", NF["cog"]),
        ("Done → builds/framework/<name>/", "Out-of-source native project tree", "#E8F5E9", "#2E7D32", NF["rocket"]),
    ])
    parts.extend(main_parts)

    stop_y = y + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    arrows = [
        arrow_between(s_box, head_boxes[0]),
        *_act_chain_arrows(head_boxes),
        arrow_between(head_boxes[-1], d_box),
        arrow_between(d_box, dry_box, "right", "left", label="yes"),
        arrow_between(dry_box, dry_stop),
        arrow_between(d_box, main_boxes[0], "bottom", "top", label="no"),
        *_act_chain_arrows(main_boxes),
        arrow_between(main_boxes[-1], stop_box),
    ]

    legend_y = stop_y + START_R + ACT_GAP
    w = max(dry_box["x"] + dry_box["w"], main_boxes[0]["x"] + main_boxes[0]["w"]) + 64
    h = legend_y + 110
    body = f"""{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 560, 70, [
    ("#E3F2FD", "#1565C0", "Validate — ProjectSpec"),
    ("#FFF3E0", "#EF6C00", "Emit — template copy + render"),
    ("#E8EAF6", "#3949AB", "Validate — config / blueprint / flows"),
], "Generation pipeline")}"""
    return _activity_svg("UML activity — project generation pipeline", w, h, body)


def activity_build_desktop_app() -> str:
    """Framework — build a generated desktop app."""
    cx = 280
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    act_parts, act_boxes, y = _vertical_actions(x, y, [
        ("cd builds/framework/<App>", "Enter generated project tree", "#ECEFF1", "#455A64", NF["box"]),
        ("./build_app.sh", "Host orchestrator for native build", "#E3F2FD", "#1565C0", NF["terminal"]),
        ("Create / refresh Python venv", "Host tooling for pack/archive steps", "#E8F5E9", "#2E7D32", NF["python"]),
        ("Fetch / check native deps", "SDL3, Lua, ImGui, …", "#E8F5E9", "#2E7D32", NF["package"]),
        ("g++ -fmodules-ts compile", "Compile .cppm translation units", "#F3E5F5", "#7B1FA2", NF["code"]),
        ("zig build nexus_zig sidecar", "zig-services compile + link helpers", "#E3F2FD", "#1565C0", NF["gear"]),
        ("Link desktop binary", "Produce runnable SDL3 executable", "#E3F2FD", "#1565C0", NF["desktop"]),
    ])
    parts.extend(act_parts)

    dec_cy = y + 50
    d_svg, d_box = _act_decision(cx, dec_cy, "Success?")
    parts.append(d_svg)

    yes_x = cx + 140
    no_x = cx - ACT_W - 50
    yes_y = d_box["y"] + d_box["h"] + ACT_GAP
    yes_svg, yes_box = _act_action(
        yes_x - ACT_W // 2, yes_y, "Run / package binary", "Launch or ship the desktop app",
        "#E8F5E9", "#2E7D32", icon=NF["rocket"],
    )
    no_svg, no_box = _act_action(
        no_x, yes_y, "Inspect build log", "Fix modules / Zig / deps and retry",
        "#FCE4EC", "#C2185B", icon=NF["search"],
    )
    parts.extend([yes_svg, no_svg])

    stop_y = max(yes_box["y"] + yes_box["h"], no_box["y"] + no_box["h"]) + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    flow = [s_box] + act_boxes + [d_box]
    arrows = _act_chain_arrows(flow)
    arrows.append(arrow_between(d_box, yes_box, "right", "top", label="yes", via_x=yes_box["x"] + yes_box["w"] // 2))
    arrows.append(arrow_between(d_box, no_box, "left", "top", label="no", via_x=no_box["x"] + no_box["w"] // 2))
    arrows.append(arrow_between(yes_box, stop_box, "bottom", "top", via_x=cx))
    arrows.append(arrow_between(no_box, stop_box, "bottom", "top", via_x=cx, dashed=True))

    legend_y = stop_y + START_R + ACT_GAP
    w = max(yes_box["x"] + yes_box["w"], 720)
    h = legend_y + 100
    body = f"""{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 520, 70, [
    ("#E3F2FD", "#1565C0", "Build — script + Zig sidecar"),
    ("#F3E5F5", "#7B1FA2", "Compile — C++ modules"),
    ("#FFFDE7", "#F9A825", "Decision — link success"),
], "Desktop build")}"""
    return _activity_svg("UML activity — build generated desktop app", w, h, body)


def activity_desktop_frame_loop() -> str:
    """Derived app — typical desktop frame loop."""
    cx = 300
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    init_parts, init_boxes, y = _vertical_actions(x, y, [
        ("main() — init SDL3 + GL + ImGui", "Create window and graphics context", "#E3F2FD", "#1565C0", NF["desktop"]),
        ("Init NexusBridge / Lua / Python", "Optional scripting runtimes", "#E8F5E9", "#2E7D32", NF["plug"]),
        ("Load blueprint + optional flows", "Graph + FlowRunner registration", "#FFF8E1", "#F57F17", NF["file"]),
        ("Create AppModel + Controller + View", "MVC wiring for the frame", "#F3E5F5", "#7B1FA2", NF["code"]),
    ])
    parts.extend(init_parts)

    # loop region
    loop_top = y + 8
    loop_label = f'  <text x="{x - 8}" y="{loop_top + 16}" text-anchor="end" class="badge">repeat</text>'

    loop_parts, loop_boxes, y = _vertical_actions(x, y + 24, [
        ("Poll SDL events", "Input and window messages", "#ECEFF1", "#455A64", NF["desktop"]),
        ("AppController.dispatch", "Route commands from UI / scripts", "#E3F2FD", "#1565C0", NF["gear"]),
        ("Update AppModel", "Mutate domain state", "#FFF3E0", "#EF6C00", NF["database"]),
    ])
    parts.extend(loop_parts)

    dec_cy = y + 50
    d_svg, d_box = _act_decision(cx, dec_cy, "flows?")
    parts.append(d_svg)

    flow_x = cx + 150
    flow_svg, flow_box = _act_action(
        flow_x, d_box["y"] - 10, "FlowRunner tick / triggers", "Background and event steps",
        "#E8F5E9", "#2E7D32", w=280, icon=NF["cog"],
    )
    parts.append(flow_svg)

    cont_y = d_box["y"] + d_box["h"] + ACT_GAP
    cont_parts, cont_boxes, y = _vertical_actions(x, cont_y, [
        ("LuaPanels hot-reload check", "Reload panels.lua when changed", "#E0F7FA", "#00838F", NF["terminal"]),
        ("AppView.render (ImGui)", "Draw UI for this frame", "#E3F2FD", "#1565C0", NF["chart"]),
        ("Swap buffers", "Present frame to display", "#ECEFF1", "#455A64", NF["desktop"]),
    ])
    parts.extend(cont_parts)

    # while window open — back arrow annotation
    loop_bottom = y
    while_note = (
        f'  <text x="{cx + ACT_W // 2 + 24}" y="{(loop_top + loop_bottom) // 2}" '
        f'class="desc">while window open</text>'
    )

    shut_y = y + ACT_GAP
    shut_svg, shut_box = _act_action(
        x, shut_y, "Shutdown bridges + SDL", "Tear down Python / Lua / GL",
        "#FCE4EC", "#C2185B", icon=NF["wrench"],
    )
    parts.append(shut_svg)

    stop_y = shut_box["y"] + shut_box["h"] + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    # loop panel background
    panel = (
        f'  <rect x="{x - 24}" y="{loop_top}" width="{ACT_W + 48}" '
        f'height="{loop_bottom - loop_top + 8}" class="panel" fill="#E8F4FD" '
        f'stroke="#1565C0" fill-opacity="0.25"/>'
    )

    arrows = [
        arrow_between(s_box, init_boxes[0]),
        *_act_chain_arrows(init_boxes),
        arrow_between(init_boxes[-1], loop_boxes[0]),
        *_act_chain_arrows(loop_boxes),
        arrow_between(loop_boxes[-1], d_box),
        arrow_between(d_box, flow_box, "right", "left", label="yes"),
        arrow_between(flow_box, cont_boxes[0], "bottom", "top", via_x=cx, dashed=True),
        arrow_between(d_box, cont_boxes[0], "bottom", "top", label="no"),
        *_act_chain_arrows(cont_boxes),
        # loop back
        arrow_ortho(
            *_anchor(cont_boxes[-1], "right"),
            *_anchor(loop_boxes[0], "right"),
            via_x=x + ACT_W + 80,
            label="loop",
            dashed=True,
        ),
        arrow_between(cont_boxes[-1], shut_box, "bottom", "top", label="close"),
        arrow_between(shut_box, stop_box),
    ]

    legend_y = stop_y + START_R + ACT_GAP
    w = max(flow_box["x"] + flow_box["w"], x + ACT_W + 120) + 48
    h = legend_y + 100
    body = f"""{panel}
{loop_label}
{chr(10).join(parts)}
{while_note}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 560, 70, [
    ("#E3F2FD", "#1565C0", "Init / render — SDL3 + ImGui"),
    ("#E8F4FD", "#1565C0", "Frame loop — poll → update → draw"),
    ("#E8F5E9", "#2E7D32", "Optional — FlowRunner tick"),
], "Desktop frame loop")}"""
    return _activity_svg("UML activity — desktop frame loop", w, h, body)


def activity_hello_counter() -> str:
    """Derived app — sample hello / counter starter."""
    cx = 400
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    open_svg, open_box = _act_action(
        cx - ACT_W // 2, y, "User opens generated app", "Default template desktop binary",
        "#E3F2FD", "#1565C0", icon=NF["desktop"],
    )
    parts.append(open_svg)
    greet_y = open_box["y"] + open_box["h"] + ACT_GAP
    greet_svg, greet_box = _act_action(
        cx - ACT_W // 2, greet_y, "See greeting + counter panel", "ImGui starter UI",
        "#E3F2FD", "#1565C0", icon=NF["chart"],
    )
    parts.append(greet_svg)

    bar_w = 700
    bar_x = cx - bar_w // 2
    fork_y = greet_box["y"] + greet_box["h"] + ACT_GAP
    fork_svg, fork_box = _act_sync_bar(bar_x, fork_y, bar_w)
    parts.append(fork_svg)

    branch_y = fork_box["y"] + fork_box["h"] + ACT_GAP
    branches = [
        (cx - 360, [
            ("Click Increment", "UI command from counter panel", "#FFF3E0", "#EF6C00", NF["layer"]),
            ("Controller → Model.counter++", "Mutate domain state", "#F3E5F5", "#7B1FA2", NF["gear"]),
            ("View redraws count", "ImGui reflects new value", "#E3F2FD", "#1565C0", NF["chart"]),
        ]),
        (cx - 110, [
            ("Edit Lua panel script", "Author change in panels.lua", "#E0F7FA", "#00838F", NF["terminal"]),
            ("Hot-reload panels.lua", "sol2 picks up new panel defs", "#E0F7FA", "#00838F", NF["code"]),
        ]),
        (cx + 140, [
            ("Call Python helper", "FFT / util via pybind11", "#E8F5E9", "#2E7D32", NF["python"]),
            ("Return into C++ model", "Bridge result stored in model", "#E8F5E9", "#2E7D32", NF["database"]),
        ]),
    ]
    all_branch_boxes: list[list[dict]] = []
    branch_arrows: list[str] = []
    for bx, steps in branches:
        bp, bb, _ = _vertical_actions(bx, branch_y, steps, gap=28)
        parts.extend(bp)
        all_branch_boxes.append(bb)
        branch_arrows.extend(_act_chain_arrows(bb))

    join_y = max(bb[-1]["y"] + bb[-1]["h"] for bb in all_branch_boxes) + ACT_GAP
    join_svg, join_box = _act_sync_bar(bar_x, join_y, bar_w)
    parts.append(join_svg)

    close_y = join_box["y"] + join_box["h"] + ACT_GAP
    close_svg, close_box = _act_action(
        cx - ACT_W // 2, close_y, "User closes window", "Exit main loop",
        "#FCE4EC", "#C2185B", icon=NF["desktop"],
    )
    parts.append(close_svg)
    stop_y = close_box["y"] + close_box["h"] + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    arrows = [
        arrow_between(s_box, open_box),
        arrow_between(open_box, greet_box),
        arrow_between(greet_box, fork_box),
        *branch_arrows,
    ]
    for bb in all_branch_boxes:
        arrows.append(arrow_between(fork_box, bb[0], "bottom", "top", via_x=bb[0]["x"] + bb[0]["w"] // 2))
        arrows.append(arrow_between(bb[-1], join_box, "bottom", "top", via_x=bb[-1]["x"] + bb[-1]["w"] // 2))
    arrows.append(arrow_between(join_box, close_box))
    arrows.append(arrow_between(close_box, stop_box))

    legend_y = stop_y + START_R + ACT_GAP
    w = 880
    h = legend_y + 110
    body = f"""{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 640, 70, [
    ("#FFF3E0", "#EF6C00", "Counter — increment path"),
    ("#E0F7FA", "#00838F", "Lua — hot-reload panels"),
    ("#E8F5E9", "#2E7D32", "Python — helper via pybind11"),
], "Starter interactions (parallel)")}"""
    return _activity_svg("UML activity — hello / counter starter", w, h, body)


def activity_flows_automation() -> str:
    """Derived app — blueprint + flows automation."""
    cx = 300
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    head_parts, head_boxes, y = _vertical_actions(x, y, [
        ("App boot — load flows.json", "Parse automation definitions", "#E8F5E9", "#2E7D32", NF["file"]),
        ("Register triggers", "timer · event · UI · startup", "#E8F5E9", "#2E7D32", NF["cog"]),
    ])
    parts.extend(head_parts)

    loop_top = y + 8
    loop_parts, loop_boxes, y = _vertical_actions(x, y + 24, [
        ("Wait for trigger", "Block until matching event", "#ECEFF1", "#455A64", NF["comment"]),
        ("Select matching flow", "Pick enabled flow by trigger", "#FFF3E0", "#EF6C00", NF["search"]),
        ("Execute steps in order", "Walk steps[] sequentially", "#E3F2FD", "#1565C0", NF["gear"]),
    ])
    parts.extend(loop_parts)

    bar_w = 640
    bar_x = cx - bar_w // 2
    fork_y = y + 8
    fork_svg, fork_box = _act_sync_bar(bar_x, fork_y, bar_w)
    parts.append(fork_svg)

    step_y = fork_box["y"] + fork_box["h"] + ACT_GAP
    step_specs = [
        (cx - 340, "Invoke C++ / Lua action", "nxs.* or lua.* target", "#F3E5F5", "#7B1FA2", NF["code"]),
        (cx - 110, "Invoke Python step", "python.* via bridge", "#E8F5E9", "#2E7D32", NF["python"]),
        (cx + 120, "Delay / branch", "Condition or wait step", "#E0F7FA", "#00838F", NF["branch"]),
    ]
    step_boxes: list[dict] = []
    for sx, label, desc, fill, stroke, icon in step_specs:
        svg, box = _act_action(sx, step_y, label, desc, fill, stroke, w=260, icon=icon)
        parts.append(svg)
        step_boxes.append(box)

    join_y = step_boxes[0]["y"] + step_boxes[0]["h"] + ACT_GAP
    join_svg, join_box = _act_sync_bar(bar_x, join_y, bar_w)
    parts.append(join_svg)

    log_y = join_box["y"] + join_box["h"] + ACT_GAP
    log_svg, log_box = _act_action(
        x, log_y, "Log step result", "Record success / failure for debugger",
        "#E8EAF6", "#3949AB", icon=NF["file"],
    )
    parts.append(log_svg)

    panel = (
        f'  <rect x="{x - 24}" y="{loop_top}" width="{ACT_W + 48}" '
        f'height="{log_box["y"] + log_box["h"] - loop_top + 16}" class="panel" '
        f'fill="#E8F5E9" stroke="#2E7D32" fill-opacity="0.2"/>'
    )
    loop_label = f'  <text x="{x - 8}" y="{loop_top + 16}" text-anchor="end" class="badge">while app running</text>'

    stop_y = log_box["y"] + log_box["h"] + ACT_GAP + 40 + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    arrows = [
        arrow_between(s_box, head_boxes[0]),
        *_act_chain_arrows(head_boxes),
        arrow_between(head_boxes[-1], loop_boxes[0]),
        *_act_chain_arrows(loop_boxes),
        arrow_between(loop_boxes[-1], fork_box),
    ]
    for sb in step_boxes:
        arrows.append(arrow_between(fork_box, sb, "bottom", "top", via_x=sb["x"] + sb["w"] // 2))
        arrows.append(arrow_between(sb, join_box, "bottom", "top", via_x=sb["x"] + sb["w"] // 2))
    arrows.append(arrow_between(join_box, log_box))
    arrows.append(
        arrow_ortho(
            *_anchor(log_box, "left"),
            *_anchor(loop_boxes[0], "left"),
            via_x=x - 60,
            label="loop",
            dashed=True,
        )
    )
    arrows.append(arrow_between(log_box, stop_box, "bottom", "top", label="exit", dashed=True))

    legend_y = stop_y + START_R + ACT_GAP
    w = 820
    h = legend_y + 100
    body = f"""{panel}
{loop_label}
{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 600, 70, [
    ("#E8F5E9", "#2E7D32", "Load — flows.json + triggers"),
    ("#E3F2FD", "#1565C0", "Execute — ordered steps"),
    ("#F3E5F5", "#7B1FA2", "Step kinds — C++/Lua · Python · branch"),
], "Flows automation")}"""
    return _activity_svg("UML activity — flows automation", w, h, body)


def activity_langflow_import() -> str:
    """Derived app — Langflow → Nexus import."""
    cx = 300
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    act_parts, act_boxes, y = _vertical_actions(x, y, [
        ("Export Langflow JSON", "API or Export flow from Langflow UI", "#E8F5E9", "#2E7D32", NF["robot"]),
        ("LangflowTransformationEngine", ":core import — map DAG to Nexus", "#E8EAF6", "#3949AB", NF["gear"]),
        ("Produce blueprint.json (+ flows)", "Structure + optional automations", "#F3E5F5", "#7B1FA2", NF["file"]),
        ("ProjectGenerator.generate", "Emit builds/framework/<name>/", "#FFF3E0", "#EF6C00", NF["rocket"]),
        ("User edits C++ / Lua / Python", "Customize generated sources", "#E3F2FD", "#1565C0", NF["code"]),
        ("./build_app.sh", "Native compile of derived app", "#E3F2FD", "#1565C0", NF["terminal"]),
        ("Ship native binary", "Distribute desktop or Android build", "#E8F5E9", "#2E7D32", NF["box"]),
    ])
    parts.extend(act_parts)

    note_y = y + 12
    note = f"""  <rect x="{x}" y="{note_y}" width="{ACT_W}" height="56" class="panel" fill="{NOTE_FILL}" stroke="{NOTE_STROKE}"/>
  <text x="{cx}" y="{note_y + 24}" text-anchor="middle" class="small">Retain NOTICE attribution</text>
  <text x="{cx}" y="{note_y + 42}" text-anchor="middle" class="desc">Nexus License (Nexus-1.0)</text>"""

    stop_y = note_y + 56 + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    arrows = [
        arrow_between(s_box, act_boxes[0]),
        *_act_chain_arrows(act_boxes),
        arrow(cx, act_boxes[-1]["y"] + act_boxes[-1]["h"], cx, note_y),
        arrow(cx, note_y + 56, cx, stop_box["y"]),
    ]

    legend_y = stop_y + START_R + ACT_GAP
    w = 640
    h = legend_y + 100
    body = f"""{chr(10).join(parts)}
{note}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 520, 70, [
    ("#E8F5E9", "#2E7D32", "External — Langflow export"),
    ("#E8EAF6", "#3949AB", "Transform — :core engine"),
    ("#FFF3E0", "#EF6C00", "Generate — ProjectGenerator"),
], "Langflow import path")}"""
    return _activity_svg("UML activity — Langflow → Nexus import", w, h, body)


def activity_android_field_tablet() -> str:
    """Derived app — sample Android field tablet."""
    cx = 300
    x = cx - ACT_W // 2
    y = 100
    parts: list[str] = []

    s_svg, s_box = _act_start(cx, y)
    parts.append(s_svg)
    y = s_box["y"] + s_box["h"] + ACT_GAP

    init_parts, init_boxes, y = _vertical_actions(x, y, [
        ("Launch APK (NexusApplication)", "Android process start", "#FCE4EC", "#C2185B", NF["android"]),
        ("Start Chaquopy Python", "Embed CPython in APK", "#FCE4EC", "#C2185B", NF["python"]),
        ("Init Zig JNI bridge", "Hand-authored C ABI / JNI", "#FCE4EC", "#C2185B", NF["plug"]),
        ("SDL3 / GLES + ImGui loop", "Full-screen touch UI", "#E3F2FD", "#1565C0", NF["desktop"]),
    ])
    parts.extend(init_parts)

    loop_top = y + 8
    loop_parts, loop_boxes, y = _vertical_actions(x, y + 24, [
        ("Touch / sensor input", "Pointer and device sensors", "#ECEFF1", "#455A64", NF["phone"]),
        ("Controller updates model", "MVC command path", "#F3E5F5", "#7B1FA2", NF["gear"]),
        ("Optional Python ML / analysis", "Chaquopy evaluate on device", "#E8F5E9", "#2E7D32", NF["python"]),
        ("Render ImGui UI", "GLES present", "#E3F2FD", "#1565C0", NF["chart"]),
    ])
    parts.extend(loop_parts)

    panel = (
        f'  <rect x="{x - 24}" y="{loop_top}" width="{ACT_W + 48}" '
        f'height="{y - loop_top + 8}" class="panel" fill="#FCE4EC" '
        f'stroke="#C2185B" fill-opacity="0.2"/>'
    )
    loop_label = f'  <text x="{x - 8}" y="{loop_top + 16}" text-anchor="end" class="badge">while activity alive</text>'

    shut_y = y + ACT_GAP
    shut_svg, shut_box = _act_action(
        x, shut_y, "Release JNI + Python", "Tear down bridge and Chaquopy",
        "#FFF3E0", "#EF6C00", icon=NF["wrench"],
    )
    parts.append(shut_svg)

    stop_y = shut_box["y"] + shut_box["h"] + ACT_GAP + START_R
    stop_svg, stop_box = _act_stop(cx, stop_y)
    parts.append(stop_svg)

    arrows = [
        arrow_between(s_box, init_boxes[0]),
        *_act_chain_arrows(init_boxes),
        arrow_between(init_boxes[-1], loop_boxes[0]),
        *_act_chain_arrows(loop_boxes),
        arrow_ortho(
            *_anchor(loop_boxes[-1], "right"),
            *_anchor(loop_boxes[0], "right"),
            via_x=x + ACT_W + 70,
            label="loop",
            dashed=True,
        ),
        arrow_between(loop_boxes[-1], shut_box, "bottom", "top", label="destroy"),
        arrow_between(shut_box, stop_box),
    ]

    legend_y = stop_y + START_R + ACT_GAP
    w = x + ACT_W + 120
    h = legend_y + 100
    body = f"""{panel}
{loop_label}
{chr(10).join(parts)}
{chr(10).join(arrows)}
{legend_box(48, legend_y, 520, 70, [
    ("#FCE4EC", "#C2185B", "Android — APK + Zig JNI + Chaquopy"),
    ("#E3F2FD", "#1565C0", "UI — SDL3 GLES + ImGui"),
    ("#E8F5E9", "#2E7D32", "Optional — on-device Python ML"),
], "Android field tablet")}"""
    return _activity_svg("UML activity — Android field tablet", w, h, body)


# ──────────────────────────────────────────────
# Interface mockups (docs/assets/examples/*.svg)
# Palette aligned with Compose NexusTheme / NexusBranding.
# Regenerate mockups only (does not touch activity/architecture SVGs):
#   python3 misc/scripts/generate-diagrams.py --mockups
# ──────────────────────────────────────────────

MOCKUP_W = 920
MOCKUP_H = 600
MOCKUP_BG = "#1A1A2E"
MOCKUP_SURFACE = "#16213E"
MOCKUP_CARD = "#1F2B47"
MOCKUP_TEXT = "#E8E8E8"
MOCKUP_SUBTEXT = "#9090A0"
MOCKUP_MUTED = "#707070"
MOCKUP_DIVIDER = "#3A3A5C"
MOCKUP_CYAN = "#00D4FF"
MOCKUP_GREEN = "#00E676"
MOCKUP_ORANGE = "#FF9100"
MOCKUP_RED = "#FF5252"
MOCKUP_PINK = "#F38BA8"
MOCKUP_PURPLE = "#6C63FF"
MOCKUP_FLAMINGO = "#F38BA8"
MOCKUP_BEAK = "#FAB387"


def _mockup_frame(title: str, body: str, w: int = MOCKUP_W, h: int = MOCKUP_H) -> str:
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">
  <rect width="100%" height="100%" fill="{MOCKUP_BG}"/>
  <rect x="0" y="0" width="100%" height="40" fill="{MOCKUP_CARD}"/>
  <circle cx="20" cy="20" r="6" fill="{MOCKUP_FLAMINGO}"/>
  <circle cx="38" cy="20" r="6" fill="{MOCKUP_BEAK}"/>
  <circle cx="56" cy="20" r="6" fill="{MOCKUP_GREEN}"/>
  <text x="{w // 2}" y="26" text-anchor="middle" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">{title}</text>
{body}
</svg>"""


def _mockup_card(x: int, y: int, w: int, h: int, accent: str, icon: str, label: str, desc: str) -> str:
    return f"""  <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="{MOCKUP_CARD}" stroke="{accent}" stroke-width="2"/>
  <text x="{x + 16}" y="{y + 32}" font-family="sans-serif" font-size="20" fill="{accent}">{icon}</text>
  <text x="{x + 16}" y="{y + 60}" font-family="sans-serif" font-size="14" font-weight="bold" fill="{MOCKUP_TEXT}">{label}</text>
  <text x="{x + 16}" y="{y + 78}" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">{desc}</text>"""


def welcome_dashboard_mockup() -> str:
    """Home hub matching Compose HomeScreen (the main dashboard).

    Not a second tool-grid hub — recent creations + create/analyze/blueprint.
    Written to mockup-welcome.svg only (no separate mockup-dashboard.svg).
    """
    recent = ""
    for i, (name, meta) in enumerate([
        ("DemoDesktop", "Desktop · builds/framework/DemoDesktop"),
        ("MyAndroidApp", "Android · builds/framework/MyAndroidApp"),
        ("PrototypeUI", "Desktop · builds/framework/PrototypeUI"),
    ]):
        y = 200 + i * 70
        recent += f"""  <rect x="48" y="{y}" width="360" height="58" rx="10" fill="{MOCKUP_CARD}"/>
  <circle cx="74" cy="{y + 29}" r="12" fill="{MOCKUP_FLAMINGO}" opacity="0.85"/>
  <text x="98" y="{y + 24}" font-family="sans-serif" font-size="13" font-weight="bold" fill="{MOCKUP_TEXT}">{name}</text>
  <text x="98" y="{y + 42}" font-family="sans-serif" font-size="10" fill="{MOCKUP_SUBTEXT}">{meta}</text>
"""

    actions = ""
    for i, (title, desc, accent) in enumerate([
        ("Create project", "Scaffold from Nexus templates", MOCKUP_CYAN),
        ("Analyze project", "Debugger · Flows · Tests", MOCKUP_FLAMINGO),
        ("Blueprint editor", "Design the app structure DAG", MOCKUP_PURPLE),
    ]):
        y = 100 + i * 100
        actions += f"""  <rect x="460" y="{y}" width="400" height="84" rx="14" fill="{MOCKUP_CARD}"/>
  <rect x="460" y="{y}" width="400" height="5" fill="{accent}"/>
  <text x="484" y="{y + 38}" font-family="sans-serif" font-size="15" font-weight="bold" fill="{MOCKUP_TEXT}">{title}</text>
  <text x="484" y="{y + 58}" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">{desc}</text>
"""

    body = f"""  <!-- Left: brand + recent -->
  <circle cx="220" cy="100" r="36" fill="{MOCKUP_FLAMINGO}" opacity="0.9"/>
  <text x="220" y="160" text-anchor="middle" font-family="sans-serif" font-size="20" font-weight="bold" fill="{MOCKUP_TEXT}">The Nexus Framework</text>
  <text x="220" y="180" text-anchor="middle" font-family="sans-serif" font-size="12" fill="{MOCKUP_SUBTEXT}">v1.0.2 · Home (main dashboard)</text>
  <text x="48" y="192" font-family="sans-serif" font-size="12" font-weight="bold" fill="{MOCKUP_TEXT}">Recent creations</text>
{recent}
  <text x="220" y="440" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_MUTED}">SDL3 · ImGui · Lua · Python · TS/XHTML · Zig</text>

  <!-- Right: actions -->
{actions}
  <text x="460" y="430" font-family="sans-serif" font-size="10" fill="{MOCKUP_FLAMINGO}">About · What's new · Docs · GitHub</text>
  <text x="460" y="560" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">Compose: HomeScreen.kt · AppScreen.Welcome/Dashboard alias Home</text>"""
    return _mockup_frame("Home — The Nexus Framework", body)


def generate_project_mockup() -> str:
    body = f"""  <text x="60" y="90" font-family="sans-serif" font-size="16" font-weight="bold" fill="{MOCKUP_TEXT}">Generate Project</text>

  <rect x="60" y="110" width="300" height="32" rx="6" fill="{MOCKUP_CARD}" stroke="{MOCKUP_SUBTEXT}"/>
  <text x="70" y="131" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">MyApp</text>
  <text x="60" y="100" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">Project name</text>

  <rect x="60" y="170" width="220" height="80" rx="10" fill="{MOCKUP_CARD}" stroke="{MOCKUP_CYAN}" stroke-width="2"/>
  <text x="80" y="200" font-family="sans-serif" font-size="24" fill="{MOCKUP_CYAN}">&#x1f5a5;</text>
  <text x="110" y="200" font-family="sans-serif" font-size="14" font-weight="bold" fill="{MOCKUP_TEXT}">Desktop App</text>
  <text x="110" y="218" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">SDL3 + ImGui + pybind11</text>

  <rect x="300" y="170" width="220" height="80" rx="10" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="320" y="200" font-family="sans-serif" font-size="24" fill="{MOCKUP_GREEN}">&#x1f4f1;</text>
  <text x="350" y="200" font-family="sans-serif" font-size="14" font-weight="bold" fill="{MOCKUP_SUBTEXT}">Android App</text>
  <text x="350" y="218" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">SDL3 GLES + Chaquopy</text>

  <text x="60" y="168" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">App type</text>

  <rect x="60" y="280" width="460" height="32" rx="6" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="70" y="301" font-family="sans-serif" font-size="12" fill="{MOCKUP_SUBTEXT}">builds/framework/MyApp/</text>
  <text x="60" y="270" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">Output path</text>

  <rect x="60" y="340" width="200" height="40" rx="8" fill="{MOCKUP_CYAN}"/>
  <text x="160" y="365" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="bold" fill="{MOCKUP_BG}">Generate</text>

  <rect x="280" y="340" width="140" height="40" rx="8" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="350" y="365" text-anchor="middle" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">Blueprint</text>

  <rect x="440" y="340" width="120" height="40" rx="8" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="500" y="365" text-anchor="middle" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">Flows</text>"""
    return _mockup_frame("Generate Project", body, w=800, h=520)


def blueprint_editor_mockup() -> str:
    """Node-graph mockup matching Compose BlueprintEditorView — palette / canvas / inspector."""
    w, h = 960, 580
    palette_types = [
        ("Python module", "python.module", MOCKUP_GREEN),
        ("C++ model", "cpp.model", MOCKUP_CYAN),
        ("C++ controller", "cpp.controller", MOCKUP_PURPLE),
        ("UI page", "ui.page", MOCKUP_ORANGE),
        ("Lua script", "lua.script", MOCKUP_FLAMINGO),
    ]
    palette_rows = ""
    for i, (label, tid, accent) in enumerate(palette_types):
        y = 150 + i * 44
        palette_rows += f"""  <rect x="28" y="{y}" width="132" height="36" rx="6" fill="{MOCKUP_SURFACE}" stroke="{accent}" stroke-opacity="0.4"/>
  <circle cx="44" cy="{y + 18}" r="4" fill="{accent}"/>
  <text x="56" y="{y + 15}" font-family="sans-serif" font-size="10" font-weight="bold" fill="{MOCKUP_TEXT}">{label}</text>
  <text x="56" y="{y + 28}" font-family="sans-serif" font-size="8" fill="{MOCKUP_MUTED}">{tid}</text>
"""

    nodes = [
        (220, 180, "py-helpers", "python.module", MOCKUP_GREEN),
        (420, 140, "cpp-model", "cpp.model", MOCKUP_CYAN),
        (420, 240, "cpp-ctrl", "cpp.controller", MOCKUP_PURPLE),
        (620, 180, "ui-main", "ui.page", MOCKUP_ORANGE),
        (420, 360, "lua-panels", "lua.script", MOCKUP_FLAMINGO),
    ]
    node_rects = ""
    for x, y, nid, ntype, accent in nodes:
        node_rects += f"""  <rect x="{x}" y="{y}" width="148" height="52" rx="8" fill="{MOCKUP_CARD}" stroke="{accent}" stroke-width="1.5"/>
  <rect x="{x}" y="{y}" width="4" height="52" fill="{accent}"/>
  <text x="{x + 14}" y="{y + 22}" font-family="sans-serif" font-size="11" font-weight="bold" fill="{MOCKUP_TEXT}">{nid}</text>
  <text x="{x + 14}" y="{y + 38}" font-family="sans-serif" font-size="9" fill="{MOCKUP_SUBTEXT}">{ntype}</text>
"""

    body = f"""  <defs>
    <marker id="bp-arrow" markerWidth="7" markerHeight="7" refX="6" refY="3.5" orient="auto">
      <path d="M0,0 L7,3.5 L0,7 Z" fill="{MOCKUP_CYAN}" fill-opacity="0.7"/>
    </marker>
  </defs>

  <!-- Header -->
  <circle cx="36" cy="64" r="10" fill="{MOCKUP_ORANGE}" opacity="0.85"/>
  <text x="54" y="60" font-family="sans-serif" font-size="16" font-weight="bold" fill="{MOCKUP_TEXT}">Blueprint Editor</text>
  <text x="54" y="76" font-family="sans-serif" font-size="10" fill="{MOCKUP_SUBTEXT}">MyApp flow · drag nodes, connect edges</text>
  <text x="920" y="68" text-anchor="end" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">← Back</text>

  <!-- Toolbar -->
  <rect x="20" y="90" width="920" height="36" rx="8" fill="{MOCKUP_CARD}"/>
  <rect x="28" y="96" width="78" height="24" rx="5" fill="{MOCKUP_GREEN}"/>
  <text x="67" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" font-weight="bold" fill="#fff">+ Add node</text>
  <rect x="114" y="96" width="52" height="24" rx="5" fill="none" stroke="{MOCKUP_RED}"/>
  <text x="140" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_RED}">Delete</text>
  <rect x="174" y="96" width="58" height="24" rx="5" fill="none" stroke="{MOCKUP_CYAN}"/>
  <text x="203" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_CYAN}">Connect</text>
  <line x1="244" y1="100" x2="244" y2="116" stroke="{MOCKUP_DIVIDER}"/>
  <rect x="256" y="96" width="44" height="24" rx="5" fill="none" stroke="{MOCKUP_DIVIDER}"/>
  <text x="278" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_TEXT}">Save</text>
  <rect x="306" y="96" width="44" height="24" rx="5" fill="none" stroke="{MOCKUP_DIVIDER}"/>
  <text x="328" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_TEXT}">Load</text>
  <rect x="356" y="96" width="58" height="24" rx="5" fill="none" stroke="{MOCKUP_ORANGE}"/>
  <text x="385" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_ORANGE}">Validate</text>
  <rect x="422" y="96" width="44" height="24" rx="5" fill="none" stroke="{MOCKUP_FLAMINGO}"/>
  <text x="444" y="112" text-anchor="middle" font-family="sans-serif" font-size="10" fill="{MOCKUP_FLAMINGO}">JSON</text>
  <text x="920" y="112" text-anchor="end" font-family="sans-serif" font-size="10" fill="{MOCKUP_MUTED}">5 nodes · 4 edges</text>

  <!-- Palette -->
  <rect x="20" y="136" width="148" height="360" rx="10" fill="{MOCKUP_CARD}"/>
  <text x="32" y="156" font-family="sans-serif" font-size="11" font-weight="bold" fill="{MOCKUP_TEXT}">Palette</text>
  <text x="32" y="170" font-family="sans-serif" font-size="8" fill="{MOCKUP_MUTED}">Click to add to canvas</text>
{palette_rows}
  <text x="32" y="480" font-family="sans-serif" font-size="8" fill="{MOCKUP_MUTED}">imnodes · v1.1</text>

  <!-- Canvas -->
  <rect x="180" y="136" width="520" height="360" rx="10" fill="{MOCKUP_SURFACE}" stroke="{MOCKUP_DIVIDER}"/>
  <circle cx="240" cy="200" r="1.2" fill="{MOCKUP_DIVIDER}"/><circle cx="280" cy="200" r="1.2" fill="{MOCKUP_DIVIDER}"/>
  <circle cx="320" cy="200" r="1.2" fill="{MOCKUP_DIVIDER}"/><circle cx="360" cy="200" r="1.2" fill="{MOCKUP_DIVIDER}"/>
  <circle cx="400" cy="240" r="1.2" fill="{MOCKUP_DIVIDER}"/><circle cx="440" cy="280" r="1.2" fill="{MOCKUP_DIVIDER}"/>
  <circle cx="480" cy="320" r="1.2" fill="{MOCKUP_DIVIDER}"/><circle cx="520" cy="360" r="1.2" fill="{MOCKUP_DIVIDER}"/>
  <circle cx="560" cy="200" r="1.2" fill="{MOCKUP_DIVIDER}"/><circle cx="600" cy="280" r="1.2" fill="{MOCKUP_DIVIDER}"/>

  <path d="M368,206 C394,206 394,166 420,166" fill="none" stroke="{MOCKUP_CYAN}" stroke-width="1.8" stroke-opacity="0.7" marker-end="url(#bp-arrow)"/>
  <path d="M368,206 C394,206 394,266 420,266" fill="none" stroke="{MOCKUP_CYAN}" stroke-width="1.8" stroke-opacity="0.7" marker-end="url(#bp-arrow)"/>
  <path d="M568,166 C594,166 594,206 620,206" fill="none" stroke="{MOCKUP_CYAN}" stroke-width="1.8" stroke-opacity="0.7" marker-end="url(#bp-arrow)"/>
  <path d="M494,292 C494,320 494,360 494,360" fill="none" stroke="{MOCKUP_CYAN}" stroke-width="1.8" stroke-opacity="0.7" marker-end="url(#bp-arrow)"/>
{node_rects}
  <text x="196" y="484" font-family="sans-serif" font-size="8" fill="{MOCKUP_MUTED}">Compose canvas mock · CUSTOMIZE: imnodes</text>

  <!-- Inspector -->
  <rect x="712" y="136" width="228" height="360" rx="10" fill="{MOCKUP_CARD}"/>
  <text x="726" y="158" font-family="sans-serif" font-size="12" font-weight="bold" fill="{MOCKUP_TEXT}">Inspector</text>
  <rect x="726" y="168" width="64" height="18" rx="3" fill="{MOCKUP_CYAN}" fill-opacity="0.15"/>
  <text x="734" y="181" font-family="sans-serif" font-size="9" fill="{MOCKUP_CYAN}">5 nodes</text>
  <rect x="798" y="168" width="64" height="18" rx="3" fill="{MOCKUP_ORANGE}" fill-opacity="0.15"/>
  <text x="806" y="181" font-family="sans-serif" font-size="9" fill="{MOCKUP_ORANGE}">4 edges</text>
  <rect x="726" y="198" width="200" height="72" rx="6" fill="{MOCKUP_ORANGE}" fill-opacity="0.12"/>
  <text x="738" y="220" font-family="sans-serif" font-size="12" font-weight="bold" fill="{MOCKUP_TEXT}">ui-main</text>
  <circle cx="744" cy="236" r="4" fill="{MOCKUP_ORANGE}"/>
  <text x="754" y="239" font-family="sans-serif" font-size="10" fill="{MOCKUP_SUBTEXT}">ui.page</text>
  <text x="738" y="258" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">data keys: source, widgets…</text>
  <rect x="726" y="282" width="200" height="28" rx="5" fill="none" stroke="{MOCKUP_CYAN}"/>
  <text x="738" y="300" font-family="sans-serif" font-size="10" fill="{MOCKUP_SUBTEXT}">Node id: ui-main</text>
  <text x="726" y="330" font-family="sans-serif" font-size="11" font-weight="bold" fill="{MOCKUP_TEXT}">Edges</text>
  <rect x="726" y="340" width="200" height="28" rx="4" fill="{MOCKUP_SURFACE}"/>
  <text x="736" y="358" font-family="sans-serif" font-size="10" fill="{MOCKUP_TEXT}">py-helpers → cpp-model</text>
  <rect x="726" y="374" width="200" height="28" rx="4" fill="{MOCKUP_SURFACE}"/>
  <text x="736" y="392" font-family="sans-serif" font-size="10" fill="{MOCKUP_TEXT}">cpp-ctrl → ui-main</text>
  <rect x="726" y="408" width="200" height="28" rx="4" fill="{MOCKUP_SURFACE}"/>
  <text x="736" y="426" font-family="sans-serif" font-size="10" fill="{MOCKUP_TEXT}">ui-main → lua-panels</text>

  <!-- Status -->
  <rect x="20" y="508" width="920" height="28" rx="6" fill="{MOCKUP_SURFACE}"/>
  <text x="32" y="526" font-family="sans-serif" font-size="10" fill="{MOCKUP_SUBTEXT}">Ready — select a node or add from the palette</text>
  <text x="480" y="556" text-anchor="middle" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">Compose: BlueprintEditorView.kt · regenerate via generate-diagrams.py --mockups</text>"""
    return _mockup_frame("Blueprint Editor", body, w=w, h=h)


def flows_editor_mockup() -> str:
    flows = [
        (60, 110, MOCKUP_GREEN, "Data Refresh", "background", "interval: 5000ms", "ON"),
        (60, 170, MOCKUP_ORANGE, "Alert Handler", "triggered", "event: sensor.alert", "ON"),
        (60, 230, MOCKUP_PURPLE, "Init Pipeline", "startup", "runs once at app launch", "OFF"),
        (60, 290, MOCKUP_CYAN, "Manual Export", "manual", "hotkey: Ctrl+E", "ON"),
    ]
    cards = ""
    for _i, (x, y, color, name, mode, detail, state) in enumerate(flows):
        toggle = f"""  <rect x="{x + 400}" y="{y + 10}" width="36" height="20" rx="10" fill="{MOCKUP_GREEN if state == 'ON' else MOCKUP_CARD}" stroke="{MOCKUP_GREEN if state == 'ON' else MOCKUP_SUBTEXT}"/>
  <circle cx="{x + 410 if state == 'ON' else x + 426}" cy="{y + 20}" r="7" fill="#ffffff"/>"""
        cards += f"""  <rect x="{x}" y="{y}" width="480" height="40" rx="8" fill="{MOCKUP_CARD}"/>
  <circle cx="{x + 16}" cy="{y + 20}" r="5" fill="{color}"/>
  <text x="{x + 30}" y="{y + 24}" font-family="sans-serif" font-size="13" font-weight="bold" fill="{MOCKUP_TEXT}">{name}</text>
  <text x="{x + 140}" y="{y + 24}" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">{mode}</text>
  <text x="{x + 260}" y="{y + 24}" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">{detail}</text>
{toggle}"""

    body = f"""  <text x="60" y="90" font-family="sans-serif" font-size="16" font-weight="bold" fill="{MOCKUP_TEXT}">Flows Editor</text>
  <rect x="60" y="110" width="480" height="220" rx="10" fill="none" stroke="{MOCKUP_ORANGE}" stroke-width="1"/>
{cards}
  <rect x="60" y="360" width="140" height="36" rx="8" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="130" y="383" text-anchor="middle" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">Reload template</text>

  <rect x="220" y="360" width="120" height="36" rx="8" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="280" y="383" text-anchor="middle" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">Preview JSON</text>

  <text x="60" y="440" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">All imported flows default to disabled (enabled: false). Toggle each flow to opt in.</text>"""
    return _mockup_frame("Flows Editor", body, w=800, h=520)


def loading_mockup() -> str:
    """Splash matching LoadingScreen — flamingo + progress steps."""
    steps = ["Resolve JDK toolchain", "Load templates", "Warm Compose runtime", "Ready"]
    step_rows = ""
    for i, label in enumerate(steps):
        y = 320 + i * 36
        done = i < 2
        active = i == 2
        color = MOCKUP_GREEN if done else (MOCKUP_PURPLE if active else MOCKUP_DIVIDER)
        step_rows += f"""  <circle cx="320" cy="{y}" r="7" fill="{color}"/>
  <text x="340" y="{y + 4}" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT if active or done else MOCKUP_MUTED}">{label}</text>
"""
    body = f"""  <circle cx="400" cy="140" r="48" fill="{MOCKUP_FLAMINGO}" opacity="0.9"/>
  <text x="400" y="220" text-anchor="middle" font-family="sans-serif" font-size="22" font-weight="bold" fill="{MOCKUP_TEXT}">The Nexus Framework</text>
  <text x="400" y="244" text-anchor="middle" font-family="sans-serif" font-size="13" fill="{MOCKUP_SUBTEXT}">v1.0.2</text>
  <rect x="220" y="270" width="360" height="10" rx="5" fill="{MOCKUP_DIVIDER}"/>
  <rect x="220" y="270" width="216" height="10" rx="5" fill="{MOCKUP_PURPLE}"/>
{step_rows}
  <text x="400" y="500" text-anchor="middle" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">Compose: LoadingScreen.kt</text>"""
    return _mockup_frame("Loading", body, w=800, h=540)


def debugger_mockup() -> str:
    """Debugger panel — paste scan, filters, match list."""
    matches = ""
    for i, (sev, cat, line) in enumerate([
        ("HIGH", "secret", 'password = "hunter2"'),
        ("MED", "todo", "TODO: wire persistence"),
        ("LOW", "debug", 'println("debug trace")'),
    ]):
        y = 280 + i * 52
        sev_c = MOCKUP_RED if sev == "HIGH" else (MOCKUP_ORANGE if sev == "MED" else MOCKUP_CYAN)
        matches += f"""  <rect x="40" y="{y}" width="720" height="44" rx="8" fill="{MOCKUP_CARD}"/>
  <rect x="52" y="{y + 12}" width="44" height="20" rx="4" fill="{sev_c}" fill-opacity="0.2"/>
  <text x="60" y="{y + 26}" font-family="sans-serif" font-size="10" fill="{sev_c}">{sev}</text>
  <text x="110" y="{y + 26}" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">{cat}</text>
  <text x="200" y="{y + 26}" font-family="monospace" font-size="11" fill="{MOCKUP_TEXT}">{line}</text>
"""
    body = f"""  <text x="40" y="80" font-family="sans-serif" font-size="18" font-weight="bold" fill="{MOCKUP_TEXT}">Debugger</text>
  <text x="760" y="80" text-anchor="end" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">3 match(es) · Enabled · ← Back</text>
  <text x="40" y="110" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">Paste log or source to scan</text>
  <rect x="40" y="120" width="720" height="72" rx="8" fill="{MOCKUP_CARD}" stroke="{MOCKUP_DIVIDER}"/>
  <text x="56" y="150" font-family="monospace" font-size="11" fill="{MOCKUP_MUTED}">println("debug"); TODO(); password = "secret" …</text>
  <rect x="40" y="208" width="88" height="28" rx="6" fill="{MOCKUP_CYAN}"/>
  <text x="84" y="226" text-anchor="middle" font-family="sans-serif" font-size="11" font-weight="bold" fill="{MOCKUP_BG}">Scan</text>
  <rect x="140" y="208" width="72" height="28" rx="6" fill="none" stroke="{MOCKUP_DIVIDER}"/>
  <text x="176" y="226" text-anchor="middle" font-family="sans-serif" font-size="11" fill="{MOCKUP_TEXT}">Clear</text>
  <text x="40" y="262" font-family="sans-serif" font-size="12" font-weight="bold" fill="{MOCKUP_TEXT}">Matches</text>
{matches}
  <text x="40" y="470" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">Compose: DebuggerPanel.kt · asset mockup-debugger-v102.svg (avoids root-owned mockup-debugger.svg)</text>"""
    return _mockup_frame("Debugger", body, w=800, h=500)


def test_runner_mockup() -> str:
    """In-memory test runner panel."""
    rows = ""
    for i, (name, status, color) in enumerate([
        ("blueprint_sample_valid", "PASS", MOCKUP_GREEN),
        ("flows_default_disabled", "PASS", MOCKUP_GREEN),
        ("template_vars_render", "FAIL", MOCKUP_RED),
        ("nxs_config_schema_v2", "PASS", MOCKUP_GREEN),
    ]):
        y = 200 + i * 48
        rows += f"""  <rect x="40" y="{y}" width="720" height="40" rx="8" fill="{MOCKUP_CARD}"/>
  <circle cx="64" cy="{y + 20}" r="6" fill="{color}"/>
  <text x="84" y="{y + 24}" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT}">{name}</text>
  <text x="720" y="{y + 24}" text-anchor="end" font-family="sans-serif" font-size="11" font-weight="bold" fill="{color}">{status}</text>
"""
    body = f"""  <text x="40" y="80" font-family="sans-serif" font-size="18" font-weight="bold" fill="{MOCKUP_TEXT}">Test Runner</text>
  <text x="760" y="80" text-anchor="end" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">← Back</text>
  <rect x="40" y="100" width="720" height="64" rx="12" fill="{MOCKUP_CARD}"/>
  <text x="60" y="128" font-family="sans-serif" font-size="14" font-weight="bold" fill="{MOCKUP_TEXT}">3 / 4 passed</text>
  <text x="60" y="148" font-family="sans-serif" font-size="11" fill="{MOCKUP_SUBTEXT}">In-memory unitary checks — not device instrumentation</text>
  <rect x="520" y="118" width="100" height="32" rx="8" fill="{MOCKUP_CYAN}"/>
  <text x="570" y="138" text-anchor="middle" font-family="sans-serif" font-size="12" font-weight="bold" fill="{MOCKUP_BG}">Run all</text>
  <rect x="640" y="118" width="100" height="32" rx="8" fill="none" stroke="{MOCKUP_DIVIDER}"/>
  <text x="690" y="138" text-anchor="middle" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT}">Clear</text>
{rows}
  <text x="40" y="420" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">Compose: TestRunnerPanel.kt</text>"""
    return _mockup_frame("Test Runner", body, w=800, h=460)


def whats_new_mockup() -> str:
    """What's New modal overlay."""
    body = f"""  <rect width="100%" height="100%" fill="#000000" fill-opacity="0.65"/>
  <rect x="170" y="80" width="460" height="360" rx="20" fill="{MOCKUP_CARD}"/>
  <text x="400" y="130" text-anchor="middle" font-family="sans-serif" font-size="20" font-weight="bold" fill="{MOCKUP_TEXT}">What's New</text>
  <text x="400" y="158" text-anchor="middle" font-family="sans-serif" font-size="14" fill="{MOCKUP_PURPLE}">Version 1.0.2</text>
  <circle cx="400" cy="210" r="28" fill="{MOCKUP_FLAMINGO}" opacity="0.9"/>
  <rect x="210" y="260" width="380" height="1" fill="{MOCKUP_DIVIDER}"/>
  <text x="230" y="290" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT}">• Home hub + flamingo branding</text>
  <text x="230" y="314" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT}">• Blueprint &amp; Flows Compose editors</text>
  <text x="230" y="338" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT}">• Zig 0.16.0 native sidecars</text>
  <rect x="300" y="370" width="200" height="36" rx="8" fill="{MOCKUP_PURPLE}" fill-opacity="0.45"/>
  <text x="400" y="392" text-anchor="middle" font-family="sans-serif" font-size="12" fill="{MOCKUP_TEXT}">Continue (5s)</text>
  <text x="400" y="460" text-anchor="middle" font-family="sans-serif" font-size="9" fill="{MOCKUP_MUTED}">Compose: WhatsNewDialog.kt</text>"""
    return _mockup_frame("What's New", body, w=800, h=500)


def _diagram_outputs() -> dict:
    return {
        DIAGRAMS / "full-stack-architecture.svg": full_stack_architecture(),
        DIAGRAMS / "generation-builds-flow.svg": generation_builds_flow(),
        DIAGRAMS / "desktop-vs-android-runtime.svg": desktop_vs_android(),
        DIAGRAMS / "langflow-vs-n8n-blueprint.svg": langflow_vs_n8n(),
        DIAGRAMS / "python-desktop-vs-android-flow.svg": python_desktop_vs_android_flow(),
        DIAGRAMS / "tsxhtml-lowering-pipeline.svg": tsxhtml_lowering_pipeline(),
        DIAGRAMS / "blueprint-vs-flows-layers.svg": blueprint_vs_flows_layers(),
        DIAGRAMS / "langflow-adoption-workflow.svg": langflow_adoption_workflow(),
        DIAGRAMS / "zig-orchestration-layer.svg": zig_orchestration_layer(),
        DIAGRAMS / "cmake-to-zig-migration.svg": cmake_to_zig_migration(),
        DIAGRAMS / "langflow-import-pipeline.svg": langflow_import_pipeline(),
        # UML activity diagrams (see docs/assets/diagrams/activity-diagrams.md)
        DIAGRAMS / "activity-first-run-bootstrap.svg": activity_first_run_bootstrap(),
        DIAGRAMS / "activity-client-navigation.svg": activity_client_navigation(),
        DIAGRAMS / "activity-generate-pipeline.svg": activity_generate_pipeline(),
        DIAGRAMS / "activity-build-desktop-app.svg": activity_build_desktop_app(),
        DIAGRAMS / "activity-desktop-frame-loop.svg": activity_desktop_frame_loop(),
        DIAGRAMS / "activity-hello-counter.svg": activity_hello_counter(),
        DIAGRAMS / "activity-flows-automation.svg": activity_flows_automation(),
        DIAGRAMS / "activity-langflow-import.svg": activity_langflow_import(),
        DIAGRAMS / "activity-android-field-tablet.svg": activity_android_field_tablet(),
        EXAMPLES / "langflow-rag-chatbot.svg": langflow_rag_chatbot(),
        EXAMPLES / "langflow-agent-tools.svg": langflow_agent_tools(),
        EXAMPLES / "nexus-blueprint-app-structure.svg": nexus_blueprint_app_structure(),
    }


def _mockup_outputs() -> dict:
    # Home hub is HomeScreen (Welcome/Dashboard alias Home in App.kt).
    # mockup-debugger-v102.svg avoids root-owned mockup-debugger.svg PermissionError.
    return {
        EXAMPLES / "mockup-welcome.svg": welcome_dashboard_mockup(),
        EXAMPLES / "mockup-loading.svg": loading_mockup(),
        EXAMPLES / "mockup-generate-project.svg": generate_project_mockup(),
        EXAMPLES / "mockup-blueprint-editor.svg": blueprint_editor_mockup(),
        EXAMPLES / "mockup-flows-editor.svg": flows_editor_mockup(),
        EXAMPLES / "mockup-debugger-v102.svg": debugger_mockup(),
        EXAMPLES / "mockup-test-runner.svg": test_runner_mockup(),
        EXAMPLES / "mockup-whats-new.svg": whats_new_mockup(),
    }


def main() -> None:
    import argparse

    parser = argparse.ArgumentParser(
        description="Generate Nexus docs SVG diagrams and Compose UI mockups.",
    )
    parser.add_argument(
        "--mockups",
        action="store_true",
        help="Write only interface mockups under docs/assets/examples/ "
             "(skip architecture + activity diagrams).",
    )
    parser.add_argument(
        "--diagrams",
        action="store_true",
        help="Write only architecture/activity/example diagrams (skip mockups).",
    )
    args = parser.parse_args()

    if args.mockups and not args.diagrams:
        outputs = _mockup_outputs()
    elif args.diagrams and not args.mockups:
        outputs = _diagram_outputs()
    else:
        outputs = {**_diagram_outputs(), **_mockup_outputs()}

    for out_path, content in outputs.items():
        _write_svg(out_path, content)


def _is_writable_target(path: Path) -> bool:
    """True if we can create or overwrite *path* without elevated privileges."""
    if path.exists():
        return os.access(path, os.W_OK)
    parent = path.parent
    return parent.exists() and os.access(parent, os.W_OK)


def _alternate_generated_path(path: Path) -> Path:
    """User-writable fallback: foo.svg → foo-generated.svg (no sudo)."""
    return path.with_name(f"{path.stem}-generated{path.suffix}")


def _try_write_svg(target: Path, content: str) -> bool:
    """Write *content* to *target*. Return False on permission denial."""
    try:
        target.write_text(content, encoding="utf-8")
        return True
    except PermissionError:
        return False
    except OSError as err:
        if getattr(err, "errno", None) in (13, 1):  # EACCES, EPERM
            return False
        raise


def _write_svg(out_path: Path, content: str) -> None:
    """Write SVG; if not writable, try *-generated.svg, else skip with warning."""
    out_path.parent.mkdir(parents=True, exist_ok=True)

    if _is_writable_target(out_path) and _try_write_svg(out_path, content):
        size_kb = out_path.stat().st_size / 1024
        print(f"Wrote {out_path.relative_to(ROOT)} ({size_kb:.1f} KB)")
        return

    alt = _alternate_generated_path(out_path)
    if out_path.exists() and not _is_writable_target(out_path):
        print(
            f"Warning: {out_path.relative_to(ROOT)} is not writable "
            f"(often root-owned); trying {alt.relative_to(ROOT)}"
        )
    elif not _try_write_svg(out_path, content):
        print(
            f"Warning: could not write {out_path.relative_to(ROOT)}; "
            f"trying {alt.relative_to(ROOT)}"
        )

    if _try_write_svg(alt, content):
        size_kb = alt.stat().st_size / 1024
        print(
            f"Wrote {alt.relative_to(ROOT)} ({size_kb:.1f} KB) "
            f"(fallback; primary {out_path.name} not writable)"
        )
        return

    print(
        f"Warning: skipped {out_path.relative_to(ROOT)} — not writable. "
        "Fix ownership (e.g. chown) or remove the root-owned file; "
        "this script does not require sudo."
    )


if __name__ == "__main__":
    main()
