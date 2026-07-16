const std = @import("std");

// Build file for the Zig bootstrap executable.
// Compiles bootstrap.zig into a standalone "zig-bootstrap" binary that can
// be distributed or run without the Zig compiler present (after initial bootstrap).
//
// Usage:
//   zig build          # build zig-bootstrap executable
//   zig build install  # install to zig-out/bin/
pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    // Bootstrap executable — downloads and installs Zig 0.16.0.
    // This can be run standalone, or called by setup.zig (the user entry point).
    const bootstrap_exe = b.addExecutable(.{
        .name = "zig-bootstrap",
        .root_source_file = b.path("bootstrap.zig"),
        .target = target,
        .optimize = optimize,
    });
    b.installArtifact(bootstrap_exe);

    // Test the bootstrap module's utility functions.
    const bootstrap_test = b.addTest(.{
        .root_source_file = b.path("bootstrap.zig"),
        .target = target,
        .optimize = optimize,
    });
    const test_step = b.step("test", "Run bootstrap unit tests");
    test_step.dependOn(&bootstrap_test.step);

    // Run step for quick testing.
    const run_bootstrap = b.addRunArtifact(bootstrap_exe);
    const run_step = b.step("run", "Run the zig-bootstrap executable");
    run_step.dependOn(&run_bootstrap.step);
}
