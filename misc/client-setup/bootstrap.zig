//! Zig 0.14.0 bootstrap — cross-platform installer.
//!
//! Compatible with Zig 0.16+ using the `main(init: std.process.Init)` pattern.
//! Downloads + extracts the pinned Zig version from ziglang.org.
//! Once 0.14.0 is installed, subsequent Zig code targets that version.

const std = @import("std");

pub const zig_version: []const u8 = "0.14.0";

const platform = struct {
    url: []const u8,
    file: []const u8,
    install_dir: []const u8,
};

fn detectPlatform() ?platform {
    const builtin = @import("builtin");
    const os = builtin.target.os.tag;
    const arch = builtin.target.cpu.arch;
    const base = "https://ziglang.org/builds";
    return switch (os) {
        .linux => switch (arch) {
            .x86_64 => .{
                .url = base ++ "/zig-linux-x86_64-0.14.0.tar.xz",
                .file = "zig-linux-x86_64-0.14.0.tar.xz",
                .install_dir = "/usr/local/zig-0.14.0",
            },
            .aarch64 => .{
                .url = base ++ "/zig-linux-aarch64-0.14.0.tar.xz",
                .file = "zig-linux-aarch64-0.14.0.tar.xz",
                .install_dir = "/usr/local/zig-0.14.0",
            },
            else => return null,
        },
        .macos => switch (arch) {
            .x86_64 => .{
                .url = base ++ "/zig-macos-x86_64-0.14.0.tar.xz",
                .file = "zig-macos-x86_64-0.14.0.tar.xz",
                .install_dir = "/usr/local/zig-0.14.0",
            },
            .aarch64 => .{
                .url = base ++ "/zig-macos-aarch64-0.14.0.tar.xz",
                .file = "zig-macos-aarch64-0.14.0.tar.xz",
                .install_dir = "/usr/local/zig-0.14.0",
            },
            else => return null,
        },
        .windows => switch (arch) {
            .x86_64 => .{
                .url = base ++ "/zig-windows-x86_64-0.14.0.zip",
                .file = "zig-windows-x86_64-0.14.0.zip",
                .install_dir = "C:\\tools\\zig-0.14.0",
            },
            else => return null,
        },
        else => return null,
    };
}

pub fn installZig(gpa: std.mem.Allocator, io: std.Io, options: struct { force: bool = false }) !bool {
    const info = detectPlatform() orelse {
        std.debug.print("Unsupported platform ({s}-{s})\n", .{
            @tagName(@import("builtin").target.os.tag),
            @tagName(@import("builtin").target.cpu.arch),
        });
        return false;
    };

    // Check if zig already exists at install dir
    if (!options.force) {
        if (std.Io.Dir.accessAbsolute(io, info.install_dir, .{})) {
            std.debug.print("Zig {s} already installed at {s}\n", .{ zig_version, info.install_dir });
            return false;
        } else |_| {}
    }

    // Download tarball
    std.debug.print("Downloading {s}...\n", .{info.url});
    _ = try std.process.run(gpa, io, .{
        .argv = &.{ "curl", "-fsSL", "-o", info.file, info.url },
    });

    // Create install directory
    std.debug.print("Creating {s}...\n", .{info.install_dir});
    _ = try std.process.run(gpa, io, .{
        .argv = &.{ "mkdir", "-p", info.install_dir },
    });

    // Extract
    std.debug.print("Extracting...\n", .{});
    _ = try std.process.run(gpa, io, .{
        .argv = &.{ "tar", "-xJf", info.file, "--directory", info.install_dir, "--strip-components=1" },
    });

    // Clean up tarball
    _ = try std.process.run(gpa, io, .{
        .argv = &.{ "rm", "-f", info.file },
    });

    std.debug.print("Zig {s} installed at {s}\n", .{ zig_version, info.install_dir });
    return true;
}

/// Write env.sh and env.bat with toolchain paths.
pub fn writeEnvFiles(gpa: std.mem.Allocator, io: std.Io) !void {
    _ = try std.process.run(gpa, io, .{ .argv = &.{
        "sh", "-c",
        "cat > misc/client-setup/env.sh << 'EOF'\n" ++
        "export ZIG_VERSION=0.14.0\n" ++
        "export ZIG_HOME=/usr/local/zig-0.14.0\n" ++
        "export PATH=$ZIG_HOME:$PATH\n" ++
        "# export ANDROID_NDK=$HOME/Android/Sdk/ndk/27.0.12077973\n" ++
        "EOF",
    }});
    std.debug.print("Created misc/client-setup/env.sh\n", .{});

    _ = try std.process.run(gpa, io, .{ .argv = &.{
        "sh", "-c",
        "cat > misc/client-setup/env.bat << 'EOF'\n" ++
        "@echo off\n" ++
        "set ZIG_VERSION=0.14.0\n" ++
        "set ZIG_HOME=C:\\tools\\zig-0.14.0\n" ++
        "set PATH=%ZIG_HOME%;%PATH%\n" ++
        "EOF",
    }});
    std.debug.print("Created misc/client-setup/env.bat\n", .{});
}

pub fn main(init: std.process.Init) !void {
    const gpa = init.gpa;
    const io = init.io;

    std.debug.print("=== Nexus Bootstrap ===\n", .{});
    _ = try installZig(gpa, io, .{});
    try writeEnvFiles(gpa, io);
    std.debug.print("Done. Source env.sh and run ./gradlew :app:run\n", .{});
}
