-- panels.lua — generic Lua panel sample for {{projectName}}.

nxs.register_panel("Lua panel", function()
    ui.text("Optional Lua extension")
    if ui.button("Increment") then nxs.increment() end
end)
