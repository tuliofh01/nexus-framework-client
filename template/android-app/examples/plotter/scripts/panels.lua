-- panels.lua — runtime scripting layer for {{projectName}}.
--
-- Loaded by LuaPanels (sol2). Scripts see three tables:
--   nxs  : app commands (add_function, set_range, register_panel, ...)
--   ui   : a curated ImGui subset for panel bodies (text, button, ...)
--   keys : ImGuiKey constants for register_hotkey
--
-- This file is what the TypeScript/XHTML DSL in ui/ lowers into; you can
-- also edit it directly and hot-reload without recompiling the app.

-- Quick-add panel: one button per showcase function.
nxs.register_panel("Quick add", function()
    ui.text("One-click curves:")
    ui.separator()
    if ui.button("sin(x)") then nxs.add_function("sine") end
    ui.same_line()
    if ui.button("gaussian") then nxs.add_function("gaussian") end
    ui.same_line()
    if ui.button("damped") then nxs.add_function("damped") end
    if ui.button("Focus [-pi, pi]") then nxs.set_range(-math.pi, math.pi) end
end)

-- Hotkeys: F1 loads a demo set, L toggles the log-scale Y axis.
nxs.register_hotkey(keys.F1, function()
    nxs.add_function("sine")
    nxs.add_function("cosine")
    nxs.add_function("sinc")
    nxs.set_samples(1024)
end)

nxs.register_hotkey(keys.L, function()
    nxs.toggle_log_y()
end)
