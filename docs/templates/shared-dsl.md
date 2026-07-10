# Shared TypeScript + XHTML DSL

Web-familiar UI authoring for native ImGui. Sources: `template/shared/dsl/`.

## Concept

XHTML tags are **abstractions depicting foreign components** — each maps to Dear ImGui or ImPlot. TypeScript controllers bind state and events; the toolchain lowers the tree into Lua (`scripts/panels.lua` equivalent).

## Files

| File | Role |
|------|------|
| `core.ts` | `Component` base, styles, callbacks |
| `components.ts` | Widget classes (`Button`, `Slider`, `Plot`, …) |
| `tags.ts` | XHTML tag constants |
| `index.ts` | Public exports |

Example: `template/desktop-app/ui/ui.xhtml` + `ui/ui.ts`.

## Controller pattern

```typescript
export class PlotterPage extends NexusPage {
  pendingFunction = state<string>("sine");
  activeCurves = native<Curve[]>("registry.active");

  addPending(): void {
    this.invoke("nxs.add_function", this.pendingFunction.value);
  }
}
```

- `state()` — two-way `bind=` in XHTML
- `native()` — read-only C++ model projection
- `invoke()` — controller commands via sol2

## Blueprint link

`blueprint.json` references `ui/ui.xhtml` and `ui/ui.ts#PlotterPage` in a `ui.page` node.

## Related

- [Coding with Nexus](../guides/coding-with-nexus.md#build-ui-typescript--xhtml)
- [Desktop template](desktop-app.md)
