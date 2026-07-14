#pragma once

#include <string>
#include <utility>
#include <vector>

namespace nxs::bridge {

struct EvalResult final {
    bool ok;
    std::string error;
    std::vector<double> xs;
    std::vector<double> ys;

    EvalResult(bool ok_, std::string error_, std::vector<double> xs_, std::vector<double> ys_)
        : ok(std::move(ok_)), error(std::move(error_)), xs(std::move(xs_)), ys(std::move(ys_)) {}
};

}  // namespace nxs::bridge
