# Zig JNI Bridge — C ABI Interface

Replaces the legacy 7-file C++ JNI bridge (deleted) with pure Zig.

## Files

| File | Role |
|------|------|
| `python_bridge.zig` | Python bridge JNI callbacks — `installPythonBridge`, `greeting`, `evaluate` |
| `lua_bridge.zig` | Lua bridge JNI callbacks (optional, same pattern) |

## C ABI Exports

Declared in `PythonEngine.cppm` as `extern "C"` and implemented in `python_bridge.zig`:

```c
bool zig_python_bridge_is_installed(void);
const char* zig_python_greeting(const char* name);
ZigEvalResult zig_python_evaluate(const char* func, double xmin, double xmax, int32_t samples);
void zig_free_string(const char* ptr);
void zig_free_eval_result(ZigEvalResult result);
```

## ZigEvalResult struct layout (must match C++)

```c
typedef struct {
    bool        ok;
    const char* error;
    const double* xs;
    int32_t     xs_len;
    const double* ys;
    int32_t     ys_len;
} ZigEvalResult;
```

Field order and sizes must be identical in Zig and C++ (both use C-compatible layout).

## Memory ownership

| Allocator | Returned by | Freed by |
|-----------|-------------|----------|
| `std.c.malloc` | `zig_python_greeting` → `const char*` | `zig_free_string` |
| `std.c.malloc` | `zig_python_evaluate` → `ZigEvalResult` (.error, .xs, .ys) | `zig_free_eval_result` |

- Zig allocates all heap memory with `std.c.malloc`.
- C++ caller **must** free every returned allocation.
- `zig_free_eval_result` frees `.error`, `.xs`, and `.ys` in one call.
- After freeing, do not dereference the pointers.

## Call chain

```
Kotlin MainActivity
  → AppCore.installPythonBridge(bridge)        // Kotlin fun
    → JNI: Java_com_nexus_{{pkg}}_AppCore_installPythonBridge  // Zig export
      → stores JavaVM*, bridge jobject, method IDs in Zig globals

C++ later:
  PythonEngine::greeting("MyApp")
    → zig_python_greeting("MyApp")              // C ABI → Zig
      → JNI CallObjectMethodA on bridge.greeting()
      → returns heap-allocated const char*
    → wraps in std::string, calls zig_free_string()

  PythonEngine::evaluate("sin", 0.0, 3.14, 100)
    → zig_python_evaluate(...)
      → JNI CallObjectMethodA on bridge.evaluate()
      → returns ZigEvalResult with heap-allocated arrays
    → copies into std::vector, calls zig_free_eval_result()
```

## Thread safety

- Zig module stores `JavaVM*` at install time.
- Each call to `zig_python_greeting` / `zig_python_evaluate` calls `(*vm)->AttachCurrentThread` / `DetachCurrentThread` for that invocation.
- Safe to call from any thread (including SDL render thread).

## Upgrade from C++ bridge

Before (5 layers): `Kotlin → Djinni → C++ singleton → virtual dispatch → JNI`

After (3 layers): `Kotlin → Zig C ABI → JNI`

7 C++ files deleted: `jni_bridge.cpp`, `app_core.cpp`, `app_core.hpp`, `NativePythonBridge.cpp`, `NativePythonBridge.hpp`, `python_bridge.hpp`, `eval_result.hpp`.
