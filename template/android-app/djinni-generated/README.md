# djinni-generated/

**Deprecated (Phase 4).** Replaced by `zig-services/jni/` — Zig `export fn` JNI entries.

Output of the Djinni generator for `../djinni/plotter.djinni`. Kept for backward compatibility during Phase 4 migration. New JNI bridges should be authored as Zig exports under `zig-services/jni/`.

```
cpp/    C++ headers the native core includes (records + interface bases)
kotlin/ Kotlin JVM stubs consumed by the app (PlotterCore, PythonBridge, EvalResult)
jni/    JNI marshalling glue compiled into the native library
```

Djinni does not emit Kotlin. The `kotlin/` tree is maintained to match JNI signatures produced from the IDL.

See: [`docs/guides/legacy-djinni.md`](../../docs/guides/legacy-djinni.md) for archived regen instructions.
