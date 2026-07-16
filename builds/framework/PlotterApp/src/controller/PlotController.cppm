//==============================================================================
// nxs.desktop.plot — Plot Controller (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Coordinates the chart: responds to user input (add/remove function, zoom,
// sample count), triggers Python re-evaluation, and keeps the model's curve
// caches fresh. This is the controller for the plotter view (ImPlot-based).
//
// ════════════════════════════════════════════════════════════════════════════
// MVC FLOW
// ════════════════════════════════════════════════════════════════════════════
//
//   User clicks "Add Sine" ──► PlotController::addFunction()
//       ──► FunctionRegistry::activate() ──► marks series dirty
//       ──► PlotController::refresh() ──► PythonEngine::evaluate()
//       ──► fills xs/ys in PlotSeries ──► view renders via ImPlot
//
// The controller owns ChartSettings (plot window, range, grid toggle) and
// exposes them as mutable references so the view can bind ImGui/ImPlot widgets
// directly to them.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit            — prevents implicit conversion from references
// noexcept            — constructor and simple accessors guarantee no throws
// = delete            — Rule of Five: non-copyable, non-movable
// [[nodiscard]]       — return values must be checked (no ignored errors)
// constexpr           — setRange is constexpr (no side effects on member refs)
//                       setSampleCount uses std::clamp (constexpr in C++20)
// trailing return    — auto fn() -> Type for uniform declaration syntax
// {} brace-init      — uniform initialization for all members
// auto&              — deduced reference type in range-for
// const auto&        — const reference in range-for (iteration without copy)
// std::clamp         — safe numeric clamping [lo, hi]
// std::string_view   — non-owning constexpr-capable string params
// std::ranges        — (imported via <algorithm>)
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Standard library (private to this module) ──
#include <algorithm>    // std::clamp for sample count bounds
#include <string>       // std::string for error messages
#include <string_view>  // std::string_view for function spec IDs

export module nxs.desktop.plot;

// ── Import peer modules ──
//
// The plot controller orchestrates between:
//   - FunctionRegistry (model): stores function specs + active series
//   - PythonEngine (service): evaluates curves via embedded CPython
import nxs.desktop.func;
import nxs.desktop.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Chart Controller
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::controller {

/// Per-chart settings that the user controls via sliders and checkboxes
/// in the view.
///
/// Uses {} brace-initialization for defaults: -10..+10 range, 512 samples,
/// linear Y axis, grid on. No uninitialized members.
export struct ChartSettings {
    double xMin{-10.0};           ///< Visible X-axis minimum
    double xMax{10.0};            ///< Visible X-axis maximum
    int sampleCount{512};         ///< Points per curve (slider: 64..4096)
    bool logScaleY{false};        ///< Linear/log toggle for Y axis
    bool showGrid{true};          ///< Grid overlay toggle
};

/// Responds to user chart commands and drives Python evaluation. Owned
/// by main(); the view and LuaPanels both reference it.
///
/// RAII: owns ChartSettings by value; references to registry and python
/// are non-owning (created in main() before controller).
export class PlotController {
public:
    /// Store references to the model and Python engine. Both outlive the
    /// controller because they are created in main().
    ///
    /// explicit: prevents implicit conversion from FunctionRegistry+PythonEngine.
    /// noexcept: guarantees no exceptions from construction.
    explicit PlotController(model::FunctionRegistry& registry,
                            PythonEngine& python) noexcept
        : m_registry{registry}, m_python{python} {}  // {} brace-init

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Non-copyable: holds references to registry and python engine.
    // Copying would create dangling-reference hazards.

    PlotController(const PlotController&) = delete;
    PlotController& operator=(const PlotController&) = delete;
    PlotController(PlotController&&) = delete;
    PlotController& operator=(PlotController&&) = delete;

    ~PlotController() = default;

    // ── Curve management ──────────────────────────────────────────────

    /// Activate a function by spec ID. The new series starts dirty and
    /// gets sampled on the next refresh() call.
    ///
    /// std::string_view: non-owning reference for the spec ID — avoids
    /// heap allocation for a read-only lookup key.
    void addFunction(const std::string_view specId) {
        m_registry.activate(specId);
    }

    /// Deactivate and remove a function from the active set.
    void removeFunction(const std::string_view specId) {
        m_registry.deactivate(specId);
    }

    // ── Plot window ───────────────────────────────────────────────────

    /// Update the visible X range.
    ///
    /// constexpr: this function has no side effects beyond member mutation
    /// and can be evaluated at compile time if called with constexpr args.
    /// noexcept: no exceptions are possible (simple assignment).
    ///
    /// Degenerate ranges (min >= max) are silently ignored to avoid
    /// half-typed input issues.
    constexpr void setRange(double xMin, double xMax) noexcept {
        if (xMin >= xMax) return;
        m_settings.xMin = xMin;
        m_settings.xMax = xMax;
        m_registry.invalidateAll();
    }

    /// Set the number of sample points per curve.
    ///
    /// std::clamp: constexpr in C++20 — bounds the value to [64, 4096].
    /// If the value hasn't changed (early return), we skip invalidation
    /// to avoid unnecessary Python re-evaluations.
    void setSampleCount(int samples) {
        samples = std::clamp(samples, 64, 4096);
        if (samples == m_settings.sampleCount) return;  // no change
        m_settings.sampleCount = samples;
        m_registry.invalidateAll();
    }

    // ── Per-frame refresh ─────────────────────────────────────────────

    /// Re-evaluate every dirty series through Python.
    ///
    /// Called once per frame before drawing; cheap when nothing changed.
    /// On evaluation failure, the broken series is cleared and the error
    /// is surfaced via the view's error bar.
    ///
    /// Design note: we clear the dirty flag even on failure so a broken
    /// script does not spam the interpreter every frame. The error bar
    /// in the view shows the last error.
    void refresh() {
        // Range-for with auto&: we need mutable access to series.xs/.ys
        for (auto& series : m_registry.active()) {
            if (!series.dirty) continue;

            const auto ok = m_python.evaluate(
                series.spec.pythonName,
                m_settings.xMin, m_settings.xMax,
                m_settings.sampleCount, series.xs, series.ys);

            // Always clear dirty — even on failure — to prevent
            // infinite error loops.
            series.dirty = false;

            if (!ok) {
                series.xs.clear();
                series.ys.clear();
            }
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────

    /// Mutable access to chart settings for ImGui/ImPlot widget binding.
    [[nodiscard]] auto settings() noexcept -> ChartSettings& {
        return m_settings;
    }

    /// Const access to chart settings (used by const-ref code paths).
    [[nodiscard]] auto settings() const noexcept -> const ChartSettings& {
        return m_settings;
    }

    /// Access to the function registry for view-level iteration.
    [[nodiscard]] auto registry() noexcept -> model::FunctionRegistry& {
        return m_registry;
    }

    /// Forward the Python engine's last error to the view.
    [[nodiscard]] auto lastPythonError() const noexcept
        -> const std::string& {
        return m_python.lastError();
    }

private:
    model::FunctionRegistry& m_registry;  ///< Non-owning: created in main()
    PythonEngine& m_python;               ///< Non-owning: created in main()
    ChartSettings m_settings;             ///< Owned by value
};

}  // namespace nxs::controller
