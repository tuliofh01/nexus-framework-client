//==============================================================================
// nxs.desktop.func — Function Catalog & Plot-Series Registry (C++20 Module)
//
// == WHAT THIS MODULE DOES ==
// Owns the catalog of plottable functions (`FunctionSpec`) and the sampled-data
// caches (`PlotSeries`) for every curve currently on the chart. The model layer
// is the single source of truth — the controller fills the caches, the view
// reads them.
//
// == C++20 MODULE CONCEPTS ==
// This is a **module interface unit** (.cppm). The `export` keyword makes
// `FunctionRegistry`, `FunctionSpec`, and `PlotSeries` visible to any
// translation unit that writes `import nxs.desktop.func;`. Everything else
// in this module (the includes in the global fragment, the namespace
// internals) is private.
//
// == MVC ROLE ==
//   Model (this module)  <── mutates  ──  Controller (PlotController)
//        │                                      │
//        └──── reads  ──  View (AppView / ImGui)
//
// The model never talks to Python or ImGui directly. The controller calls
// `activate()` / `deactivate()` / `invalidateAll()`; the view reads
// `available()` and `active()` every frame.
//==============================================================================

module;  // global module fragment — everything before `export module` is private

// ── Standard library ──
#include <algorithm>
#include <cstdint>
#include <optional>
#include <string>
#include <vector>

export module nxs.desktop.func;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::model — Plottable-Function Catalog
// ═══════════════════════════════════════════════════════════════════════════
//
// A `FunctionSpec` is a static descriptor that maps 1:1 to a callable
// exported by `python/functions.py` (see the REGISTRY there). Colors
// follow the default ImPlot qualitative palette so the first curves
// look good without user tweaking.
//
// `PlotSeries` pairs a spec with sampled x/y data and per-curve
// presentation state (color, visibility, dirty flag). Ownership lives
// here in the model so the data survives view rebuilds.

export namespace nxs::model {

/// A function the embedded Python side knows how to evaluate.
/// `pythonName` must match a callable registered in python/functions.py.
export struct FunctionSpec {
    std::string id;          // stable key, e.g. "gaussian"
    std::string label;       // UI label, e.g. "Gaussian bell"
    std::string pythonName;  // callable in the functions registry
    float defaultColor[4];   // RGBA suggestion; the view may override
};

/// One active curve: which function, its sampled data, and per-curve
/// presentation state owned by the model so it survives view rebuilds.
export struct PlotSeries {
    FunctionSpec spec;
    std::vector<double> xs;  // filled by the controller from Python
    std::vector<double> ys;
    float color[4];
    bool visible = true;
    bool dirty = true;  // true => needs (re-)evaluation before drawing
};

// ═══════════════════════════════════════════════════════════════════════════
// FunctionRegistry — Catalog + Active-Series Manager
// ═══════════════════════════════════════════════════════════════════════════
//
// Thread-safety note: Nexus desktop apps are single-threaded (ImGui frame
// loop). No locks needed on these accessors. If a future version uses worker
// threads, wrap access behind a mutex or atomics.

export class FunctionRegistry {
public:
    /// Seeds the built-in catalog (sine, cosine, gaussian, ...).
    /// Each entry's `pythonName` maps 1:1 to a callable in
    /// `python/functions.py`.
    FunctionRegistry() {
        // Built-in catalog. Colors follow the default ImPlot qualitative
        // palette so the first curves look good without user tweaking.
        m_available = {
            {"sine",       "Sine wave",          "sine",       {0.26f, 0.62f, 0.96f, 1.0f}},
            {"cosine",     "Cosine wave",        "cosine",     {0.96f, 0.55f, 0.26f, 1.0f}},
            {"gaussian",   "Gaussian bell",      "gaussian",   {0.35f, 0.80f, 0.42f, 1.0f}},
            {"polynomial", "Cubic polynomial",   "polynomial", {0.85f, 0.37f, 0.63f, 1.0f}},
            {"damped",     "Damped oscillation", "damped",     {0.65f, 0.52f, 0.93f, 1.0f}},
            {"sinc",       "Sinc",               "sinc",       {0.93f, 0.83f, 0.32f, 1.0f}},
        };
    }

    /// Read-only view of the full catalog. The view iterates this to
    /// populate the "add curve" combo box.
    [[nodiscard]] const std::vector<FunctionSpec>& available() const { return m_available; }

    /// Mutable access to active series — the controller fills xs/ys and
    /// toggles visibility; the view reads during the ImGui frame.
    [[nodiscard]] std::vector<PlotSeries>& active() { return m_active; }

    /// Read-only access to active series (for const observers).
    [[nodiscard]] const std::vector<PlotSeries>& active() const { return m_active; }

    /// Adds a curve for `specId` if it is in the catalog and not already
    /// active. Returns the index of the new series, or nullopt on
    /// duplicate / unknown id.
    [[nodiscard]] std::optional<std::size_t> activate(const std::string& specId) {
        if (isActive(specId)) {
            return std::nullopt;
        }
        auto it = std::find_if(m_available.begin(), m_available.end(),
                               [&](const FunctionSpec& s) { return s.id == specId; });
        if (it == m_available.end()) {
            return std::nullopt;
        }
        PlotSeries series;
        series.spec = *it;
        std::copy(std::begin(it->defaultColor), std::end(it->defaultColor), std::begin(series.color));
        m_active.push_back(std::move(series));
        return m_active.size() - 1;
    }

    /// Removes a curve by spec id. Safe to call even if the id is not
    /// currently active — `std::erase_if` simply does nothing.
    void deactivate(const std::string& specId) {
        std::erase_if(m_active, [&](const PlotSeries& s) { return s.spec.id == specId; });
    }

    /// Returns true if a curve with the given spec id is currently in
    /// the active set.
    [[nodiscard]] bool isActive(const std::string& specId) const {
        return std::any_of(m_active.begin(), m_active.end(),
                           [&](const PlotSeries& s) { return s.spec.id == specId; });
    }

    /// Marks every active series dirty so the controller knows to
    /// (re-)evaluate them before the next draw call. Called when the
    /// sample range or point count changes.
    void invalidateAll() {
        for (auto& s : m_active) {
            s.dirty = true;
        }
    }

private:
    std::vector<FunctionSpec> m_available;
    std::vector<PlotSeries> m_active;
};

}  // namespace nxs::model
