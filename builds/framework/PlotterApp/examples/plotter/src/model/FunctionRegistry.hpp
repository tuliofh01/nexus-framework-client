// Model layer: the catalog of plottable functions and the sampled-data
// caches for the ones currently on the chart.
#pragma once

#include <cstdint>
#include <optional>
#include <string>
#include <string_view>
#include <vector>

namespace nxs::model {

struct FunctionSpec {
    std::string id;
    std::string label;
    std::string pythonName;
    float defaultColor[4];
};

struct PlotSeries {
    FunctionSpec spec;
    std::vector<double> xs;
    std::vector<double> ys;
    float color[4];
    bool visible = true;
    bool dirty = true;
};

class FunctionRegistry {
public:
    FunctionRegistry();

    [[nodiscard]] auto available() const -> const std::vector<FunctionSpec>& { return m_available; }
    [[nodiscard]] auto active() -> std::vector<PlotSeries>& { return m_active; }
    [[nodiscard]] auto active() const -> const std::vector<PlotSeries>& { return m_active; }

    [[nodiscard]] auto activate(const std::string& specId) -> std::optional<std::size_t>;
    void deactivate(const std::string& specId);
    [[nodiscard]] auto isActive(const std::string& specId) const -> bool;

    void invalidateAll();

private:
    std::vector<FunctionSpec> m_available;
    std::vector<PlotSeries> m_active;
};

}  // namespace nxs::model
