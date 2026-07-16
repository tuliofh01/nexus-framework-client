const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    // ---- Source roots ----
    const src_root    = b.path("../src");
    const shared_root = b.path("../shared");

    // ---- C++20 module compilation flags ----
    const cppm_flags = &.{
        "-std=c++20",
        "-fmodules-ts",
        "-Xclang", "-fmodules-codegen",
        "-Xclang", "-fmodules-debuginfo",
        "-fvisibility=default",
    };
    const cpp_module_flags = &.{
        "-std=c++20",
        "-fmodules-ts",
    };

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

    // ---- App C++20 module interface units (src/) ----
    const app_module_sources = [_][]const u8{
        "model/AppModel.cppm",
        "controller/AppController.cppm",
        "controller/PythonEngine.cppm",
        "view/AppView.cppm",
        "view/LuaPanels.cppm",
        "service/FlowRunner.cppm",
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

    // ---- Shared runtime is fully consolidated into .cppm files ----
    // (no separate module implementation .cpp files needed)

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

    // ---- Module interface library ----
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
    // (shared_module_impl_sources removed — all runtime modules are now
    //  self-contained .cppm files that don't need separate impl files)

    module_lib.addIncludePath(src_root);
    module_lib.addIncludePath(shared_root.path(b, "runtime"));
    module_lib.addIncludePath(imgui_bundle.getEmittedIncludeTree());
    module_lib.addIncludePath(b.pathJoin(&.{ b.pathSlice(imgui_bundle.getEmittedIncludeTree()), "backends" }));
    module_lib.addIncludePath(b.path("c_abi"));

    if (sol2_dep) |s| {
        module_lib.addIncludePath(s.path("include"));
    }

    module_lib.linkLibrary(imgui_bundle);
    module_lib.linkLibrary(lua_dep.artifact("lua"));

    b.installArtifact(module_lib);

    // ---- Target shared library: {{projectName}}.so ----
    const lib = b.addLibrary(.{
        .name = "{{projectName}}",
        .linkage = .dynamic,
        .target = target,
        .optimize = optimize,
    });
    lib.linkLibCpp();

    // main.cpp imports C++20 modules compiled by module_lib
    lib.addCSourceFile(.{ .file = src_root.path(b, "main.cpp"), .flags = cpp_module_flags });

    // Note: JNI bridge is now pure Zig (jni/python_bridge.zig + jni/lua_bridge.zig).
    // No C++ JNI files are compiled — the Zig library handles all JNI callbacks.
    // See zig-services/jni/README.md for the C ABI interface docs.

    // Include paths
    lib.addIncludePath(src_root);
    lib.addIncludePath(shared_root.path(b, "runtime"));
    lib.addIncludePath(imgui_bundle.getEmittedIncludeTree());
    lib.addIncludePath(b.pathJoin(&.{ b.pathSlice(imgui_bundle.getEmittedIncludeTree()), "backends" }));
    lib.addIncludePath(b.path("c_abi"));

    if (sol2_dep) |s| {
        lib.addIncludePath(s.path("include"));
    }

    // Link libraries
    lib.linkLibrary(module_lib);
    lib.linkLibrary(imgui_bundle);
    lib.linkLibrary(nexus_zig);
    lib.linkLibrary(lua_dep.artifact("lua"));
    lib.linkSystemLibrary("android");
    lib.linkSystemLibrary("log");
    lib.linkSystemLibrary("GLESv3");
    lib.linkSystemLibrary("EGL");

    lib.defineCMacro("SDL_MAIN_USE_CALLBACKS", "0");

    b.installArtifact(lib);

    // ---- Install step alias ----
    const install_step = b.step("install", "Build and install {{projectName}}.so for Android");
    install_step.dependOn(&b.getInstallStep());
}
