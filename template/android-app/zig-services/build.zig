const std = @import("std");

/// Zig 0.16 build — Android JNI + C-ABI sidecar for {{projectName}}.
///
/// Zig owns:
///   * `nexus_zig` static library (allocator / C ABI exports)
///   * `lib{{projectName}}.so` with JNI entry points from
///     `jni/python_bridge.zig` and `jni/lua_bridge.zig`
///
/// Zig does NOT compile C++20 named modules (`.cppm`). Zig's bundled Clang
/// cannot process them; the app's C++ modules stay in `src/` / `shared/` and
/// are not part of this sidecar. Kotlin loads this `.so` for the Chaquopy
/// bridge; see `../build_app.sh` for venv + Gradle orchestration.
///
/// Cross-compile (needs NDK — Zig does not ship Bionic):
///   zig build -Dtarget=aarch64-linux-android -Dandroid-ndk=$ANDROID_NDK -Dandroid-api=29
pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    const ndk_path = b.option([]const u8, "android-ndk", "Path to the Android NDK (required for android targets)");
    const api_level = b.option(u32, "android-api", "Android API level (default 29)") orelse 29;

    const is_android = target.result.abi.isAndroid();

    // ── Shared module: C ABI + JNI exports ─────────────────────────────
    const root_mod = b.createModule(.{
        .root_source_file = b.path("src/root.zig"),
        .target = target,
        .optimize = optimize,
        .link_libc = true,
    });
    root_mod.addIncludePath(b.path("c_abi"));

    // jni/ is a sibling of src/; expose it as named modules so root.zig can
    // `@import("jni_python")` / `@import("jni_lua")` without `../` (illegal).
    const jni_python = b.createModule(.{
        .root_source_file = b.path("jni/python_bridge.zig"),
        .target = target,
        .optimize = optimize,
        .link_libc = true,
    });
    const jni_lua = b.createModule(.{
        .root_source_file = b.path("jni/lua_bridge.zig"),
        .target = target,
        .optimize = optimize,
        .link_libc = true,
    });
    root_mod.addImport("jni_python", jni_python);
    root_mod.addImport("jni_lua", jni_lua);

    if (is_android) {
        if (ndk_path) |ndk| {
            const host_tag = "linux-x86_64";
            const sysroot = b.fmt("{s}/toolchains/llvm/prebuilt/{s}/sysroot", .{ ndk, host_tag });
            const arch_dir = switch (target.result.cpu.arch) {
                .aarch64 => "aarch64-linux-android",
                .x86_64 => "x86_64-linux-android",
                .arm => "arm-linux-androideabi",
                else => "aarch64-linux-android",
            };
            const include_usr = b.fmt("{s}/usr/include", .{sysroot});
            const include_arch = b.fmt("{s}/usr/include/{s}", .{ sysroot, arch_dir });
            const lib_arch = b.fmt("{s}/usr/lib/{s}/{d}", .{ sysroot, arch_dir, api_level });
            for ([_]*std.Build.Module{ root_mod, jni_python, jni_lua }) |mod| {
                mod.addSystemIncludePath(.{ .cwd_relative = include_usr });
                mod.addSystemIncludePath(.{ .cwd_relative = include_arch });
                mod.addLibraryPath(.{ .cwd_relative = lib_arch });
            }
        } else {
            std.log.warn("android target without -Dandroid-ndk=<path>; jni.h / Bionic link will fail", .{});
        }
    }

    // Static C-ABI library (useful for host smoke / linking tests)
    const nexus_zig = b.addLibrary(.{
        .name = "nexus_zig",
        .linkage = .static,
        .root_module = root_mod,
    });
    b.installArtifact(nexus_zig);
    b.getInstallStep().dependOn(
        &b.addInstallFile(b.path("c_abi/zig_allocator.h"), "include/zig_allocator.h").step,
    );

    // Host builds validate Zig sources only — <jni.h> lives in the NDK.
    if (!is_android) return;

    // Shared library Kotlin loads via System.loadLibrary("{{projectName}}")
    const jni_lib = b.addLibrary(.{
        .name = "{{projectName}}",
        .linkage = .dynamic,
        .root_module = root_mod,
    });
    jni_lib.root_module.linkSystemLibrary("android", .{});
    jni_lib.root_module.linkSystemLibrary("log", .{});
    b.installArtifact(jni_lib);
}
