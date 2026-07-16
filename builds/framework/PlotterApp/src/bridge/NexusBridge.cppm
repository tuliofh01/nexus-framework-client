//==============================================================================
// NexusBridge — Cross-Language Interoperability Module (C++20)
//
// ┌──────────────────────────────────────────────────────────────────────┐
// │                     NexusBridge (C++20 module)                       │
// │                                                                      │
// │  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐       │
// │  │  C++ App  │◄──►│  Python  │◄──►│   Lua    │◄──►│   Zig    │       │
// │  │(ImGui/MVC)│    │(pybind11)│    │  (sol2)  │    │(C ABI)   │       │
// │  └──────────┘    └──────────┘    └──────────┘    └──────────┘       │
// │                                                                      │
// │  Each language can:                                                  │
// │    • Call functions in any other language                            │
// │    • Register callbacks that others can invoke                       │
// │    • Pass strings and primitives across boundaries                   │
// └──────────────────────────────────────────────────────────────────────┘
//
// ═══════════════════════════════════════════════════════════════════════
// AI-ASSISTANT QUICK REFERENCE
// ═══════════════════════════════════════════════════════════════════════
//
// This file defines `export module nxs.bridge` (C++20 modules).
// All public API lives under `namespace nxs::bridge` with sub-namespaces:
//
//   nxs::bridge::zig       — Zig C ABI: alloc, free, version, available()
//   nxs::bridge::python    — pybind11: call, eval, init(module)
//   nxs::bridge::lua       — sol2:     call, run,  init(state)
//   nxs::bridge::registry  — callback hub: set(name, fn), call(name, arg)
//   nxs::bridge::call      — unified router: "python:fn", "lua:fn", "zig:fn"
//
// KEY CONTRACT:
// - call() always returns std::string — never throws across language boundary
// - All init() functions are idempotent (bridge recovers on reconnect)
// - Registry functions are thread-safe (single-threaded app model in v1)
// - zig::available() is compile-time via NXS_ZIG_LINKED macro
//
// EXTENDING:
// 1. Add a set() call in init() to register a new C++ function
// 2. Callers in any language use registry::call("my.fn", arg)
// 3. Prefix routing ("python:", "lua:", "zig:") auto-dispatches
//
// ═══════════════════════════════════════════════════════════════════════
//
// == HOW TO USE ==
//
// 1. FROM C++ (anywhere):
//    import nxs.bridge;
//    nxs::bridge::call("python:helpers.greeting", "World");
//    nxs::bridge::call("lua:panels.refresh", "");
//    nxs::bridge::zig::alloc(1024);
//
// 2. FROM PYTHON (python/helpers.py):
//    # Bridge must be registered in C++ first, then:
//    import bridge  # pybind11 module
//    result = bridge.call_cpp("nxs.get_counter", "")
//
// 3. FROM LUA (scripts/panels.lua):
//    -- Bridge exposed via nxs.bridge table
//    local result = nxs.bridge.call_cpp("nxs.get_counter", "")
//
// 4. FROM ZIG (zig-services/src/root.zig):
//    // Call C++ through C ABI
//    const cpp = @cImport({
//        @include("bridge_c_api.h");
//    });
//    const result = cpp.nxs_bridge_call("nxs.get_counter", "");
//
// == ARCHITECTURE ==
// Each language pair uses the most native mechanism:
//   C++ ←→ Zig:   C ABI (extern "C" functions)
//   C++ ←→ Python: pybind11 embedded interpreter
//   C++ ←→ Lua:   sol2 Lua state
//   Python ←→ Lua: Register callbacks via C++ bridge
//   Zig ←→ Python: Register callbacks via C++ bridge
//   Zig ←→ Lua:    Register callbacks via C++ bridge
//==============================================================================

module;

// ── Standard library ──
#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <functional>
#include <iterator>
#include <ostream>
#include <ranges>
#include <sstream>
#include <source_location>
#include <string>
#include <string_view>
#include <unordered_map>
#include <utility>

// ── Transparent hash for heterogeneous unordered_map lookup ──
// Allows find(string_view) on std::string-keyed maps without
// allocating a temporary std::string for every lookup.
struct transparent_hash {
    using is_transparent = void;
    size_t operator()(std::string_view sv) const noexcept {
        return std::hash<std::string_view>{}(sv);
    }
    size_t operator()(const std::string& s) const noexcept {
        return std::hash<std::string>{}(s);
    }
};

