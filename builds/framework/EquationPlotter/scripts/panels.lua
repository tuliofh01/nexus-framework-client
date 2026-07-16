-- panels.lua — runtime scripting layer for EquationPlotter.
--
-- Loaded by LuaPanels (sol2). Scripts see three tables:
--   nxs  : app commands (increment, decrement, register_panel, ...)
--   ui   : a curated ImGui subset for panel bodies
--   keys : ImGuiKey constants for register_hotkey

nxs.register_panel("Lua panel", function()
    ui.text("Optional Lua extension — delete or replace freely.")
    ui.separator()
    if ui.button("Increment") then nxs.increment() end
    ui.same_line()
    if ui.button("Reset") then nxs.reset() end
end)

nxs.register_hotkey(keys.F1, function()
    nxs.log("F1 pressed — manual trigger")
    nxs.increment()
end)
