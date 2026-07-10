#!/usr/bin/env bash
# Regenerate Djinni stubs for the Android plotter template.
#
# Prerequisites: Djinni CLI on PATH (https://djinni.xlcpp.dev/ or Snapchat/djinni build).
# Djinni has no --kotlin-out; JVM stubs live as Kotlin in djinni-generated/kotlin/
# and must be updated to match JNI after IDL changes (see README.md).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IDL="$ROOT/djinni/plotter.djinni"
GEN="$ROOT/djinni-generated"
STAGING_JAVA="$(mktemp -d)"

cleanup() { rm -rf "$STAGING_JAVA"; }
trap cleanup EXIT

if ! command -v djinni >/dev/null 2>&1; then
    echo "error: djinni not found on PATH — install from https://djinni.xlcpp.dev/" >&2
    exit 1
fi

echo "==> C++ headers"
djinni \
    --idl "$IDL" \
    --cpp-out "$GEN/cpp" \
    --cpp-namespace nxs::bridge

echo "==> JNI marshalling"
djinni \
    --idl "$IDL" \
    --java-out "$STAGING_JAVA/com/nexus/plotter" \
    --java-package com.nexus.plotter \
    --jni-out "$GEN/jni" \
    --ident-jni-class Native

echo "==> Kotlin JVM stubs (reference diff)"
djinni \
    --idl "$IDL" \
    --java-out "$STAGING_JAVA/com/nexus/plotter" \
    --java-package com.nexus.plotter

echo ""
echo "C++ and JNI regenerated under djinni-generated/{cpp,jni}/."
echo "Compare staged Java (for JNI signatures) with djinni-generated/kotlin/:"
echo "  diff -ru $STAGING_JAVA/com/nexus/plotter $GEN/kotlin/com/nexus/plotter || true"
echo ""
echo "Update djinni-generated/kotlin/ to match any IDL changes, then rebuild:"
echo "  ./gradlew :app:assembleDebug"
