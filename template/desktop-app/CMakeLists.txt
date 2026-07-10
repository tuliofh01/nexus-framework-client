# {{projectName}} — Nexus Framework desktop template.
#
# Stack: SDL3 (window/input/GPU) + Dear ImGui + ImPlot (charts) + imnodes
# (blueprint editing), Lua 5.4 bound through sol2, and CPython embedded
# through pybind11. Dependencies are pulled with FetchContent by default;
# configure with -DNXS_PREFER_SYSTEM_DEPS=ON to use find_package first
# (useful with vcpkg/conan or distro packages).

cmake_minimum_required(VERSION 3.24)
project({{projectName}} VERSION 0.1.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

option(NXS_PREFER_SYSTEM_DEPS "Try find_package before FetchContent" OFF)

include(FetchContent)

# --------------------------------------------------------------------------
# SDL3 — windowing, input, GPU context. Same backend the Android template
# uses, which is what keeps app code identical across targets.
# --------------------------------------------------------------------------
if(NXS_PREFER_SYSTEM_DEPS)
    find_package(SDL3 CONFIG QUIET)
endif()
if(NOT SDL3_FOUND)
    FetchContent_Declare(SDL3
        GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
        GIT_TAG        release-3.2.4)
    set(SDL_SHARED OFF CACHE BOOL "" FORCE)
    set(SDL_STATIC ON  CACHE BOOL "" FORCE)
    set(SDL_TEST_LIBRARY OFF CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(SDL3)
endif()

# --------------------------------------------------------------------------
# Dear ImGui + ImPlot + imnodes. None of these ship CMake configs upstream,
# so we fetch sources and build small static libs with the SDL3/OpenGL3
# backends compiled in.
# --------------------------------------------------------------------------
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
target_compile_definitions(imgui_bundle PUBLIC IMNODES_NAMESPACE=imnodes)
target_link_libraries(imgui_bundle PUBLIC SDL3::SDL3)

# --------------------------------------------------------------------------
# Lua 5.4 + sol2 — the runtime scripting layer the TS/XHTML DSL lowers into.
# --------------------------------------------------------------------------
if(NXS_PREFER_SYSTEM_DEPS)
    find_package(Lua 5.4 QUIET)
endif()
if(NOT LUA_FOUND)
    FetchContent_Declare(lua
        GIT_REPOSITORY https://github.com/walterschell/Lua.git  # CMake-friendly Lua 5.4 mirror
        GIT_TAG        v5.4.5)
    set(LUA_ENABLE_TESTING OFF CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(lua)
    set(NXS_LUA_TARGET lua_static)
else()
    add_library(nxs_lua INTERFACE)
    target_include_directories(nxs_lua INTERFACE ${LUA_INCLUDE_DIR})
    target_link_libraries(nxs_lua INTERFACE ${LUA_LIBRARIES})
    set(NXS_LUA_TARGET nxs_lua)
endif()

FetchContent_Declare(sol2
    GIT_REPOSITORY https://github.com/ThePhD/sol2.git
    GIT_TAG        v3.3.1)
FetchContent_MakeAvailable(sol2)

# --------------------------------------------------------------------------
# Embedded Python via pybind11. Requires a Python 3 dev install at build
# time; numpy arrays produced by python/functions.py are consumed through
# the buffer protocol so samples stay in native memory (no copies to JVM/JS
# style marshalling layers).
# --------------------------------------------------------------------------
find_package(Python3 3.10 REQUIRED COMPONENTS Interpreter Development.Embed)
FetchContent_Declare(pybind11
    GIT_REPOSITORY https://github.com/pybind/pybind11.git
    GIT_TAG        v2.13.6)
FetchContent_MakeAvailable(pybind11)

# --------------------------------------------------------------------------
# Application target — MVC layout under src/.
# --------------------------------------------------------------------------
set(NXS_SHARED_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../shared)
set(MISC_OUTPUT_DIR ${CMAKE_BINARY_DIR}/misc)

# --------------------------------------------------------------------------
# Script archive packer — builds misc/lua.dat and misc/python.dat (v2 optional
# encryption when scriptProtection.enabled is true in nxs_config.json).
# --------------------------------------------------------------------------
add_executable(pack_archive
    ${NXS_SHARED_DIR}/tools/pack_archive.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptArchive.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptCrypto.cpp)
target_include_directories(pack_archive PRIVATE ${NXS_SHARED_DIR}/runtime ${NXS_SHARED_DIR}/tools)

add_custom_target(pack_lua_dat ALL
    COMMAND pack_archive lua
        ${CMAKE_SOURCE_DIR}/scripts
        ${CMAKE_BINARY_DIR}/misc/lua.dat
        ${CMAKE_SOURCE_DIR}/nxs_config.json
    DEPENDS pack_archive
    BYPRODUCTS ${CMAKE_BINARY_DIR}/misc/lua.dat
    COMMENT "Packing Lua scripts to misc/lua.dat")
add_custom_target(pack_python_dat ALL
    COMMAND pack_archive python
        ${CMAKE_SOURCE_DIR}/python
        ${CMAKE_BINARY_DIR}/misc/python.dat
        ${CMAKE_SOURCE_DIR}/nxs_config.json
    DEPENDS pack_archive
    BYPRODUCTS ${CMAKE_BINARY_DIR}/misc/python.dat
    COMMENT "Packing Python scripts to misc/python.dat")

add_executable({{projectName}}
    src/main.cpp
    src/model/FunctionRegistry.cpp
    src/controller/PlotController.cpp
    src/controller/PythonEngine.cpp
    src/view/PlotterView.cpp
    src/view/LuaPanels.cpp
    ${NXS_SHARED_DIR}/runtime/NexusTheme.cpp
    ${NXS_SHARED_DIR}/runtime/FontConfig.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptArchive.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptCrypto.cpp)

target_include_directories({{projectName}} PRIVATE src ${NXS_SHARED_DIR}/runtime)
target_link_libraries({{projectName}} PRIVATE
    imgui_bundle
    sol2::sol2
    ${NXS_LUA_TARGET}
    pybind11::embed
    Python3::Python)
add_dependencies({{projectName}} pack_lua_dat pack_python_dat)

# Copy packed archives and runtime assets beside the executable.
add_custom_command(TARGET {{projectName}} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory $<TARGET_FILE_DIR:{{projectName}}>/misc
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        ${MISC_OUTPUT_DIR}/lua.dat $<TARGET_FILE_DIR:{{projectName}}>/misc/lua.dat
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        ${MISC_OUTPUT_DIR}/python.dat $<TARGET_FILE_DIR:{{projectName}}>/misc/python.dat
    COMMENT "Staging misc/lua.dat and misc/python.dat")

# Dev fallback: plain script trees + DSL (python/ omitted when using python.dat).
foreach(asset_dir scripts ui)
    add_custom_command(TARGET {{projectName}} POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory
            ${CMAKE_SOURCE_DIR}/${asset_dir}
            $<TARGET_FILE_DIR:{{projectName}}>/${asset_dir})
endforeach()
add_custom_command(TARGET {{projectName}} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
        ${CMAKE_SOURCE_DIR}/blueprint.json
        $<TARGET_FILE_DIR:{{projectName}}>/blueprint.json)
foreach(shared_asset_dir assets themes)
    if(EXISTS ${NXS_SHARED_DIR}/${shared_asset_dir})
        add_custom_command(TARGET {{projectName}} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy_directory
                ${NXS_SHARED_DIR}/${shared_asset_dir}
                $<TARGET_FILE_DIR:{{projectName}}>/assets/${shared_asset_dir})
    endif()
endforeach()
if(EXISTS ${CMAKE_SOURCE_DIR}/assets)
    add_custom_command(TARGET {{projectName}} POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory
            ${CMAKE_SOURCE_DIR}/assets
            $<TARGET_FILE_DIR:{{projectName}}>/assets)
endif()
