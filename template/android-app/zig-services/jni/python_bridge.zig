//! Android JNI exports for Chaquopy Python bridge — pure Zig implementation.
//!
//! ════════════════════════════════════════════════════════════════════════════
//! WHAT THIS MODULE DOES
//! ════════════════════════════════════════════════════════════════════════════
//!
//! Replaces the hand-authored C++ JNI bridge (jni_bridge.cpp, app_core.cpp,
//! NativePythonBridge.cpp, python_bridge.hpp, eval_result.hpp) with pure Zig.
//!
//! The Kotlin MainActivity calls AppCore.installPythonBridge(bridge) via JNI.
//! This Zig export stores the bridge reference and method IDs in module-level
//! globals. C++ code calls the C ABI functions (zig_python_greeting,
//! zig_python_evaluate) which internally dispatch through JNI to the Kotlin
//! bridge object.
//!
//! ════════════════════════════════════════════════════════════════════════════
//! JNI THREAD ATTACHMENT
//! ════════════════════════════════════════════════════════════════════════════
//!
//! JNI calls must be made from a thread attached to the Java VM. The C++
//! frames (ImGui, SDL) run on the main thread which is already attached.
//! For safety, we use GetEnv + conditional AttachCurrentThread on every call,
//! matching the same pattern as the original C++ NativePythonBridge.
//!
//! ════════════════════════════════════════════════════════════════════════════
//! MEMORY MODEL
//! ════════════════════════════════════════════════════════════════════════════
//!
//! Strings and arrays returned to C++ are heap-allocated via std.c.malloc.
//! The C++ caller is responsible for freeing them with the corresponding
//! zig_free_* functions. This matches the standard C ABI pattern.
//!
//! Package: `com.nexus.{{packageName}}`
//!════════════════════════════════════════════════════════════════════════════

const std = @import("std");
const jni = @cImport({
    @cInclude("jni.h");
});

// ═══════════════════════════════════════════════════════════════════════════
// Module-level global state
// ═══════════════════════════════════════════════════════════════════════════

var g_vm: ?*jni.JavaVM = null;
var g_bridge: jni.jobject = undefined;
var g_bridge_set: bool = false;
var g_greeting_mid: jni.jmethodID = undefined;
var g_evaluate_mid: jni.jmethodID = undefined;
var g_eval_cls: jni.jclass = undefined;
var g_ok_fid: jni.jfieldID = undefined;
var g_error_fid: jni.jfieldID = undefined;
var g_xs_fid: jni.jfieldID = undefined;
var g_ys_fid: jni.jfieldID = undefined;

// ═══════════════════════════════════════════════════════════════════════════
// C ABI types shared with C++
// ═══════════════════════════════════════════════════════════════════════════

pub const ZigEvalResult = extern struct {
    ok: bool,
    error: ?[*:0]const u8,
    xs: ?[*]f64,
    xs_len: i32,
    ys: ?[*]f64,
    ys_len: i32,
};

// ═══════════════════════════════════════════════════════════════════════════
// JNI entry point: AppCore.installPythonBridge(bridge)
// ═══════════════════════════════════════════════════════════════════════════

// Matches Kotlin `AppCore.installPythonBridgeNative` (wrapper calls this).
export fn Java_com_nexus_{{packageName}}_AppCore_installPythonBridgeNative(
    env: ?*jni.JNIEnv,
    _: jni.jclass,
    bridge: jni.jobject,
) callconv(.C) void {
    const env_ptr = env orelse return;
    const vt = env_ptr.*;

    // Store the JavaVM pointer for thread attachment in later calls
    _ = vt.GetJavaVM.?(env_ptr, &g_vm);

    // Cache the method IDs for bridge.greeting() and bridge.evaluate()
    const cls = vt.GetObjectClass.?(env_ptr, bridge);
    g_greeting_mid = vt.GetMethodID.?(
        env_ptr, cls, "greeting", "(Ljava/lang/String;)Ljava/lang/String;");
    g_evaluate_mid = vt.GetMethodID.?(
        env_ptr, cls, "evaluate",
        "(Ljava/lang/String;DDI)Lcom/nexus/{{packageName}}/EvalResult;");

    // Keep a global reference so the bridge object survives past this call
    g_bridge = vt.NewGlobalRef.?(env_ptr, bridge);

    // Cache EvalResult class and field IDs for efficient field extraction
    const eval_cls = vt.FindClass.?(env_ptr, "com/nexus/{{packageName}}/EvalResult");
    g_eval_cls = vt.NewGlobalRef.?(env_ptr, eval_cls);
    g_ok_fid = vt.GetFieldID.?(env_ptr, eval_cls, "ok", "Z");
    g_error_fid = vt.GetFieldID.?(env_ptr, eval_cls, "error", "Ljava/lang/String;");
    g_xs_fid = vt.GetFieldID.?(env_ptr, eval_cls, "xs", "Ljava/util/List;");
    g_ys_fid = vt.GetFieldID.?(env_ptr, eval_cls, "ys", "Ljava/util/List;");

    g_bridge_set = true;
}

