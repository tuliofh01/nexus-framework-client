//==============================================================================
// nxs.desktop.func — Function Catalog & Plot-Series Registry (C++20 Module)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Owns the catalog of plottable functions (`FunctionSpec`) and the
// sampled-data caches (`PlotSeries`) for every curve currently on the
// chart. The model layer is the single source of truth — the controller
// fills the caches, the view reads them.
//
// ════════════════════════════════════════════════════════════════════════════
// C++20 MODULE STRUCTURE (for readers new to modules)
// ════════════════════════════════════════════════════════════════════════════
//
//   1. module;           — global module fragment (private to this TU)
//   2. export module X;  — declares this file as module X's interface
//   3. export namespace/class — the public API importers can use
//
// This replaces the old .hpp/.cpp split: one .cppm IS both header and
// source. The compiler handles the rest.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// std::optional<T>   — a value that may or may not exist. Returns
//                      std::nullopt on failure instead of -1 or throwing.
//                      Caller checks: `if (auto idx = reg.activate("sine"))`
//
// std::find_if        — algorithm that finds the first element matching a
//                       predicate (lambda). Returns an iterator.
//
// std::any_of         — returns true if ANY element matches the predicate.
//                       Replaces a manual for-loop + break.
//
// std::erase_if       — C++20 free function. Removes all elements from a
//                       container that match a predicate. One-liner instead
//                       of the old erase-remove idiom.
//
// std::copy           — copies a range [first, last) to a destination.
//                       Used here to copy the default RGBA color array.
//
// [[nodiscard]]       — compiler warns if the return value is discarded.
//
// lambda captures     — `[&]` captures all local variables by reference.
//                       `[this]` captures the object pointer.
//
// ════════════════════════════════════════════════════════════════════════════
// MVC ROLE
// ════════════════════════════════════════════════════════════════════════════
//
//   Model (this module)  <── mutates  ──  Controller (PlotController)
//        │                                      │
//        └──── reads  ──  View (AppView / ImGui)
//
// The model never talks to Python or ImGui directly. The controller calls
// `activate()` / `deactivate()` / `invalidateAll()`; the view reads
// `available()` and `active()` every frame.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Standard library (private to this module) ──
//
// <algorithm>  — std::find_if, std::any_of, std::copy, std::erase_if
// <cstdint>    — std::size_t for index types
// <optional>   — std::optional<T>, std::nullopt
// <string>     — std::string for IDs and labels
// <vector>     — std::vector for the catalog and active-series lists
//
// These are NOT visible to importers. An `import nxs.desktop.func;` file
// cannot call std::find_if without its own #include.
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
// exported by `python/functions.py`. Colors follow the default ImPlot
// qualitative palette so the first curves look good without tweaking.
//
// `PlotSeries` pairs a spec with sampled x/y data and per-curve
// presentation state. Ownership lives here in the model so the data
// survives view rebuilds.

export namespace nxs::model {

/// A function the embedded Python side knows how to evaluate.
/// `pythonName` must match a callable registered in python/functions.py.
///
/// Design note: this is a POD-like struct (no methods, no inheritance).
/// It is cheap to copy and could be constexpr if we added a constructor.
export struct FunctionSpec {
    std::string id;          // stable key, e.g. "gaussian"
    std::string label;       // UI label, e.g. "Gaussian bell"
    std::string pythonName;  // callable in the functions registry
    float defaultColor[4];   // RGBA suggestion; the view may override
};

/// One active curve: which function, its sampled data, and per-curve
/// presentation state owned by the model so it survives view rebuilds.
///
/// Design note: xs/ys are std::vector<double> — dynamically sized arrays
/// that manage their own memory via RAII. When PlotSeries is destroyed,
/// the vectors free their buffers automatically. No manual new/delete.
export struct PlotSeries {
    FunctionSpec spec;
    std::vector<double> xs;  // filled by the controller from Python
    std::vector<double> ys;
    float color[4]{0.0f, 0.0f, 0.0f, 1.0f};  // {} zero-init RGBA
    bool visible{true};
    bool dirty{true};  // true => needs (re-)evaluation before drawing
};

// ═══════════════════════════════════════════════════════════════════════════
// FunctionRegistry — Catalog + Active-Series Manager
// ═══════════════════════════════════════════════════════════════════════════
//
// Thread-safety: Nexus desktop apps are single-threaded (ImGui frame
// loop). No locks needed on these accessors.

export class FunctionRegistry {
public:
    // ── Constructor ────────────────────────────────────────────────────
    //
    // Seeds the built-in catalog. Each entry's `pythonName` maps 1:1 to
    // a callable in `python/functions.py`.
    //
    // The initializer list uses brace-init for each FunctionSpec:
    //   {"sine", "Sine wave", "sine", {0.26f, 0.62f, 0.96f, 1.0f}}
    // This is aggregate initialization — no constructor needed.