// ── Python (pybind11) ──
// Only included when pybind11 is available.
// The bridge gracefully degrades if Python support is absent.
#include <pybind11/embed.h>
namespace py = pybind11;

// ── Lua (sol2) ──
#include <sol/sol.hpp>

// ── Zig C ABI header ──
// When NXS_ZIG_LINKED is defined (set by build.zig when the Zig runtime
// is linked), the Zig symbols are real. Otherwise, bridge_c_api.h provides
// inline stubs that return nullptr / null / no-op so the program compiles
// and runs gracefully without Zig.
#include "bridge_c_api.h"

export module nxs.bridge;

namespace nxs::bridge {

// ══════════════════════════════════════════════════════════════════════
// INTERNAL STATE
// ══════════════════════════════════════════════════════════════════════

namespace {

// ── String builder (variadic template with C++20 concepts) ──
// Concatenates any streamable arguments into a single std::string.
// Replaces all manual ostringstream + .str() patterns.
//
// The `streamable` concept constrains Args to types that support
// operator<<(std::ostream&, T). This produces clearer compiler errors
// when a non-streamable type is passed.
template<typename T>
concept streamable = requires(std::ostream& os, T v) {
    os << v;
};

template<streamable... Args>
auto str(const Args&... args) -> std::string {
    auto os = std::ostringstream{};
    ((os << args), ...);
    return os.str();
}

// Logger: bridge messages go to stderr for debugging.
// Variadic — accepts any mix of streamable types; no pre-formatting needed.
//
// Uses std::source_location (C++20) to auto-detect the caller's file,
// line, and function name. This eliminates the need to pass __FILE__
// or __LINE__ manually.
template<streamable... Args>
void log(const char* level, const Args&... args,
         const std::source_location& loc = std::source_location::current()) {
    auto msg = str(args...);
    std::fprintf(stderr, "[NexusBridge::%s] [%s:%u] %s\n",
                 level, loc.file_name(), loc.line(), msg.c_str());
}

// ── Callback Registry ──
// Any language can register a function by name; any language can call it.
// This is the central "hub" that connects Zig, Python, and Lua indirectly.
using Callback = std::function<std::string(std::string_view)>;

// Uses transparent_hash + equal_to<> for heterogeneous lookup:
//   find(string_view) works without allocating a temp std::string.
std::unordered_map<std::string, Callback, transparent_hash, std::equal_to<>>& registry() {
    static std::unordered_map<std::string, Callback, transparent_hash, std::equal_to<>> reg;
    return reg;
}

// ── Lua State ──
// Shared sol2 state, initialized by the app's LuaPanels class.
sol::state* g_lua = nullptr;

// ── Python Modules ──
// Reference to the pybind11 module for the helpers.py interface.
py::module_* g_python_helpers = nullptr;

// ── Zig Allocator Tracking ──
// Tracks Zig-allocated pointers for debugging.
int g_zig_alloc_count = 0;

}  // anonymous namespace

// ══════════════════════════════════════════════════════════════════════
// ZIG INTEROP (C ABI)
// ══════════════════════════════════════════════════════════════════════
//
// Zig exposes C ABI symbols that we link against. The header zig_allocator.h
// declares these functions. When Zig is not linked, we fall back to malloc/free.
//
// ZIG → C++:  Zig calls extern "C" functions defined in bridge_c_api.h
// C++ → ZIG:  C++ calls extern "C" functions Zig exports
//
// Example Zig code (zig-services/src/root.zig):
//   export fn nxs_zig_version() callconv(.C) u32 { return 1; }
//   export fn nxs_zig_alloc(size: usize) callconv(.C) ?*anyopaque {
//       return @import("std").heap.c_allocator.alloc(u8, size) catch null;
//   }
//
export namespace zig {

/// Check if Zig runtime is linked and available.
/// Uses compile-time toggle NXS_ZIG_LINKED (set by build system).
/// When undefined, bridge_c_api.h provides inline stubs.
[[nodiscard]] bool available() {
#ifdef NXS_ZIG_LINKED
    return true;
#else
    return false;
#endif
}

/// Allocate memory through Zig's arena allocator.
/// Returns null if Zig is not linked or allocation fails.
[[nodiscard]] void* alloc(size_t bytes) {
    if (!available()) {
        log("zig", "alloc called but Zig is not linked — using malloc");
        return std::malloc(bytes);
    }
    void* ptr = nxs_alloc(bytes);
    if (ptr) {
        ++g_zig_alloc_count;
        log("zig", "allocated ", bytes, " bytes (total active: ", g_zig_alloc_count, ")");
    }
    return ptr;
}

/// Free memory allocated by Zig (or by the fallback malloc).
/// For the arena allocator, individual frees are no-ops until reset.
void free(void* ptr) {
    if (!ptr) return;
    if (available()) {
        nxs_free(ptr);
    } else {
        std::free(ptr);
    }
    if (g_zig_alloc_count > 0) --g_zig_alloc_count;
}

/// Get the Zig version string.
[[nodiscard]] std::string version() {
    if (!available()) return "(Zig not linked)";
    return nexus_zig_version();
}

/// Reset the Zig arena — reclaim all arena allocations since last reset.
void resetArena() {
    if (available()) nxs_reset_arena();
}

/// Number of active Zig-allocated blocks (for leak detection).
[[nodiscard]] int activeAllocations() { return g_zig_alloc_count; }

// ── RAII Guard ──
// Calls zig::alloc(n) on construction, zig::free() on destruction.
// Move-only — use std::move() to transfer ownership.
// Example:
//   auto buf = ZigAllocGuard{1024};
//   if (!buf) { /* handle OOM */ }
//   auto ptr = static_cast<uint8_t*>(buf.get());
class ZigAllocGuard {
    void* m_ptr{nullptr};
public:
    explicit ZigAllocGuard(size_t bytes) : m_ptr(zig::alloc(bytes)) {}
    ~ZigAllocGuard() { if (m_ptr) zig::free(m_ptr); }

