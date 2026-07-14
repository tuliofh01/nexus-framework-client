//==============================================================================
// nxs.desktop.plot — Plot Controller (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Coordinates the chart: responds to user input (add/remove function, zoom,
// sample count), triggers Python re-evaluation, and keeps the model's curve
// caches fresh. This is the controller for the plotter view (ImPlot-based).
//
// == MVC FLOW ==
//   User clicks "Add Sine" ──► PlotController::addFunction()
//       ──► FunctionRegistry::activate() ──► marks series dirty
//       ──► PlotController::refresh() ──► PythonEngine::evaluate()
//       ──► fills xs/ys in PlotSeries ──► view renders via ImPlot
//
// The controller owns ChartSettings (plot window, range, grid toggle) and
// exposes them as mutable references so the view can bind ImGui/ImPlot widgets
// directly to them.
//==============================================================================

module;  // global module fragment

// ── Standard library ──
#include <algorithm>
#include <string>

export module nxs.desktop.plot;

// ── Import peer modules ──
import nxs.desktop.func;
import nxs.desktop.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Chart Controller
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::controller {

/// Per-chart settings that the user controls via sliders and checkboxes
/// in the view. Defaults produce a reasonable -10..+10 window with 512
/// sample points per curve.
export struct ChartSettings {
    double xMin = -10.0;
    double xMax = 10.0;
    int sampleCount = 512;   // per curve; slider range 64..4096 in the view
    bool logScaleY = false;  // linear/log toggle for the Y axis
    bool showGrid = true;
};

/// Responds to user chart commands and drives Python evaluation. Owned
/// by main(); the view and LuaPanels both reference it.
export class PlotController {
public:
    PlotController(model::FunctionRegistry& registry, PythonEngine& python)
        : m_registry(registry), m_python(python) {}

    // ── Curve management ──────────────────────────────────────────────

    /// Activate a function by spec id. The new series starts dirty and
    /// gets sampled on the next refresh() call.
    void addFunction(const std::string& specId) {
        m_registry.activate(specId);
    }

    /// Deactivate and remove a function from the active set.
    void removeFunction(const std::string& specId) {
        m_registry.deactivate(specId);
    }

    // ── Plot window ───────────────────────────────────────────────────

    /// Update the visible X range. Degenerate ranges (min >= max) are
    /// silently ignored to avoid half-typed input issues.
    void setRange(double xMin, double xMax) {
        if (xMin >= xMax) return;
        m_settings.xMin = xMin;
        m_settings.xMax = xMax;
        m_registry.invalidateAll();
    }

    /// Set the number of sample points per curve. Clamped to [64, 4096].
    void setSampleCount(int samples) {
        samples = std::clamp(samples, 64, 4096);
        if (samples == m_settings.sampleCount) return;
        m_settings.sampleCount = samples;
        m_registry.invalidateAll();
    }

    // ── Per-frame refresh ─────────────────────────────────────────────

    /// Re-evaluate every dirty series through Python. Called once per
    /// frame before drawing; cheap when nothing changed. On evaluation
    /// failure, the broken series is cleared and the error is surfaced
    /// via the view's error bar.
    void refresh() {
        for (auto& series : m_registry.active()) {
            if (!series.dirty) continue;
            const bool ok = m_python.evaluate(
                series.spec.pythonName,
                m_settings.xMin, m_settings.xMax,
                m_settings.sampleCount, series.xs, series.ys);
            // Even on failure, clear the dirty flag so a broken script
            // does not spam the interpreter every frame.
            series.dirty = false;
            if (!ok) {
                series.xs.clear();
                series.ys.clear();
            }
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────

    ChartSettings& settings() { return m_settings; }
    model::FunctionRegistry& registry() { return m_registry; }
    const std::string& lastPythonError() const {
        return m_python.lastError();
    }

private:
    model::FunctionRegistry& m_registry;
    PythonEngine& m_python;
    ChartSettings m_settings;
};

}  // namespace nxs::controller