    FunctionRegistry() {
        m_available = {
            {"sine",       "Sine wave",          "sine",       {0.26f, 0.62f, 0.96f, 1.0f}},
            {"cosine",     "Cosine wave",        "cosine",     {0.96f, 0.55f, 0.26f, 1.0f}},
            {"gaussian",   "Gaussian bell",      "gaussian",   {0.35f, 0.80f, 0.42f, 1.0f}},
            {"polynomial", "Cubic polynomial",   "polynomial", {0.85f, 0.37f, 0.63f, 1.0f}},
            {"damped",     "Damped oscillation", "damped",     {0.65f, 0.52f, 0.93f, 1.0f}},
            {"sinc",       "Sinc",               "sinc",       {0.93f, 0.83f, 0.32f, 1.0f}},
        };
    }

    // ── Catalog accessors ──────────────────────────────────────────────
    //
    // Returns a const reference — no copy. The view iterates this to
    // populate the "add curve" combo box.

    [[nodiscard]] auto available() const
        -> const std::vector<FunctionSpec>& {
        return m_available;
    }

    // ── Active-series accessors ────────────────────────────────────────
    //
    // Mutable and const overloads. The controller calls the mutable
    // version to fill xs/ys; the view calls the const version to read.

    [[nodiscard]] auto active() -> std::vector<PlotSeries>& {
        return m_active;
    }

    [[nodiscard]] auto active() const
        -> const std::vector<PlotSeries>& {
        return m_active;
    }

    // ── Activation / Deactivation ──────────────────────────────────────
    //
    // activate(): adds a curve for `specId` if it is in the catalog and
    // not already active. Returns std::optional<std::size_t>:
    //
    //   - std::nullopt  → duplicate or unknown id (failure)
    //   - some index    → position in the active vector (success)
    //
    // Why std::optional? It is the modern C++ way to express "may not
    // have a value". Better than returning -1 (which requires the caller
    // to remember a sentinel) or throwing (which is overkill for a
    // simple lookup failure).
    //
    // [[nodiscard]] forces the caller to handle the result.

    [[nodiscard]] auto activate(const std::string& specId)
        -> std::optional<std::size_t> {
        if (isActive(specId)) {
            return std::nullopt;  // already active — no duplicate
        }

        // std::find_if: scans m_available for the first element whose id
        // matches specId. The lambda `[&](const FunctionSpec& s) { ... }`
        // captures nothing by value ([&] = capture all by reference) and
        // returns true when the id matches.
        //
        // Iterator pattern: find_if returns m_available.end() on failure.
        // This is the standard C++ "sentinel" pattern — end() means "not
        // found".
        const auto it = std::find_if(
            m_available.begin(), m_available.end(),
            [&](const FunctionSpec& s) { return s.id == specId; });

        if (it == m_available.end()) {
            return std::nullopt;  // not in catalog
        }

        // Create a new PlotSeries from the found spec.
        PlotSeries series;
        series.spec = *it;  // copy the spec

        // std::copy: copies the 4-element RGBA color array from the spec
        // to the series. std::begin/std::end work on raw arrays.
        std::copy(std::begin(it->defaultColor),
                  std::end(it->defaultColor),
                  std::begin(series.color));

        // std::move: transfers ownership of `series` into the vector.
        // After this line, `series` is in a valid-but-unspecified state
        // (we don't use it again, so this is fine).
        m_active.push_back(std::move(series));

        // Return the index of the newly added series.
        return m_active.size() - 1;
    }

    // deactivate(): removes all curves with the given spec id.
    //
    // std::erase_if (C++20): a free function that removes elements from
    // a container matching a predicate. Equivalent to the old
    // "erase-remove" idiom but as a single call:
    //   vec.erase(std::remove_if(...), vec.end());
    // Now it's just:
    //   std::erase_if(vec, predicate);
    //
    // Safe to call even if the id is not active — it simply does nothing.

    void deactivate(const std::string& specId) {
        std::erase_if(m_active,
            [&](const PlotSeries& s) { return s.spec.id == specId; });
    }

    // isActive(): returns true if any active series has the given spec id.
    //
    // std::any_of: returns true if ANY element in the range matches the
    // predicate. More expressive than a manual for-loop:
    //   for (const auto& s : m_active)
    //       if (s.spec.id == specId) return true;
    //   return false;

    [[nodiscard]] auto isActive(const std::string& specId) const -> bool {
        return std::any_of(m_active.begin(), m_active.end(),
            [&](const PlotSeries& s) { return s.spec.id == specId; });
    }

    // invalidateAll(): marks every active series dirty so the controller
    // knows to re-evaluate them before the next draw call.
    //
    // Range-based for loop: `for (auto& s : m_active)` iterates over
    // every element. `auto&` gives a mutable reference (we modify `dirty`).

    void invalidateAll() {
        for (auto& s : m_active) {
            s.dirty = true;
        }
    }

private:
    std::vector<FunctionSpec> m_available;  // full catalog (never changes)
    std::vector<PlotSeries> m_active;       // currently plotted curves
};

}  // namespace nxs::model
