//! Nexus Framework first-run setup.
//!
//! Entry point for users on any platform:
//! ```bash
//! zig run misc/client-setup/setup.zig
//! source misc/client-setup/env.sh          # Linux/macOS
//! ```
//!
//! Installs Zig 0.16.0 and writes environment files.

const std = @import("std");
const bootstrap = @import("zig/bootstrap.zig");

pub fn main(init: std.process.Init) !void {
    const gpa = init.gpa;
    const io = init.io;

    std.debug.print("=== Nexus Framework — first-run setup ===\n\n", .{});

    // Step 1: Install Zig 0.16.0
    std.debug.print("Step 1/2: Installing Zig {s}...\n", .{bootstrap.zig_version});
    const installed = try bootstrap.installZig(gpa, io, .{});
    if (installed) {
        std.debug.print("  Zig {s} installed\n", .{bootstrap.zig_version});
    } else {
        std.debug.print("  Already present — continuing.\n", .{});
    }

    // Step 2: Write environment files
    std.debug.print("Step 2/2: Writing environment files...\n", .{});
    try bootstrap.writeEnvFiles(gpa, io);

    std.debug.print("\nDone.\n", .{});
    std.debug.print("Next: source misc/client-setup/env.sh\n", .{});
    std.debug.print("      ./gradlew :app:run\n", .{});
}
