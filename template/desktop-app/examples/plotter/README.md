# Optional plotter example

Desmos-style function plotter demonstrating ImPlot, numpy sampling, and plotter-specific flows.

## Build

From the generated project root:

```bash
cmake --preset debug -DBUILD_NEXUS_EXAMPLES=ON
cmake --build --preset debug --target {{projectName}}_plotter
```

## Contents

| Path | Role |
|------|------|
| `src/` | PlotController, PlotterView, FunctionRegistry |
| `python/functions.py` | numpy curve library |
| `ui/` | PlotterPage DSL |
| `scripts/panels.lua` | Quick-add panels and hotkeys |
| `blueprint.json` | Plotter-specific app graph |
| `flows/flows.json` | Resample timer + curve.added trigger |

Delete this folder if you do not need the sample.
