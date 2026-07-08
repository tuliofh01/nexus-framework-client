# builds/

Out-of-source CMake trees for standalone NDK builds go here
(`builds/android-arm64`, `builds/android-x86_64`). Day-to-day APK builds
use Gradle's `.cxx/` cache instead — both are git-ignored.

```bash
cmake --preset android-arm64   # requires ANDROID_NDK in the environment
cmake --build --preset android-arm64
```
