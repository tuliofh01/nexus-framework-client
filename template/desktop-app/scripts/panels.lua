-- panels.lua — widget layer for {{projectName}}.
--
-- Architecture split:
--   * C++   nxs.desktop.compute / nxs.desktop.training  → number crunching
--   * Python python/helpers.py                          → data prep + SQLite
--   * Lua   this file                                   → widgets / panels
--   * TS    ui/ui.xhtml + ui/ui.ts                       → declarative UI
--
-- Two compact panels only: Training and Chart. The 5-digit hour counter is
-- rendered with %05d so it is always exactly five digits (00000..99999).

nxs.register_panel("Training", function()
    ui.text(string.format("Hours: %05d", nxs.training_hours()))
    ui.text(string.format("Gen %04d   best %+.3f",
                          nxs.training_generation(), nxs.training_best()))
    ui.text(nxs.training_status())
    ui.separator()

    if ui.button(nxs.training_active() and "Stop training" or "Start training") then
        if nxs.training_active() then
            nxs.training_stop()
        else
            nxs.training_start()
        end
    end
    ui.same_line()
    if ui.button("Reset") then
        nxs.training_reset()
    end
end)

nxs.register_panel("Chart", function()
    if ui.button("Sine") then
        nxs.add_expression("sin(x)")
    end
    ui.same_line()
    if ui.button("Damped") then
        nxs.add_expression("exp(-0.1*x)*sin(2*x)")
    end
end)
