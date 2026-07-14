# NexusBridge — Cross-Language Interoperability Guide

> **"Connect everything"** — The NexusBridge module lets C++, Python, Lua, and Zig call each other seamlessly.

## Architecture

```
                    ┌──────────────────────┐
                    │   NexusBridge.cppm   │
                    │   (C++20 Module)     │
                    │                      │
    ┌───────────────┤  registry (name→fn)  ├───────────────┐
    │               │                      │               │
    ▼               └──────────┬───────────┘               ▼
┌─────────┐              ┌─────┴─────┐              ┌─────────┐
│  Python │◄──pybind11──►│           │◄────sol2────►│   Lua   │
│ (CPython)│             │  C++ App  │              │ (sol2)  │
└─────────┘              │ (ImGui)   │              └─────────┘
                         │           │
┌─────────┐              │           │
│   Zig   │◄───C ABI────►│           │
│ (0.14.0)│              └───────────┘
└─────────┘
```

## Quick Start

### From C++ → Call anything

```cpp
import nxs.bridge;

// Call a registered C++ function
auto result = nxs::bridge::call("nxs.version", "");
// result == "0.2.1 (NexusBridge)"

// Call Python (must have helpers.greeting defined)
nxs::bridge::call("python:greeting", "World");

// Call Lua (must have panels.say_hello registered)
nxs::bridge::call("lua:panels.say_hello", "World");

// Check Zig is linked
if (nxs::bridge::zig::available()) {
    void* mem = nxs::bridge::zig::alloc(1024);
    nxs::bridge::zig::free(mem);
}
```

### From Python → Call C++

```python
# python/helpers.py

# The bridge registers 'call_cpp' in the 'bridge' module at startup.
from bridge import call_cpp

# Call any function registered in the C++ registry
count = call_cpp("counter.get", "")
print(f"Counter value: {count}")

# Or call C++ through the unified API
result = call_cpp("nxs.version", "")
print(f"Version: {result}")
```

### From Lua → Call C++

```lua
-- scripts/panels.lua

-- The bridge registers nxs.bridge.call_cpp at startup.
local count = nxs.bridge.call_cpp("counter.get", "")
nxs.log("Counter: " .. count)

-- Call Python from Lua
local greeting = nxs.bridge.call_python("greeting", "Lua")
nxs.log(greeting)

-- Call Zig from Lua
local status = nxs.bridge.call_zig("ping")
nxs.log("Zig: " .. status)
```

### From Zig → Call C++

```zig
// zig-services/src/root.zig

// Zig calls C++ through the C ABI header
const c = @cImport({
    @include("../../src/bridge/bridge_c_api.h");
});

// But to call registered C++ functions by name, Zig goes through
// the C ABI bridge function (add this to bridge_c_api.h):
//   const char* nxs_bridge_call(const char* name, const char* arg);
//
// Then in Zig:
//   const result = c.nxs_bridge_call("nxs.version", "");
//   std.debug.print("Version: {s}\n", .{result});
```

## Registering Your Own Functions

Any language can register callbacks that any other language can invoke.

### C++ registers a function:

```cpp
#include "model/AppModel.hpp"

nxs::bridge::registry::set("counter.get", [&](std::string_view) {
    return std::to_string(m_model.counter());
});

nxs::bridge::registry::set("counter.increment", [&](std::string_view) {
    m_model.setCounter(m_model.counter() + 1);
    return "ok";
});
```

### Python calls it:

```python
from bridge import call_cpp
print(call_cpp("counter.get", ""))     # "0"
call_cpp("counter.increment", "")
print(call_cpp("counter.get", ""))     # "1"
```

### Lua calls it:

```lua
nxs.log(nxs.bridge.call_cpp("counter.get", ""))
nxs.bridge.call_cpp("counter.increment", "")
nxs.log(nxs.bridge.call_cpp("counter.get", ""))
```

### Zig calls it (via C ABI):

```zig
const result = c.nxs_bridge_call("counter.get", "");
```

## Language Pair Reference

| From ↓  | To → C++ | To → Python | To → Lua | To → Zig |
|---------|----------|-------------|----------|----------|
| **C++** | `registry::call("name", ...)` | `python::call("fn", ...)` | `lua::call("fn", ...)` | `zig::alloc()/free()` |
| **Python** | `call_cpp("name", arg)` | `eval("2+2")` | Via C++ bridge | Via C++ bridge |
| **Lua** | `nxs.bridge.call_cpp(...)` | `nxs.bridge.call_python(...)` | `run("script")` | `nxs.bridge.call_zig(...)` |
| **Zig** | `c.nxs_bridge_call(...)` | Via C++ bridge | Via C++ bridge | Direct Zig fn |

## Status & Testing

Run the bridge status check from any language:

```
# C++
import nxs.bridge;
nxs::bridge::call("bridge.status", "")

# Python
call_cpp("bridge.status", "")

# Lua
nxs.bridge.call_cpp("bridge.status", "")
```

Expected output:
```
Zig:    linked (or "not linked")
Python: ready
Lua:    ready
Registry: 3 functions
Zig allocs: 0 active
```
