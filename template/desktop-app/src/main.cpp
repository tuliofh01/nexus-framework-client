// {{projectName}} — entry point.
//
// Bootstraps SDL3 + OpenGL 3, Dear ImGui/ImPlot, the embedded Python
// interpreter, and the Lua panel scripts, then runs the MVC loop:
//   controller.refresh()  -> re-samples dirty curves through Python
//   view.draw()           -> ImGui/ImPlot single-page UI
//   luaPanels.drawFrame() -> script-registered panels and hotkeys
#include "controller/PlotController.hpp"
#include "controller/PythonEngine.hpp"
#include "model/FunctionRegistry.hpp"
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

int main(int, char**) {
    if (!SDL_Init(SDL_INIT_VIDEO)) {
        std::fprintf(stderr, "SDL_Init failed: %s\n", SDL_GetError());
        return 1;
    }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);

    // Window title follows the Nexus branding pattern.
    SDL_Window* window =
        SDL_CreateWindow("{{windowTitle}}", 1280, 800,
                         SDL_WINDOW_OPENGL | SDL_WINDOW_RESIZABLE | SDL_WINDOW_HIGH_PIXEL_DENSITY);
    if (!window) {
        std::fprintf(stderr, "SDL_CreateWindow failed: %s\n", SDL_GetError());
        return 1;
    }
    SDL_GLContext glContext = SDL_GL_CreateContext(window);
    SDL_GL_MakeCurrent(window, glContext);
    SDL_GL_SetSwapInterval(1);  // vsync

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImPlot::CreateContext();
    nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-dark.json");
    ImGui_ImplSDL3_InitForOpenGL(window, glContext);
    ImGui_ImplOpenGL3_Init("#version 330");
    // Optional: load Nerd Font icons when assets/fonts/NexusNerdFont-Regular.ttf exists.
    nxs::view::FontConfig::loadNerdFont(ImGui::GetIO());

    // ---- MVC wiring ------------------------------------------------------
    nxs::model::FunctionRegistry registry;
    nxs::controller::PythonEngine python;  // starts CPython, imports functions.py
    nxs::controller::PlotController controller(registry, python);
    nxs::view::PlotterView view(controller);
    nxs::view::LuaPanels luaPanels(controller);
    luaPanels.loadScripts();

    // Start with one curve so the first frame isn't empty.
    controller.addFunction("sine");

    bool running = true;
    while (running) {
        SDL_Event event;
        while (SDL_PollEvent(&event)) {
            ImGui_ImplSDL3_ProcessEvent(&event);
            if (event.type == SDL_EVENT_QUIT) {
                running = false;
            }
        }

        controller.refresh();  // Python evaluation happens here, off the render path

        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplSDL3_NewFrame();
        ImGui::NewFrame();

        view.draw();
        luaPanels.drawFrame();

        ImGui::Render();
        int w = 0, h = 0;
        SDL_GetWindowSizeInPixels(window, &w, &h);
        glViewport(0, 0, w, h);
        glClearColor(0.08f, 0.09f, 0.11f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
        SDL_GL_SwapWindow(window);
    }

    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplSDL3_Shutdown();
    ImPlot::DestroyContext();
    ImGui::DestroyContext();
    SDL_GL_DestroyContext(glContext);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
}
