// EquationPlotter — entry point.
//
// Bootstraps SDL3 + OpenGL 3, Dear ImGui, the embedded Python interpreter,
// and the Lua panel scripts, then runs the MVC loop:
//   controller.refresh()  -> optional Python greeting refresh
//   view.draw()           -> ImGui single-page UI
//   luaPanels.drawFrame() -> script-registered panels and hotkeys
//
// === Includes before imports ===
// GCC's -fmodules-ts mishandles textual #includes that appear after an
// import declaration in the same TU, so third-party headers (SDL/ImGui —
// not shipped as C++20 modules yet) come first.

#include <SDL3/SDL.h>
#include <SDL3/SDL_opengl.h>
#include <imgui.h>
#include <imgui_impl_opengl3.h>
#include <imgui_impl_sdl3.h>
#include <implot.h>

#include <cstdio>
#include <memory>
#include <type_traits>  // std::remove_pointer_t for the GL context alias

// === C++20 Module Imports ===
// All MVC and shared classes are imported via module interface units (.cppm).

import nxs.desktop.model;
import nxs.desktop.func;
import nxs.desktop.controller;
import nxs.desktop.python;
import nxs.desktop.view;
import nxs.desktop.lua;
import nxs.desktop.flow;
import nxs.desktop.plot;

import nexus.shared.font_config;
import nexus.shared.theme;

// RAII deleters for SDL resources via unique_ptr.
struct SdlWindowDeleter {
    void operator()(SDL_Window* w) const noexcept {
        if (w) SDL_DestroyWindow(w);
    }
};
struct SdlGlContextDeleter {
    void operator()(SDL_GLContext c) const noexcept {
        if (c) SDL_GL_DestroyContext(c);
    }
};

using UniqueWindow   = std::unique_ptr<SDL_Window, SdlWindowDeleter>;
using UniqueGlContext = std::unique_ptr<std::remove_pointer_t<SDL_GLContext>,
                                        SdlGlContextDeleter>;

int main(int, char**) {
    if (!SDL_Init(SDL_INIT_VIDEO)) {
        std::fprintf(stderr, "SDL_Init failed: %s\n", SDL_GetError());
        return 1;
    }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK,
                        SDL_GL_CONTEXT_PROFILE_CORE);

    auto window = UniqueWindow(
        SDL_CreateWindow("EquationPlotter - built with The Nexus Framework", 1280, 800,
                         SDL_WINDOW_OPENGL | SDL_WINDOW_RESIZABLE |
                         SDL_WINDOW_HIGH_PIXEL_DENSITY));
    if (!window) {
        std::fprintf(stderr, "SDL_CreateWindow failed: %s\n",
                     SDL_GetError());
        return 1;
    }

    // SDL_GL_CreateContext already returns SDL_GLContext (a pointer);
    // no cast needed — casting to the pointee type is ill-formed since
    // SDL_GLContextState is an opaque (incomplete) struct.
    auto glContext = UniqueGlContext(SDL_GL_CreateContext(window.get()));
    if (!glContext) {
        std::fprintf(stderr, "SDL_GL_CreateContext failed: %s\n",
                     SDL_GetError());
        return 1;
    }

    SDL_GL_MakeCurrent(window.get(), glContext.get());
    SDL_GL_SetSwapInterval(1);

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImPlot::CreateContext();
    nxs::runtime::NexusTheme::applyFromFile(
        "assets/themes/nexus-dark.json");
    ImGui_ImplSDL3_InitForOpenGL(window.get(), glContext.get());
    ImGui_ImplOpenGL3_Init("#version 330");
    nxs::view::FontConfig::loadNerdFont(ImGui::GetIO());

    // MVC wiring. Construction order matters: objects created earlier
    // outlive (and are referenced by) the ones created after them.
    auto model          = nxs::model::AppModel{};
    auto registry       = nxs::model::FunctionRegistry{};
    auto python         = nxs::controller::PythonEngine{};
    auto controller     = nxs::controller::AppController{model, python};
    auto plotController = nxs::controller::PlotController{registry, python};
    auto view           = nxs::view::AppView{controller, plotController};
    auto luaPanels      = nxs::view::LuaPanels{plotController};
    luaPanels.loadScripts();

    auto flowRunner     = nxs::service::FlowRunner{controller};
    flowRunner.load("nxs_config.json", "flows/flows.json");
    flowRunner.onStartup();

    controller.refresh();
    flowRunner.onEvent("app.ready");

    auto running = true;
    while (running) {
        SDL_Event event;
        while (SDL_PollEvent(&event)) {
            ImGui_ImplSDL3_ProcessEvent(&event);
            if (event.type == SDL_EVENT_QUIT) {
                running = false;
            }
        }

        flowRunner.tick(16);

        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplSDL3_NewFrame();
        ImGui::NewFrame();

        plotController.refresh();
        view.draw();
        luaPanels.drawFrame();

        ImGui::Render();
        auto w = 0, h = 0;
        SDL_GetWindowSizeInPixels(window.get(), &w, &h);
        glViewport(0, 0, w, h);
        glClearColor(0.08f, 0.09f, 0.11f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
        SDL_GL_SwapWindow(window.get());
    }

    // unique_ptr destructors fire in reverse order — glContext before window.
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplSDL3_Shutdown();
    ImPlot::DestroyContext();
    ImGui::DestroyContext();
    glContext.reset();
    window.reset();
    SDL_Quit();
    return 0;
}
