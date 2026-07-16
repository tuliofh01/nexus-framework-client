// Optional Desmos-style plotter example — build with -DBUILD_NEXUS_EXAMPLES=ON.
#include "controller/PlotController.hpp"
#include "controller/PythonEngine.hpp"
#include "model/FunctionRegistry.hpp"
#include "service/FlowRunner.hpp"
#include "view/LuaPanels.hpp"
#include "view/PlotterView.hpp"
#include "FontConfig.hpp"
#include "NexusTheme.hpp"

#include <SDL3/SDL.h>
#include <SDL3/SDL_opengl.h>
#include <imgui.h>
#include <imgui_impl_opengl3.h>
#include <imgui_impl_sdl3.h>
#include <implot.h>

#include <cstdio>
#include <memory>

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

auto main(int, char**) -> int {
    if (!SDL_Init(SDL_INIT_VIDEO)) {
        std::fprintf(stderr, "SDL_Init failed: %s\n", SDL_GetError());
        return 1;
    }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK,
                        SDL_GL_CONTEXT_PROFILE_CORE);

    auto window = UniqueWindow(
        SDL_CreateWindow("PlotterApp - built with The Nexus Framework (plotter example)", 1280, 800,
                         SDL_WINDOW_OPENGL | SDL_WINDOW_RESIZABLE |
                         SDL_WINDOW_HIGH_PIXEL_DENSITY));
    if (!window) {
        std::fprintf(stderr, "SDL_CreateWindow failed: %s\n",
                     SDL_GetError());
        return 1;
    }

    auto glContext = UniqueGlContext(
        static_cast<std::remove_pointer_t<SDL_GLContext>>(
            SDL_GL_CreateContext(window.get())));
    if (!glContext) {
        std::fprintf(stderr, "SDL_GL_CreateContext failed\n");
        return 1;
    }

    SDL_GL_MakeCurrent(window.get(), glContext.get());
    SDL_GL_SetSwapInterval(1);

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImPlot::CreateContext();
    nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-dark.json");
    ImGui_ImplSDL3_InitForOpenGL(window.get(), glContext.get());
    ImGui_ImplOpenGL3_Init("#version 330");
    nxs::view::FontConfig::loadNerdFont(ImGui::GetIO());

    auto registry = nxs::model::FunctionRegistry{};
    auto python = nxs::controller::PythonEngine{};
    auto controller = nxs::controller::PlotController{registry, python};
    auto view = nxs::view::PlotterView{controller};
    auto luaPanels = nxs::view::LuaPanels{controller};
    luaPanels.loadScripts("examples/plotter/scripts");

    auto flowRunner = nxs::service::FlowRunner{controller};
    flowRunner.load("nxs_config.json", "examples/plotter/flows/flows.json");
    flowRunner.onStartup();

    controller.addFunction("sine");
    flowRunner.onEvent("curve.added");

    auto running = true;
    while (running) {
        SDL_Event event;
        while (SDL_PollEvent(&event)) {
            ImGui_ImplSDL3_ProcessEvent(&event);
            if (event.type == SDL_EVENT_QUIT) running = false;
        }
        flowRunner.tick(16);
        controller.refresh();
        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplSDL3_NewFrame();
        ImGui::NewFrame();
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

    ImPlot::DestroyContext();
    ImGui::DestroyContext();
    glContext.reset();
    window.reset();
    SDL_Quit();
    return 0;
}
