const std = @import("std");

/// Zig 0.16 build — C ABI library (`nexus_zig`) + smoke test.
/// Full app compile (C++20 modules) is driven by `../build_app.sh` via g++.
pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});
    const root_mod = b.createModule(.{
        .root_source_file = b.path("src/root.zig"),
        .target = target,
        .optimize = optimize,
        .link_libc = true,
    });
    root_mod.addIncludePath(b.path("c_abi"));

    const nexus_zig = b.addLibrary(.{
        .name = "nexus_zig",
        .linkage = .static,
        .root_module = root_mod,
    });
    b.installArtifact(nexus_zig);
    b.getInstallStep().dependOn(
        &b.addInstallFile(b.path("c_abi/zig_allocator.h"), "include/zig_allocator.h").step,
    );

    const smoke = b.addExecutable(.{
        .name = "smoke_test",
        .root_module = b.createModule(.{
            .target = target,
            .optimize = optimize,
            .link_libc = true,
            .link_libcpp = true,
        }),
    });
    smoke.root_module.addCSourceFile(.{
        .file = b.path("examples/smoke_test.cpp"),
        .flags = &.{"-std=c++20"},
    });
    smoke.root_module.linkLibrary(nexus_zig);
    b.installArtifact(smoke);

    const run_smoke = b.addRunArtifact(smoke);
    const smoke_step = b.step("smoke", "Run C++ ↔ Zig C ABI smoke test");
    smoke_step.dependOn(&run_smoke.step);

    // `zig build app` — full application build. Zig's bundled Clang cannot
    // compile C++20 named modules yet, so this step bridges to build_app.sh,
    // which uses g++ -fmodules-ts for the module set and links against the
    // nexus_zig static library produced above.
    const build_app = b.addSystemCommand(&.{b.pathFromRoot("../build_app.sh")});
    build_app.setCwd(b.path(".."));
    const app_step = b.step("app", "Build the full app (delegates C++20 modules to build_app.sh/g++)");
    app_step.dependOn(&build_app.step);
}
