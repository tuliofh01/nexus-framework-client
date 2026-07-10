#!/usr/bin/env python3
"""Generate styled architecture/example SVG diagrams for docs/assets/."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
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

    scr_x = gen_x + gen_w + GAP_H
    scr_svg, scr_boxes, scr_end = hstack(scr_x + 20, gen_y + 40, [
        (180, 72, "#FFFFFF", "#2E7D32", NF["terminal"], "Lua 5.4 + sol2", "Embeds Lua runtime for scripting panels"),
        (180, 72, "#FFFFFF", "#2E7D32", NF["file"], "TS/XHTML DSL", "Declarative UI markup compiled to ImGui"),
    ])
    scr_w = scr_end - scr_x + 24
    scr_h = max(b["h"] for b in scr_boxes) + 60

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

{layer_box(scr_x, gen_y, scr_w, scr_h, "#E8F5E9", "#2E7D32", "Scripting &amp; UI", NF["code"])}
{scr_svg}

{layer_box(gen_x, rt_y, rt_w, rt_h, "#ECEFF1", "#455A64", "Runtime", NF["desktop"])}
{rt_svg}

{chr(10).join(arrows)}

{legend_box(scr_x, legend_y, scr_w, 160, [
    ("#E8F4FD", "#1565C0", "Client — Compose scaffold UI"),
    ("#FFF8E1", "#F57F17", "Authoring — blueprint.json graph"),
    ("#F3E5F5", "#6A1B9A", "Generated — C++ MVC output"),
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
            ("Run misc/client-setup/", "Installs JDK 26 and Git before first build", "#E8F5E9", "#2E7D32", NF["wrench"]),
            ("source env.sh", "Activates toolchain for Gradle and CMake", "#E8F5E9", "#2E7D32", NF["terminal"]),
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
            ("cmake --preset debug", "Configure CMake + Ninja build files", "#E3F2FD", "#1565C0", NF["desktop"]),
            ("cmake --build", "Compile C++ sources and link binary", "#E3F2FD", "#1565C0", NF["gear"]),
            ("Run native binary", "SDL3 desktop app with pybind11", "#E3F2FD", "#1565C0", NF["rocket"]),
        ],
        [
            ("assembleDebug", "Gradle builds Android APK via NDK", "#FCE4EC", "#C2185B", NF["android"]),
            ("Djinni + Chaquopy", "JNI bridge and embedded Python", "#FCE4EC", "#C2185B", NF["plug"]),
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
        arrow_between(c5[1], c5[2]),
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
        (240, 72, "#FFFFFF", "#1565C0", NF["gear"], "CMake + Ninja", "OpenGL 3.3 native build pipeline"),
        (260, 72, "#FFFFFF", "#1565C0", NF["python"], "pybind11 embed", "Embeds CPython via pybind11"),
    ])
    desk_r2, dr2, _ = hstack(desk_x + 20, dr1[0]["y"] + dr1[0]["h"] + GAP_V, [
        (240, 72, "#FFFFFF", "#1565C0", NF["file"], "python/functions.py", "In-process NumPy analytics module"),
        (260, 72, "#FFFFFF", "#1565C0", NF["box"], "Native binary", "Win · macOS · Linux executable"),
    ])
    desk_w = max(dr1[-1]["x"] + dr1[-1]["w"], dr2[-1]["x"] + dr2[-1]["w"]) - desk_x + 24
    desk_h = dr2[0]["y"] + dr2[0]["h"] - desk_y + 24

    and_x = desk_x + desk_w + GAP_H
    and_r1, ar1, _ = hstack(and_x + 20, desk_y + 40, [
        (240, 72, "#FFFFFF", "#C2185B", NF["android"], "Gradle + NDK", "APK build with native C++ libs"),
        (260, 72, "#FFFFFF", "#C2185B", NF["desktop"], "SDL3 GLES", "Full-screen touch UI on GLES"),
    ])
    and_r2, ar2, _ = hstack(and_x + 20, ar1[0]["y"] + ar1[0]["h"] + GAP_V, [
        (240, 72, "#FFFFFF", "#C2185B", NF["plug"], "Djinni IDL", "C++ ↔ Kotlin JNI codegen"),
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
        arrow_between(ui_boxes[1], ar1[0], "bottom", "top", "Android NDK", via_x=ar1[0]["x"] + ar1[0]["w"] // 2),
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

  <rect x="{(desk_x + and_x + and_w) // 2 - 180}" y="{legend_y - 56}" width="360" height="50" class="panel" fill="#E8F5E9" stroke="#2E7D32"/>
  <text x="{(desk_x + and_x + and_w) // 2 - 160}" y="{legend_y - 26}" class="small">Lowest latency: Python runs in-process via pybind11.</text>

{legend_box(32, legend_y, 500, 80, [
    ("#F3E5F5", "#6A1B9A", "Shared — blueprint + MVC + SDL3"),
    ("#E3F2FD", "#1565C0", "Desktop — CMake + pybind11"),
    ("#FCE4EC", "#C2185B", "Android — Gradle + Djinni + Chaquopy"),
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
        (240, 72, "#FFFFFF", "#1565C0", NF["gear"], "CMake: pack_python_dat", "Build step packs PYAC archive"),
    ], gap=gap)
    desk_r2, dr2, _ = hstack(desk_x + 32, dr1[0]["y"] + dr1[0]["h"] + GAP_V, [
        (260, 72, "#FFFFFF", "#1565C0", NF["box"], "misc/python.dat (PYAC)", "Encrypted script pack in misc/"),
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
        (520, 80, "#FFFFFF", "#C2185B", NF["plug"], "ChaquopyPythonBridge (Djinni)", "Type-safe C++ ↔ Kotlin JNI bridge"),
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
    ("#FCE4EC", "#C2185B", "Android — Chaquopy + Djinni"),
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
        (210, 72, "#FFF3E0", "#EF6C00", NF["gear"], "Translate fields", "Manual map to Nexus schema (v1)"),
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
  <text x="{w // 2}" y="{note_y + 40}" text-anchor="middle" class="desc">Automatic Langflow importer planned for v1.1</text>
{legend_box(40, legend_y, w - 80, 90, [
    ("#E8F5E9", "#2E7D32", "External — Langflow authoring"),
    ("#FFF3E0", "#EF6C00", "Translate — manual field mapping"),
    ("#F3E5F5", "#7B1FA2", "Ship — flows.json in project"),
    ("#3949AB", "#3949AB", "Runtime — FlowRunner at startup"),
], "Adoption path")}
</svg>"""


def main() -> None:
    outputs = {
        DIAGRAMS / "full-stack-architecture.svg": full_stack_architecture(),
        DIAGRAMS / "generation-builds-flow.svg": generation_builds_flow(),
        DIAGRAMS / "desktop-vs-android-runtime.svg": desktop_vs_android(),
        DIAGRAMS / "langflow-vs-n8n-blueprint.svg": langflow_vs_n8n(),
        DIAGRAMS / "python-desktop-vs-android-flow.svg": python_desktop_vs_android_flow(),
        DIAGRAMS / "tsxhtml-lowering-pipeline.svg": tsxhtml_lowering_pipeline(),
        DIAGRAMS / "blueprint-vs-flows-layers.svg": blueprint_vs_flows_layers(),
        DIAGRAMS / "langflow-adoption-workflow.svg": langflow_adoption_workflow(),
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
