# Coding styles — Nexus Framework

Authoritative style rules for code in this repo: the Compose client (`:app`, `:core`, `:cli`), bundled templates, and generated output. **Follow this guide** when editing native templates, Kotlin generation code, or DSL scripts.

Tooling paths:

| Tool | Location |
|------|----------|
| `.clang-format` | `template/desktop-app/`, `template/android-app/`, `template/shared/` |
| `.editorconfig` | repo root |
| `zig fmt` | `template/desktop-app/zig-services/` (no config file — Zig defaults) |

---

## C++20 (desktop + Android templates, `template/shared/runtime/`)

**Baseline:** C++20, LLVM-derived `.clang-format` (4-space indent, 110 columns). Run `clang-format -i <file>` only on files you edit — do not mass-reformat vendored trees.

### Prefer

- **RAII** for all resources (files, interpreters, GPU handles). No raw `new`/`delete` without a documented reason; use `std::unique_ptr` or stack storage.
- **`std::span`** / **`std::string_view`** for non-owning buffer views instead of `(const char*, size_t)` pairs.
- **`[[nodiscard]]`** on functions where ignoring the return value is a bug (load/save, validation, allocation).
- **`constexpr`** for compile-time constants (magic numbers, header sizes, flags).
- **`concept`s** when a template or overload set would otherwise need comments explaining valid types.
- **Modules** are optional — headers remain the default in templates until CMake/module tooling is uniform across targets.

### Avoid

- C-style casts; use `static_cast` / `reinterpret_cast` with a one-line *why* if unavoidable (binary I/O).
- Silent fallbacks on parse/load failure — return `bool` or `std::optional` and log at the call site.
- Mass include churn — keep include order per `.clang-format` categories (project → vendored → std).

### Format

```bash
clang-format -i path/to/file.cpp   # from template root or shared/
```

---

## Zig (`template/desktop-app/zig-services/`)

**Baseline:** `zig fmt` (built-in, no `.zigfmt` file). Pin **Zig 0.14.x** per `misc/client-setup/env.sh`.

### Rules

- Run **`zig fmt`** before committing any `.zig` change.
- **Explicit allocators** — pass `std::mem.Allocator` or use a documented global arena; never assume an implicit heap.
- **Error unions (`!T`)** — propagate with `try` / `catch`; document recoverable vs fatal at API boundaries.
- **C ABI exports** — every `export fn` must have a matching declaration in `c_abi/*.h` with ownership and thread-safety notes.
- **`callconv(.C)`** on function pointers stored in structs consumed from C++.

### Format

```bash
cd template/desktop-app/zig-services && zig fmt src/ build.zig
```

See [zig-services/README.md](../../template/desktop-app/zig-services/README.md).

---

## Kotlin (`:app`, `:core`, `:cli`)

**Baseline:** Kotlin 2.4, JDK 26 toolchain (`misc/build-logic`).

### Rules

- **Null-safety** — prefer non-null types; use `?.`, `?:`, and `requireNotNull` at system boundaries (file I/O, user input).
- **Data classes** for JSON schemas, validation results, and generation DTOs (`BlueprintFile`, `FlowsFile`, `GenerateOptions`).
- **`require` / `check`** for programmer errors; **`error(...)`** in validators when the caller must handle failure.
- Keep Gradle build logic **configuration-cache compatible** — no mutable global state in task actions.
- Controllers hold UI state; generation logic stays in `:core` services.

### Format

Use IDE / `ktfmt` if configured; match surrounding file style (4-space indent in this repo).

---

## Lua (`template/*/scripts/`)

- **2-space** indent, snake_case for functions and locals.
- Panels expose callbacks the C++ controller binds via sol2 — keep side effects in `cpp.controller`, not in panel layout code.
- Short header comment when a script depends on a blueprint edge or archive path.

---

## TypeScript (`template/*/ui/`, `template/shared/dsl/`)

- Match existing DSL style: **semicolons**, 4-space indent, `state<T>()` / `native<T>()` / `invoke()` for controller wiring.
- XHTML lives beside `ui.ts`; page class name must match `blueprint.json` `ui.page` references.
- Prefer `readonly` fields on component classes where the runtime does not mutate them.

---

## Python (`template/*/python/`, Chaquopy modules)

- **PEP 8** — 4-space indent, `snake_case`, type hints on public functions used from C++.
- Desktop: `functions.py` (or packed `python.dat`) returns numpy-friendly tuples for pybind11 buffer protocol.
- Android: keep JNI-facing modules thin; heavy logic stays in `app/src/main/python/`.

---

## Educational comments

On non-trivial logic (archive crypto, flow dispatch, generation pipeline, Zig C ABI), add comments that explain **why**, not **what**:

- Invariants and failure modes
- Cross-language contracts (C ABI, Djinni, pybind11 GIL)
- Versioning / migration notes

Skip comments on trivial getters, one-line delegates, and generated Djinni stubs.

---

## Related docs

- [coding-with-nexus.md](coding-with-nexus.md) — UI, MVC, blueprint usage
- [generation-pipeline.md](generation-pipeline.md) — `ProjectGenerator`, CLI
- [AGENTS.md](../../AGENTS.md) — build commands for agents
