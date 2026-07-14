# Issues - Zig Integration v0.2.0

- Zig does NOT ship Bionic libc - Android needs NDK sysroot (API ≥ 29)
- Risk analysis scores 68 with 3 Critical: FetchContent SPOF, CMake→Zig reversal risk, Djinni→Zig JNI gap
- JDK 26 required for :app/:core/:cli - cannot downgrade
