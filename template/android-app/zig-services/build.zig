const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    // ---- Source roots ----
    const src_root    = b.path("../src");
    const shared_root = b.path("../shared");

    // ---- Nexus Zig library (C ABI + JNI exports) ----
    const nexus_zig = b.addLibrary(.{
        .name = "nexus_zig",
        .linkage = .static,
        .root_module = b.createModule(.{
            .root_source_file = b.path("src/root.zig"),
            .target = target,
            .optimize = optimize,
            .link_libc = true,
        }),
    });
    nexus_zig.root_module.?.addIncludePath(b.path("c_abi"));
    b.installArtifact(nexus_zig);

    // ---- App C++ sources (src/) ----
    const app_sources = [_][]const u8{
        "main.cpp",
        "model/AppModel.cpp",
        "controller/AppController.cpp",
        "controller/PythonEngine.cpp",
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
    };

    // ---- JNI bridge (hand-authored C++ replacing Djinni) ----
    const jni_sources = [_][]const u8{
        "jni/jni_bridge.cpp",
        "jni/NativePythonBridge.cpp",
        "jni/app_core.cpp",
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
        lib.defineCMacro("IMGUI_IMPL_OPENGL_ES3", null);
        break :blk lib;
    };

    // ---- sol2 (header-only, fetched via zon) ----
    const sol2_dep = b.dependency("sol2", .{});

    // ---- Lua 5.4 (static, fetched via zon for Android cross-compile) ----
    const lua_dep = b.dependency("lua", .{});

    // ---- Target library: {{projectName}}.so ----
    const lib = b.addLibrary(.{
        .name = "{{projectName}}",
        .linkage = .dynamic,
        .target = target,
        .optimize = optimize,
    });
    lib.linkLibCpp();

    // Add all app C++ sources
    for (app_sources) |src| {
        lib.addCSourceFile(.{ .file = src_root.path(b, src), .flags = &.{"-std=c++20"} });
    }
    // Add all shared runtime C++ sources
    for (shared_sources) |src| {
        lib.addCSourceFile(.{ .file = shared_root.path(b, src), .flags = &.{"-std=c++20"} });
    }
    // Add JNI bridge C++ sources (hand-authored replacements for Djinni glue)
    for (jni_sources) |src| {
        lib.addCSourceFile(.{ .file = b.path(src), .flags = &.{"-std=c++20"} });
    }

    // Include paths
    lib.addIncludePath(src_root);
    lib.addIncludePath(shared_root.path(b, "runtime"));
    lib.addIncludePath(b.path("jni"));
    lib.addIncludePath(imgui_bundle.getEmittedIncludeTree());
    lib.addIncludePath(b.pathJoin(&.{ b.pathSlice(imgui_bundle.getEmittedIncludeTree()), "backends" }));

    if (sol2_dep) |s| {
        lib.addIncludePath(s.path("include"));
    }

    // Link libraries
    lib.linkLibrary(imgui_bundle);
    lib.linkLibrary(nexus_zig);
    lib.linkLibrary(lua_dep.artifact("lua"));
    lib.linkSystemLibrary("android");
    lib.linkSystemLibrary("log");
    lib.linkSystemLibrary("GLESv3");
    lib.linkSystemLibrary("EGL");

    // Android JNI headers come from the NDK sysroot — Zig resolves this
    // via the target triple (aarch64-linux-android, etc.)
    lib.defineCMacro("SDL_MAIN_USE_CALLBACKS", "0");

    b.installArtifact(lib);

    // ---- Install step alias ----
    const install_step = b.step("install", "Build and install {{projectName}}.so for Android");
    install_step.dependOn(&b.getInstallStep());
}
