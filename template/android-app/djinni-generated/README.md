# djinni-generated/

Output of the Djinni generator for `../djinni/plotter.djinni`. Committed so the template builds without a Djinni install; re-run `../scripts/regen-djinni.sh` after editing the IDL.

```
cpp/    C++ headers the native core includes (records + interface bases)
kotlin/ Kotlin JVM stubs consumed by the app (PlotterCore, PythonBridge, EvalResult)
jni/    JNI marshalling glue compiled into the native library
```

Djinni does not emit Kotlin. The `kotlin/` tree is maintained to match JNI signatures produced from the IDL. Do not hand-edit `cpp/` or `jni/` — change `plotter.djinni` and regenerate. Update `kotlin/` when IDL changes affect the JVM boundary.
