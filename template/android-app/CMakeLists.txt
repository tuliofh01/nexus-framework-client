# {{projectName}} — Nexus Framework Android native library.
#
# Built by the Gradle Android Gradle Plugin via externalNativeBuild, or
# standalone with the android-arm64 / android-x86_64 CMake presets.
# Python is NOT embedded here — Chaquopy owns CPython on the JVM side and
# talks to this library through the Djinni PythonBridge.

cmake_minimum_required(VERSION 3.24)
project({{projectName}} VERSION 0.1.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)

# ---- SDL3 (Android NDK build) ---------------------------------------------
FetchContent_Declare(SDL3
    GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
    GIT_TAG        release-3.2.4)
set(SDL_SHARED ON  CACHE BOOL "" FORCE)
set(SDL_STATIC OFF CACHE BOOL "" FORCE)
set(SDL_TEST_LIBRARY OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(SDL3)

# ---- Dear ImGui + ImPlot + imnodes ----------------------------------------
FetchContent_Declare(imgui
    GIT_REPOSITORY https://github.com/ocornut/imgui.git
    GIT_TAG        v1.91.8)
FetchContent_Declare(implot
    GIT_REPOSITORY https://github.com/epezent/implot.git
    GIT_TAG        v0.16)
FetchContent_Declare(imnodes
    GIT_REPOSITORY https://github.com/Nelarius/imnodes.git
    GIT_TAG        v0.5)
FetchContent_MakeAvailable(imgui implot imnodes)

add_library(imgui_bundle STATIC
    ${imgui_SOURCE_DIR}/imgui.cpp
    ${imgui_SOURCE_DIR}/imgui_draw.cpp
    ${imgui_SOURCE_DIR}/imgui_tables.cpp
    ${imgui_SOURCE_DIR}/imgui_widgets.cpp
    ${imgui_SOURCE_DIR}/backends/imgui_impl_sdl3.cpp
    ${imgui_SOURCE_DIR}/backends/imgui_impl_opengl3.cpp
    ${implot_SOURCE_DIR}/implot.cpp
    ${implot_SOURCE_DIR}/implot_items.cpp
    ${imnodes_SOURCE_DIR}/imnodes.cpp)
target_include_directories(imgui_bundle PUBLIC
    ${imgui_SOURCE_DIR}
    ${imgui_SOURCE_DIR}/backends
    ${implot_SOURCE_DIR}
    ${imnodes_SOURCE_DIR})
target_compile_definitions(imgui_bundle PUBLIC IMNODES_NAMESPACE=imnodes IMGUI_IMPL_OPENGL_ES3)
target_link_libraries(imgui_bundle PUBLIC SDL3::SDL3 GLESv3 EGL)

# ---- Lua 5.4 + sol2 -------------------------------------------------------
FetchContent_Declare(lua
    GIT_REPOSITORY https://github.com/walterschell/Lua.git
    GIT_TAG        v5.4.5)
set(LUA_ENABLE_TESTING OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(lua)

FetchContent_Declare(sol2
    GIT_REPOSITORY https://github.com/ThePhD/sol2.git
    GIT_TAG        v3.3.1)
FetchContent_MakeAvailable(sol2)

# ---- Shared native library (loaded by MainActivity) -----------------------
set(NXS_SHARED_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../shared)

# Host-only packer for Gradle packLuaDat (Android APK assets).
if(NOT ANDROID)
    add_executable(pack_archive
        ${NXS_SHARED_DIR}/tools/pack_archive.cpp
        ${NXS_SHARED_DIR}/runtime/ScriptArchive.cpp
        ${NXS_SHARED_DIR}/runtime/ScriptCrypto.cpp)
    target_include_directories(pack_archive PRIVATE ${NXS_SHARED_DIR}/runtime ${NXS_SHARED_DIR}/tools)
endif()

add_library({{projectName}} SHARED
    src/main.cpp
    src/model/FunctionRegistry.cpp
    src/controller/PlotController.cpp
    src/controller/PythonEngine.cpp
    src/view/PlotterView.cpp
    src/view/LuaPanels.cpp
    src/service/FlowRunner.cpp
    ${NXS_SHARED_DIR}/runtime/NexusTheme.cpp
    ${NXS_SHARED_DIR}/runtime/FontConfig.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptArchive.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptCrypto.cpp
    djinni-generated/jni/NativePythonBridge.cpp)

target_include_directories({{projectName}} PRIVATE
    src
    ${NXS_SHARED_DIR}/runtime
    djinni-generated/cpp
    djinni-generated/jni)

target_link_libraries({{projectName}} PRIVATE
    imgui_bundle
    sol2::sol2
    lua_static
    android
    log
    GLESv3
    EGL)

# SDL_main is provided by SDL3 on Android; keep the symbol.
target_compile_definitions({{projectName}} PRIVATE SDL_MAIN_USE_CALLBACKS=0)
