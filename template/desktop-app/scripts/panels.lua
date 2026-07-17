-- panels.lua — runtime scripting layer for {{projectName}}.
--
-- Loaded by LuaPanels (sol2). Scripts see three tables:
--   nxs  : plot commands (add_expression, set_range, register_panel, ...)
--   ui   : a curated ImGui subset for panel bodies
--   keys : ImGuiKey constants for register_hotkey
--
-- This optional panel demonstrates how Lua can provide small plot workflows.
-- Edit or remove it when tailoring the generated application.

nxs.register_panel("Quick plots", function()
    ui.text("Add a common curve:")
    ui.separator()

    if ui.button("sin(x)") then
        nxs.add_expression("sin(x)")
    end
    ui.same_line()
    if ui.button("x^2") then
        nxs.add_expression("x * x")
    end

    ui.separator()
    ui.text("Set the visible x range:")
    if ui.button("-10 to 10") then
        nxs.set_range(-10, 10)
    end
    ui.same_line()
    if ui.button("-2pi to 2pi") then
        nxs.set_range(-2 * math.pi, 2 * math.pi)
    end
end)
