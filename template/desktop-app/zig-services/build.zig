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

    // ---- App C++ sources (src/) ----
    const app_sources = [_][]const u8{
        "main.cpp",
        "model/AppModel.cpp",
        "model/FunctionRegistry.cpp",
        "controller/AppController.cpp",
        "controller/PythonEngine.cpp",
        "controller/PlotController.cpp",
        "view/AppView.cpp",
        "view/LuaPanels.cpp",
        "service/FlowRunner.cpp",
    };

    // ---- Shared runtime C++ sources ----
    const shared_sources = [_][]const u8{
        "runtime/NexusTheme.cpp",
        "runtime/FontConfig.cpp",
        "runtime/ScriptArchive.cpp",
        "runtime/ScriptCrypto.cpp",
        "runtime/ZigAllocator.cpp",
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
        // ImPlot + ImNodes (vendored under misc/)
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

    // ---- Main executable ----
    const exe = b.addExecutable(.{
        .name = "{{projectName}}",
        .target = target,
        .optimize = optimize,
    });
    exe.linkLibCpp();

    // Add all app C++ sources
    for (app_sources) |src| {
        exe.addCSourceFile(.{ .file = src_root.path(b, src), .flags = &.{"-std=c++20"} });
    }
    // Add all shared runtime C++ sources
    for (shared_sources) |src| {
        exe.addCSourceFile(.{ .file = shared_root.path(b, src), .flags = &.{"-std=c++20"} });
    }

    // Include paths
    exe.addIncludePath(src_root);
    exe.addIncludePath(shared_root.path(b, "runtime"));
    exe.addIncludePath(imgui_bundle.getEmittedIncludeTree().path(b, ""));
    exe.addIncludePath(b.pathJoin(&.{ b.pathSlice(imgui_bundle.getEmittedIncludeTree().path(b, "")), "backends" }));

    if (sol2_dep) |s| {
        exe.addIncludePath(s.path("include"));
    }
    if (pybind11_dep) |p| {
        exe.addIncludePath(p.path("include"));
    }

    // Link libraries
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

    // ---- pack_archive tool for Lua/Python archive packing ----
    const pack_archive = b.addExecutable(.{
        .name = "pack_archive",
        .target = target,
        .optimize = optimize,
    });
    pack_archive.linkLibCpp();
    pack_archive.addCSourceFile(.{ .file = shared_root.path(b, "tools/pack_archive.cpp"), .flags = &.{"-std=c++20"} });
    pack_archive.addCSourceFile(.{ .file = shared_root.path(b, "runtime/ScriptArchive.cpp"), .flags = &.{"-std=c++20"} });
    pack_archive.addCSourceFile(.{ .file = shared_root.path(b, "runtime/ScriptCrypto.cpp"), .flags = &.{"-std=c++20"} });
    pack_archive.addIncludePath(shared_root.path(b, "runtime"));
    pack_archive.addIncludePath(shared_root.path(b, "tools"));
    b.installArtifact(pack_archive);

    // ---- smoke-test: C++ ↔ Zig link verification (NOT the app entry point) ----
    // This is a standalone C++ smoke test that verifies C ABI exports from nexus_zig.
    // The actual app entry point is src/main.cpp (compiled above under app_sources).
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

    // ---- C++20 module compilation (bridge/NexusBridge.cppm) ----
    if (std.fs.accessAbsolute(b.pathJoin(&.{ b.pathSlice(src_root), "bridge/NexusBridge.cppm" }), .{})) {
        // Compile the C++20 module interface unit first
        // zig c++ (Clang) uses -fmodules-ts for module support
        // The .pcm file is emitted alongside the object; we use -Xclang -fmodules-codegen
        // to also produce a .o file we can link.
        const cppm_flags = &.{
            "-std=c++20",
            "-Xclang", "-fmodules-ts",
            "-Xclang", "-fmodules-codegen",
            "-Xclang", "-fmodules-debuginfo",
            "-fvisibility=default",
        };
        exe.addCSourceFile(.{ .file = src_root.path(b, "bridge/NexusBridge.cppm"), .flags = cppm_flags });
        exe.addIncludePath(src_root.path(b, "bridge"));
        std.log.info("C++20 module bridge/NexusBridge.cppm enabled — ensure all importers have -fmodules-ts", .{});
    } else {
        std.log.warn("bridge/NexusBridge.cppm not found — C++20 module bridge disabled", .{});
    }