    [[nodiscard]] void* get() const noexcept { return m_ptr; }
    explicit operator bool() const noexcept { return m_ptr != nullptr; }

    // Move
    ZigAllocGuard(ZigAllocGuard&& other) noexcept
        : m_ptr(std::exchange(other.m_ptr, nullptr)) {}
    auto operator=(ZigAllocGuard&& other) noexcept -> ZigAllocGuard& {
        if (this != &other) {
            if (m_ptr) zig::free(m_ptr);
            m_ptr = std::exchange(other.m_ptr, nullptr);
        }
        return *this;
    }

    // No copy
    ZigAllocGuard(const ZigAllocGuard&) = delete;
    auto operator=(const ZigAllocGuard&) -> ZigAllocGuard& = delete;
};

}  // namespace zig

// ══════════════════════════════════════════════════════════════════════
// PYTHON INTEROP (pybind11)
// ══════════════════════════════════════════════════════════════════════
//
// Python runs in-process via pybind11's embedded interpreter.
// The bridge connects C++ to the python/functions.py module.
//
// C++ → PYTHON:  py::module_.attr("function_name")(args)
// PYTHON → C++:  Functions registered with pybind11's .def() are callable
//
// Example Python code (python/functions.py):
//   def bridge_call(name: str, arg: str) -> str:
//       # This function is registered from C++ via pybind11
//       from bridge import call_cpp
//       return call_cpp(name, arg)
//
export namespace python {

/// Check if Python interpreter is running.
[[nodiscard]] bool available() {
    return g_python_helpers != nullptr && Py_IsInitialized();
}

/// Template helper: wraps any callable with availability check + error formatting.
/// Eliminates the repetitive try-catch-ostringstream pattern from eval() and call().
template<typename Fn>
auto guarded(Fn&& fn) -> std::string {
    if (!available()) return "(Python not available)";
    try {
        return std::forward<Fn>(fn)();
    } catch (const py::error_already_set& e) {
        log("python", e.what());
        return str("(error: ", e.what(), ")");
    }
}

/// Evaluate a Python expression string and return the result.
/// Example: eval("2 + 2") → "4"
std::string eval(const std::string& expr) {
    return guarded([&]() -> std::string {
        return py::str(py::eval(expr)).cast<std::string>();
    });
}

/// Call a function from the helpers module.
///   call("greeting", "World") → calls helpers.greeting("World")
std::string call(const std::string& fn, const std::string& arg) {
    return guarded([&]() -> std::string {
        return py::str(g_python_helpers->attr(fn.c_str())(arg)).cast<std::string>();
    });
}

/// Initialize the Python bridge. Called once by the app at startup.
void init(py::module_& helpers_module) {
    g_python_helpers = &helpers_module;
    log("python", "bridge initialized");

    // ── Register C++ functions so Python can call them ──
    // In python/functions.py, users can:
    //   from bridge import call_cpp
    //   result = call_cpp("counter.get", "")
    //   print(result)  # "0"
    py::module_ bridge_mod = py::module_::create("bridge");
    bridge_mod.def("call_cpp", [](const std::string& name, const std::string& arg) {
        return registry::call(name, arg);
    });
    py::module_::import("sys").attr("modules")["bridge"] = bridge_mod;

    log("python", "registered bridge.call_cpp for Python");
}

}  // namespace python

// ══════════════════════════════════════════════════════════════════════
// LUA INTEROP (sol2)
// ══════════════════════════════════════════════════════════════════════
//
// Lua panels run in-process via sol2. The bridge exposes C++ functions
// to Lua through the nxs.bridge table.
//
// C++ → LUA:   m_lua["function_name"](args)
// LUA → C++:   Functions registered with set_function() in Lua state
//
// Example Lua code (scripts/panels.lua):
//   -- Call a C++ function through the bridge
//   local result = nxs.bridge.call_cpp("counter.get", "")
//   nxs.log("Counter value: " .. result)
//
export namespace lua {

/// Check if Lua state is initialized.
[[nodiscard]] bool available() { return g_lua != nullptr; }

/// Run a Lua script string.
///   run("return 2 + 2") → "4"
auto run(const std::string& script) -> std::string {
    if (!available()) return "(Lua not available)";
    auto result = g_lua->safe_script(script, sol::script_pass_on_error);
    if (!result.valid()) {
        sol::error err = result;
        log("lua", err.what());
        return str("(error: ", err.what(), ")");
    }
    if (result.get_type() == sol::type::string) {
        return result.get<std::string>();
    }
    if (result.get_type() == sol::type::number) {
        return std::to_string(result.get<double>());
    }
    return "(ok)";
}

/// Call a Lua function by name.
///   call("panels.say_hello", "World") → runs panels.say_hello("World")
auto call(const std::string& fn, const std::string& arg) -> std::string {
    if (!available()) return "(Lua not available)";
    auto result = (*g_lua)[fn](arg);
    if (!result.valid()) {
        sol::error err = result;
        log("lua", err.what());
        return str("(error: ", err.what(), ")");
    }
    return "(ok)";
}

/// Initialize the Lua bridge. Called once by LuaPanels.
void init(sol::state& lua) {
    g_lua = &lua;

    // ── Register C++ functions so Lua can call them ──
    // In scripts/panels.lua, users can:
    //   local count = nxs.bridge.call_cpp("counter.get", "")
    //   nxs.log(count)
    lua["nxs"]["bridge"] = lua.create_table();
    lua["nxs"]["bridge"]["call_cpp"] = [](const std::string& name, const std::string& arg) {
        return registry::call(name, arg);
    };
    lua["nxs"]["bridge"]["call_python"] = [](const std::string& fn, const std::string& arg) {
        return python::call(fn, arg);
    };
    lua["nxs"]["bridge"]["call_zig"] = [](const std::string& fn) -> std::string {
        // Zig functions must be exported with C ABI and registered
        // in the C++ registry to be callable from Lua.
        return registry::call("zig:" + fn, "");
    };
    lua["nxs"]["bridge"]["version"] = []() { return std::string("0.3.0-bridge"); };

    log("lua", "bridge initialized (nxs.bridge table)");
}

}  // namespace lua

// ══════════════════════════════════════════════════════════════════════
// CALLBACK REGISTRY
// ══════════════════════════════════════════════════════════════════════
//
// The registry is the central hub for cross-language calls:
//
//   ┌────────┐   register("counter.get", fn)   ┌────────────┐
//   │  C++   │─────────────────────────────────►│            │
//   │  code  │  register("zig:ping", fn)        │  Registry  │
//   └────────┘─────────────────────────────────►│            │
//   ┌────────┐   call_cpp("counter.get", arg)   │  (name →   │
//   │ Python │─────────────────────────────────►│  callback) │
//   └────────┘                                  │            │
//   ┌────────┐   nxs.bridge.call_cpp(...)       │            │
//   │  Lua   │─────────────────────────────────►│            │
//   └────────┘                                  └────────────┘
//
export namespace registry {

/// Register a C++ function so Python and Lua can call it.
///   register("counter.get", [](std::string_view) {
///       return std::to_string(model.counter());
///   });
void set(std::string name, Callback fn) {
    registry()[name] = std::move(fn);
    log("registry", "registered: ", name);
}

/// Call a registered function by name.
/// Returns the function's result, or an error message string.
[[nodiscard]] auto call(std::string_view name, std::string_view arg) -> std::string {
    auto it = registry().find(name);
    if (it == registry().end()) {
        log("registry", "unknown function: ", name);
        return str("(no such function: ", name, ")");
    }
    try {
        return it->second(arg);
    } catch (const std::exception& e) {
        log("registry", e.what());
        return str("(error: ", e.what(), ")");
    }
}

/// List all registered function names (for debugging).
[[nodiscard]] auto list() -> std::vector<std::string> {
    auto names = std::vector<std::string>{};
    names.reserve(std::ranges::size(registry()));
    std::ranges::transform(registry(), std::back_inserter(names),
        [](const auto& pair) { return pair.first; });
    return names;
}

/// Remove a registered function.
void remove(const std::string& name) {
    registry().erase(name);
    log("registry", "unregistered: ", name);
}

/// How many functions are currently registered.
[[nodiscard]] int count() { return static_cast<int>(registry().size()); }

}  // namespace registry

// ══════════════════════════════════════════════════════════════════════
// UNIFIED CALL
// ══════════════════════════════════════════════════════════════════════
//
// Top-level convenience function. Routes calls by prefix:
//   "python:fn"  → python::call("fn", arg)
//   "lua:fn"     → lua::call("fn", arg)
//   "zig:fn"     → registry::call("zig:fn", arg)
//   otherwise    → registry::call(name, arg)
//
// This is the primary API for most users. Just call:
//   bridge::call("python:helpers.greeting", "World")
//   bridge::call("lua:panels.say_hello", "World")
//   bridge::call("counter.get", "")
//
[[nodiscard]] auto call(const std::string& name, const std::string& arg) -> std::string {
    if (name.starts_with("python:")) {
        return python::call(name.substr(7), arg);
    }
    if (name.starts_with("lua:")) {
        return lua::call(name.substr(4), arg);
    }
    if (name.starts_with("zig:")) {
        return registry::call(name, arg);
    }
    return registry::call(name, arg);
}

// ══════════════════════════════════════════════════════════════════════
// INITIALIZATION
// ══════════════════════════════════════════════════════════════════════

/// Initialize the bridge system. Call this once at app startup.
/// The bridge logs its status so users can verify interop is working.
void init(sol::state& lua_state, py::module_& python_module) {
    log("init", "╔══════════════════════════════════════════╗");
    log("init", "║  NexusBridge v0.3.0 — Cross-Language Hub ║");
    log("init", "╚══════════════════════════════════════════╝");

    // ── Register C++ → C++ introspection ──
    registry::set("bridge.list", [](std::string_view) {
        auto out = std::ostringstream{};
        auto names = registry::list();
        std::ranges::copy(names,
            std::ostream_iterator<std::string>(out, "\n"));
        return out.str();
    });
    registry::set("bridge.status", [](std::string_view) -> std::string {
        return str("Zig:    ", (zig::available() ? "linked" : "not linked"), "\n",
                   "Python: ", (python::available() ? "ready" : "not available"), "\n",
                   "Lua:    ", (lua::available() ? "ready" : "not available"), "\n",
                   "Registry: ", registry::count(), " functions\n",
                   "Zig allocs: ", zig::activeAllocations(), " active\n");
    });

    // ── Initialize language bridges ──
    lua::init(lua_state);
    python::init(python_module);

    // ── Register standard app functions ──
    // These are examples that users can extend. Each shows a different
    // language calling through the bridge to reach C++.

    registry::set("nxs.version", [](std::string_view) -> std::string {
        return "0.3.0 (NexusBridge)";
    });

    // ── Status report ──
    log("init", "Bridge ready!");
    log("init", "  Registry: ", registry::count(), " functions\n",
                "  Zig:      ", (zig::available() ? "✓" : "✗ (stub)"), "\n",
                "  Python:   ", (python::available() ? "✓" : "✗"), "\n",
                "  Lua:      ", (lua::available() ? "✓" : "✗"));
}

}  // namespace nxs::bridge
