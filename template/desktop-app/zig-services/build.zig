const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    const android = b.option(bool, "android", "Compile Android JNI stub modules") orelse false;
    const linkage_opt = b.option(std.builtin.LinkMode, "linkage", "Library linkage (dynamic or static)") orelse .dynamic;

    // ---- Nexus Zig library (C ABI exports) ----
    const build_options = b.addOptions();
    build_options.addOption(bool, "android", android);

    const nexus_zig = b.addLibrary(.{
        .name = "nexus_zig",
        .linkage = linkage_opt,
        .root_module = b.createModule(.{
            .root_source_file = b.path("src/root.zig"),
            .target = target,
            .optimize = optimize,
            .link_libc = true,
        }),
    });
    nexus_zig.root_module.?.addOptions("build_options", build_options);
    nexus_zig.installHeadersDirectory(b.path("c_abi"), ".", .{});
    b.installArtifact(nexus_zig);

    // ---- Source roots ----
    const src_root    = b.path("../src");
    const shared_root = b.path("../shared");

    // ---- C++20 module compilation flags ----
    // Module interface units (.cppm) produce both a .pcm and a .o file.
    const cppm_flags = &.{
        "-std=c++20",
        "-fmodules-ts",
        "-Xclang", "-fmodules-codegen",
        "-Xclang", "-fmodules-debuginfo",
        "-fvisibility=default",
    };
    // Module implementation units (.cpp) and importers (main.cpp) just need -fmodules-ts.
    const cpp_module_flags = &.{
        "-std=c++20",
        "-fmodules-ts",
    };

    // ---- App C++20 module interface units (src/) ----
    const app_module_sources = [_][]const u8{
        "model/AppModel.cppm",
        "model/FunctionRegistry.cppm",
        "controller/AppController.cppm",
        "controller/PythonEngine.cppm",
        "controller/PlotController.cppm",
        "view/AppView.cppm",
        "view/LuaPanels.cppm",
        "service/FlowRunner.cppm",
        "bridge/NexusBridge.cppm",
    };

    // ---- Shared runtime C++20 module interface units ----
    const shared_module_iface_sources = [_][]const u8{
        "runtime/font_config.cppm",
        "runtime/nexus_theme.cppm",
        "runtime/paths.cppm",
        "runtime/script_archive.cppm",
        "runtime/script_crypto.cppm",
        "runtime/script_protection.cppm",
        "runtime/zig_allocator.cppm",
    };

    // ---- Shared runtime C++20 module implementation units ----
    const shared_module_impl_sources = [_][]const u8{
        "runtime/font_config.cpp",
        "runtime/nexus_theme.cpp",
        "runtime/script_archive.cpp",
        "runtime/script_crypto.cpp",
    };

    // ---- Standard C++ sources (no module declarations/imports) ----
    // These legacy .cpp files are used by pack_archive and smoke_test.
    const legacy_shared_sources = [_][]const u8{
        "runtime/ScriptArchive.cpp",
        "runtime/ScriptCrypto.cpp",
    };

    // ---- imgui bundle (fetched via build.zig.zon) ----
    const imgui_bundle = blk: {
        const imgui_dep  = b.dependency("imgui", .{});
        const imgui_root = imgui_dep.path("");
        const lib = b.addStaticLibrary(.{
            .name = "imgui_bundle",
            .target = target,
            .optimize = optimize,
        });
        lib.linkLibCpp();
        lib.addCSourceFiles(.{
            .root = imgui_root,
            .files = &[_][]const u8{
                "imgui.cpp", "imgui_draw.cpp", "imgui_tables.cpp", "imgui_widgets.cpp",
                "backends/imgui_impl_sdl3.cpp", "backends/imgui_impl_opengl3.cpp",
            },
            .flags = &.{ "-std=c++20", "-fvisibility=hidden" },
        });
        lib.addCSourceFiles(.{
            .root = imgui_root,
            .files = &[_][]const u8{
                "misc/implot/implot.cpp", "misc/implot/implot_items.cpp",
                "misc/imnodes/imnodes.cpp",
            },
            .flags = &.{ "-std=c++20", "-fvisibility=hidden" },
        });
        lib.addIncludePath(imgui_root);
        lib.addIncludePath(imgui_root.path(b, "backends"));
        lib.addIncludePath(imgui_root.path(b, "misc/implot"));
        lib.addIncludePath(imgui_root.path(b, "misc/imnodes"));
        lib.defineCMacro("IMNODES_NAMESPACE", "imnodes");
        break :blk lib;
    };

    // ---- System SDL3 ----
    const sdl3 = blk: {
        if (b.systemLibrary("SDL3", .{})) |sdl| {
            break :blk sdl;
        } else |_| {
            std.log.warn("SDL3 not found via pkg-config — trying static fallback", .{});
            break :blk b.addStaticLibrary(.{ .name = "SDL3", .target = target, .optimize = optimize });
        }
    };

    // ---- sol2 (header-only, fetched via zon) ----
    const sol2_dep = b.dependency("sol2", .{});

    // ---- System Lua 5.4 ----
    const lua = blk: {
        if (b.systemLibrary("lua5.4", .{})) |l| break :blk l;
        if (b.systemLibrary("lua54", .{}))  |l| break :blk l;
        if (b.systemLibrary("lua", .{}))    |l| break :blk l;
        std.log.warn("Lua 5.4 not found — install liblua5.4-dev", .{});
        break :blk null;
    };

    // ---- pybind11 (header-only, fetched via zon) ----
    const pybind11_dep = b.dependency("pybind11", .{});

    // ---- Module interface library ----
    // Compile all C++20 module interface units and implementation units as a
    // static library FIRST, so the .pcm files are cached before any importers
    // (like main.cpp) are compiled.  The executable links against this library
    // for symbols.
    const module_lib = b.addStaticLibrary(.{
        .name = "{{projectName}}_modules",
        .target = target,
        .optimize = optimize,
    });
    module_lib.linkLibCpp();

    for (app_module_sources) |src| {
        module_lib.addCSourceFile(.{ .file = src_root.path(b, src), .flags = cppm_flags });
    }
    for (shared_module_iface_sources) |src| {
        module_lib.addCSourceFile(.{ .file = shared_root.path(b, src), .flags = cppm_flags });
    }
    for (shared_module_impl_sources) |src| {
        module_lib.addCSourceFile(.{ .file = shared_root.path(b, src), .flags = cpp_module_flags });
    }

    module_lib.addIncludePath(src_root);
    module_lib.addIncludePath(shared_root.path(b, "runtime"));
    module_lib.addIncludePath(imgui_bundle.getEmittedIncludeTree().path(b, ""));
    module_lib.addIncludePath(b.pathJoin(&.{ b.pathSlice(imgui_bundle.getEmittedIncludeTree().path(b, "")), "backends" }));
    module_lib.addIncludePath(b.path("c_abi"));

    if (sol2_dep) |s| {
        module_lib.addIncludePath(s.path("include"));
    }
    if (pybind11_dep) |p| {
        module_lib.addIncludePath(p.path("include"));
    }

    module_lib.linkLibrary(imgui_bundle);
    module_lib.linkLibrary(sdl3);
    if (lua) |l| module_lib.linkLibrary(l);

    b.installArtifact(module_lib);

    // ---- Main executable ----
    const exe = b.addExecutable(.{
        .name = "{{projectName}}",
        .target = target,
        .optimize = optimize,
    });
    exe.linkLibCpp();

    // main.cpp imports modules compiled by module_lib.
    exe.addCSourceFile(.{ .file = src_root.path(b, "main.cpp"), .flags = cpp_module_flags });

    // Include paths
    exe.addIncludePath(src_root);
    exe.addIncludePath(shared_root.path(b, "runtime"));
    exe.addIncludePath(imgui_bundle.getEmittedIncludeTree().path(b, ""));
    exe.addIncludePath(b.pathJoin(&.{ b.pathSlice(imgui_bundle.getEmittedIncludeTree().path(b, "")), "backends" }));
    exe.addIncludePath(b.path("c_abi"));

    if (sol2_dep) |s| {
        exe.addIncludePath(s.path("include"));
    }
    if (pybind11_dep) |p| {
        exe.addIncludePath(p.path("include"));
    }

    // Link libraries — module_lib provides the module interface symbols
    exe.linkLibrary(module_lib);
    exe.linkLibrary(imgui_bundle);
    exe.linkLibrary(nexus_zig);
    exe.linkLibrary(sdl3);
    if (lua) |l| exe.linkLibrary(l);
    exe.linkSystemLibrary("GL");
    exe.linkSystemLibrary("python3.12");

    b.installArtifact(exe);

    // ---- Run step ----
    const run_cmd = b.addRunArtifact(exe);
    run_cmd.step.dependOn(b.getInstallStep());
    if (b.args) |args| run_cmd.addArgs(args);
    const run_step = b.step("run", "Build and run {{projectName}}");
    run_step.dependOn(&run_cmd.step);

    // ---- pack_archive tool (no C++20 modules — uses legacy PascalCase .cpp) ----
    const pack_archive = b.addExecutable(.{
        .name = "pack_archive",
        .target = target,
        .optimize = optimize,
    });
    pack_archive.linkLibCpp();
    pack_archive.addCSourceFile(.{ .file = shared_root.path(b, "tools/pack_archive.cpp"), .flags = &.{"-std=c++20"} });
    for (legacy_shared_sources) |src| {
        pack_archive.addCSourceFile(.{ .file = shared_root.path(b, src), .flags = &.{"-std=c++20"} });
    }
    pack_archive.addIncludePath(shared_root.path(b, "runtime"));
    pack_archive.addIncludePath(shared_root.path(b, "tools"));
    b.installArtifact(pack_archive);

    // ---- smoke-test: C++ ↔ Zig link verification (no C++20 modules) ----
    const smoke_test = b.addExecutable(.{
        .name = "smoke_test",
        .root_module = b.createModule(.{
            .root_source_file = b.path("examples/smoke_test.cpp"),
            .target = target,
            .optimize = optimize,
            .link_libc = true,
            .link_libcpp = true,
        }),
    });
    smoke_test.root_module.?.linkLibrary(nexus_zig);
    b.installArtifact(smoke_test);
    const smoke_step = b.step("run-smoke-test", "Build and run C++/Zig C ABI smoke test");
    smoke_step.dependOn(&b.addRunArtifact(smoke_test).step);
}
