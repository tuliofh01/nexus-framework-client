// Model layer: the catalog of plottable functions and the sampled-data
// caches for the ones currently on the chart. The model never talks to
// Python or ImGui directly — the controller fills the caches, the view
// reads them.
#pragma once

#include <cstdint>
#include <optional>
#include <string>
#include <vector>

namespace nxs::model {

// A function the embedded Python side knows how to evaluate.
// `pythonName` must match a callable registered in python/functions.py.
struct FunctionSpec {
    std::string id;          // stable key, e.g. "gaussian"
    std::string label;       // UI label, e.g. "Gaussian bell"
    std::string pythonName;  // callable in the functions registry
    float defaultColor[4];   // RGBA suggestion; the view may override
};

// One active curve: which function, its sampled data, and per-curve
// presentation state owned by the model so it survives view rebuilds.
struct PlotSeries {
    FunctionSpec spec;
    std::vector<double> xs;  // filled by the controller from Python
    std::vector<double> ys;
    float color[4];
    bool visible = true;
    bool dirty = true;  // true => needs (re-)evaluation before drawing
};

class FunctionRegistry {
public:
    FunctionRegistry();  // seeds the built-in catalog (sin, cos, gaussian, ...)

    const std::vector<FunctionSpec>& available() const { return m_available; }
    std::vector<PlotSeries>& active() { return m_active; }
    const std::vector<PlotSeries>& active() const { return m_active; }

    // Adds a curve for `specId` if it is in the catalog and not already
    // active. Returns the index of the new series, or nullopt.
    std::optional<std::size_t> activate(const std::string& specId);
    void deactivate(const std::string& specId);
    bool isActive(const std::string& specId) const;

    // Marks every active series dirty (range or sample count changed).
    void invalidateAll();

private:
    std::vector<FunctionSpec> m_available;
    std::vector<PlotSeries> m_active;
};

}  // namespace nxs::model
