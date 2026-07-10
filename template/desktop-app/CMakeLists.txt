# {{projectName}} — Nexus Framework desktop template.
#
# General-purpose SDL3 + Dear ImGui starter with optional Lua, Python,
# TS/XHTML DSL, blueprint.json, and flows/flows.json.
# Enable -DBUILD_NEXUS_EXAMPLES=ON to also build the Desmos-style plotter sample.

cmake_minimum_required(VERSION 3.24)
project({{projectName}} VERSION 0.1.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

option(NXS_PREFER_SYSTEM_DEPS "Try find_package before FetchContent" OFF)
option(BUILD_NEXUS_EXAMPLES "Build optional examples/plotter sample target" OFF)

include(FetchContent)

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

if(NXS_PREFER_SYSTEM_DEPS)
    find_package(Lua 5.4 QUIET)
endif()
if(NOT LUA_FOUND)
    FetchContent_Declare(lua
        GIT_REPOSITORY https://github.com/walterschell/Lua.git
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

find_package(Python3 3.10 REQUIRED COMPONENTS Interpreter Development.Embed)
FetchContent_Declare(pybind11
    GIT_REPOSITORY https://github.com/pybind/pybind11.git
    GIT_TAG        v2.13.6)
FetchContent_MakeAvailable(pybind11)

set(NXS_SHARED_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../shared)
set(MISC_OUTPUT_DIR ${CMAKE_BINARY_DIR}/misc)

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

set(NXS_APP_SOURCES
    src/main.cpp
    src/model/AppModel.cpp
    src/controller/AppController.cpp
    src/controller/PythonEngine.cpp
    src/view/AppView.cpp
    src/view/LuaPanels.cpp
    src/service/FlowRunner.cpp
    ${NXS_SHARED_DIR}/runtime/NexusTheme.cpp
    ${NXS_SHARED_DIR}/runtime/FontConfig.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptArchive.cpp
    ${NXS_SHARED_DIR}/runtime/ScriptCrypto.cpp)

add_executable({{projectName}} ${NXS_APP_SOURCES})

target_include_directories({{projectName}} PRIVATE src ${NXS_SHARED_DIR}/runtime)
target_link_libraries({{projectName}} PRIVATE
    imgui_bundle
    sol2::sol2
    ${NXS_LUA_TARGET}
    pybind11::embed
    Python3::Python)
add_dependencies({{projectName}} pack_lua_dat pack_python_dat)

if(BUILD_NEXUS_EXAMPLES)
    message(STATUS "Building optional plotter example: {{projectName}}_plotter")
    set(NXS_PLOTTER_DIR ${CMAKE_SOURCE_DIR}/examples/plotter)
    add_executable({{projectName}}_plotter
        ${NXS_PLOTTER_DIR}/src/main.cpp
        ${NXS_PLOTTER_DIR}/src/model/FunctionRegistry.cpp
        ${NXS_PLOTTER_DIR}/src/controller/PlotController.cpp
        ${NXS_PLOTTER_DIR}/src/controller/PythonEngine.cpp
        ${NXS_PLOTTER_DIR}/src/view/PlotterView.cpp
        ${NXS_PLOTTER_DIR}/src/view/LuaPanels.cpp
        ${NXS_PLOTTER_DIR}/src/service/FlowRunner.cpp
        ${NXS_SHARED_DIR}/runtime/NexusTheme.cpp
        ${NXS_SHARED_DIR}/runtime/FontConfig.cpp
        ${NXS_SHARED_DIR}/runtime/ScriptArchive.cpp
        ${NXS_SHARED_DIR}/runtime/ScriptCrypto.cpp)
    target_include_directories({{projectName}}_plotter PRIVATE
        ${NXS_PLOTTER_DIR}/src src ${NXS_SHARED_DIR}/runtime)
    target_link_libraries({{projectName}}_plotter PRIVATE
        imgui_bundle sol2::sol2 ${NXS_LUA_TARGET} pybind11::embed Python3::Python)
    add_dependencies({{projectName}}_plotter pack_lua_dat pack_python_dat)
endif()

add_custom_command(TARGET {{projectName}} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory $<TARGET_FILE_DIR:{{projectName}}>/misc
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        ${MISC_OUTPUT_DIR}/lua.dat $<TARGET_FILE_DIR:{{projectName}}>/misc/lua.dat
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        ${MISC_OUTPUT_DIR}/python.dat $<TARGET_FILE_DIR:{{projectName}}>/misc/python.dat
    COMMENT "Staging misc/lua.dat and misc/python.dat")

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
add_custom_command(TARGET {{projectName}} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory $<TARGET_FILE_DIR:{{projectName}}>/flows
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        ${CMAKE_SOURCE_DIR}/flows/flows.json
        $<TARGET_FILE_DIR:{{projectName}}>/flows/flows.json
    COMMENT "Staging flows/flows.json")
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