// ═══════════════════════════════════════════════════════════════════════════
// JNIEnv retrieval — attaches current thread if needed
// ═══════════════════════════════════════════════════════════════════════════

fn getJniEnv(vm: *jni.JavaVM, out_env: *?*jni.JNIEnv, needs_detach: *bool) void {
    const vt = vm.*;
    var env: ?*jni.JNIEnv = null;
    const result = vt.GetEnv.?(vm, @ptrCast(&env), jni.JNI_VERSION_1_6);
    if (result == jni.JNI_EDETACHED) {
        _ = vt.AttachCurrentThread.?(vm, @ptrCast(&env), null);
        needs_detach.* = true;
    }
    out_env.* = env;
}

// ═══════════════════════════════════════════════════════════════════════════
// C ABI: zig_python_bridge_is_installed
// ═══════════════════════════════════════════════════════════════════════════

export fn zig_python_bridge_is_installed() callconv(.C) bool {
    return g_bridge_set;
}

// ═══════════════════════════════════════════════════════════════════════════
// C ABI: zig_python_greeting(name) -> const char*
// ═══════════════════════════════════════════════════════════════════════════

export fn zig_python_greeting(name: [*:0]const u8) callconv(.C) ?[*:0]const u8 {
    if (!g_bridge_set) return null;

    const vm = g_vm orelse return null;
    const vt = vm.*;

    var env_ptr: ?*jni.JNIEnv = null;
    var needs_detach: bool = false;
    getJniEnv(vm, &env_ptr, &needs_detach);

    const env = env_ptr orelse return null;
    const evt = env.*;

    // Create Java String from C string
    const j_name = evt.NewStringUTF.?(env, name);

    // Call bridge.greeting(String) -> String
    var args: [1]jni.jvalue = undefined;
    args[0].l = @ptrCast(j_name);
    const j_result_obj = evt.CallObjectMethodA.?(env, g_bridge, g_greeting_mid, &args[0]);
    evt.DeleteLocalRef.?(env, j_name);

    // Convert result jstring to C string
    const j_result: jni.jstring = @ptrCast(j_result_obj);
    if (j_result) |jr| {
        const utf = evt.GetStringUTFChars.?(env, jr, null);
        const len = std.mem.len(utf);
        const buf = @as([*:0]u8, @ptrCast(std.c.malloc(len + 1))) orelse {
            if (needs_detach) _ = vt.DetachCurrentThread.?(vm);
            return null;
        };
        @memcpy(buf[0..len], utf[0..len]);
        buf[len] = 0;
        evt.ReleaseStringUTFChars.?(env, jr, utf);
        evt.DeleteLocalRef.?(env, jr);

        if (needs_detach) _ = vt.DetachCurrentThread.?(vm);
        return buf;
    }

    if (needs_detach) _ = vt.DetachCurrentThread.?(vm);
    return null;
}

// ═══════════════════════════════════════════════════════════════════════════
// C ABI: zig_python_evaluate(func, xmin, xmax, samples) -> ZigEvalResult
// ═══════════════════════════════════════════════════════════════════════════

export fn zig_python_evaluate(
    func: [*:0]const u8,
    xmin: f64,
    xmax: f64,
    samples: i32,
) callconv(.C) ZigEvalResult {
    var default_result = ZigEvalResult{
        .ok = false,
        .error = null,
        .xs = null,
        .xs_len = 0,
        .ys = null,
        .ys_len = 0,
    };

    if (!g_bridge_set) return default_result;

    const vm = g_vm orelse return default_result;
    const vt = vm.*;

    var env_ptr: ?*jni.JNIEnv = null;
    var needs_detach: bool = false;
    getJniEnv(vm, &env_ptr, &needs_detach);

    const env = env_ptr orelse return default_result;
    const evt = env.*;

    // Create Java String from C function name
    const j_func = evt.NewStringUTF.?(env, func);

    // Call bridge.evaluate(String, double, double, int) -> EvalResult
    var args: [4]jni.jvalue = undefined;
    args[0].l = @ptrCast(j_func);
    args[1].d = xmin;
    args[2].d = xmax;
    args[3].i = samples;
    const j_result_obj = evt.CallObjectMethodA.?(env, g_bridge, g_evaluate_mid, &args[0]);
    evt.DeleteLocalRef.?(env, j_func);

    if (j_result_obj == null) {
        if (needs_detach) _ = vt.DetachCurrentThread.?(vm);
        return default_result;
    }

    const j_result = j_result_obj.?;

    // Extract ok field
    const ok = evt.GetBooleanField.?(env, j_result, g_ok_fid) != 0;

    // Extract error string
    const j_error: jni.jstring = @ptrCast(evt.GetObjectField.?(env, j_result, g_error_fid));
    const error_str = if (j_error) |je| blk: {
        const utf = evt.GetStringUTFChars.?(env, je, null);
        const len = std.mem.len(utf);
        const buf = @as([*:0]u8, @ptrCast(std.c.malloc(len + 1))) orelse {
            evt.ReleaseStringUTFChars.?(env, je, utf);
            break :blk null;
        };
        @memcpy(buf[0..len], utf[0..len]);
        buf[len] = 0;
        evt.ReleaseStringUTFChars.?(env, je, utf);
        evt.DeleteLocalRef.?(env, je);
        break :blk buf;
    } else null;

    // Extract xs and ys as double arrays (List<Double> -> double[])
    const xs = extractDoubleList(env, j_result, g_xs_fid);
    const ys = extractDoubleList(env, j_result, g_ys_fid);

    evt.DeleteLocalRef.?(env, j_result);

    if (needs_detach) _ = vt.DetachCurrentThread.?(vm);

    return ZigEvalResult{
        .ok = ok,
        .error = error_str,
        .xs = if (xs.len > 0) xs.ptr else null,
        .xs_len = @intCast(xs.len),
        .ys = if (ys.len > 0) ys.ptr else null,
        .ys_len = @intCast(ys.len),
    };
}

/// Extract a Java List<Double> into a heap-allocated []f64.
/// The caller (C++) must free the returned slice with std.c.free.
fn extractDoubleList(
    env: *jni.JNIEnv,
    j_result: jni.jobject,
    field_id: jni.jfieldID,
) []f64 {
    const evt = env.*;
    const j_list = evt.GetObjectField.?(env, j_result, field_id);
    if (j_list == null) return &[_]f64{};

    const list_cls = evt.GetObjectClass.?(env, j_list);
    const size_mid = evt.GetMethodID.?(env, list_cls, "size", "()I");
    const get_mid = evt.GetMethodID.?(env, list_cls, "get", "(I)Ljava/lang/Object;");
    const list_size = evt.CallIntMethod.?(env, j_list, size_mid);

    if (list_size <= 0) {
        evt.DeleteLocalRef.?(env, j_list);
        return &[_]f64{};
    }

    const buf = std.c.malloc(@as(usize, @intCast(list_size)) * @sizeOf(f64)) orelse {
        evt.DeleteLocalRef.?(env, j_list);
        return &[_]f64{};
    };
    const slice = @as([*]f64, @ptrCast(buf))[0..@as(usize, @intCast(list_size))];

    for (slice, 0..) |_, i| {
        const j_elem = evt.CallObjectMethod.?(env, j_list, get_mid, @as(jni.jint, @intCast(i)));
        if (j_elem) |je| {
            const double_cls = evt.GetObjectClass.?(env, je);
            const dv_mid = evt.GetMethodID.?(env, double_cls, "doubleValue", "()D");
            slice[i] = evt.CallDoubleMethod.?(env, je, dv_mid);
            evt.DeleteLocalRef.?(env, je);
        }
    }

    evt.DeleteLocalRef.?(env, j_list);
    return slice;
}

// ═══════════════════════════════════════════════════════════════════════════
// C ABI: Free functions for C++ callers
// ═══════════════════════════════════════════════════════════════════════════

export fn zig_free_string(ptr: ?[*:0]const u8) callconv(.C) void {
    if (ptr) |p| std.c.free(@as([*]u8, @constCast(@as([*c]u8, @ptrCast(p)))));
}

export fn zig_free_eval_result(result: ZigEvalResult) callconv(.C) void {
    if (result.error) |e| std.c.free(@as([*]u8, @constCast(@as([*c]u8, @ptrCast(e)))));
    if (result.xs) |x| std.c.free(x);
    if (result.ys) |y| std.c.free(y);
}
