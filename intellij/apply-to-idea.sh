#!/usr/bin/env bash
# Apply the shareable IntelliJ kit into .idea/ for this repo.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IDEA="$ROOT/.idea"
KIT="$ROOT/intellij"

mkdir -p \
  "$IDEA/codeStyles" \
  "$IDEA/inspectionProfiles" \
  "$IDEA/runConfigurations" \
  "$IDEA/dictionaries"

cp -f "$KIT/codeStyles/"*.xml "$IDEA/codeStyles/"
cp -f "$KIT/inspectionProfiles/"*.xml "$IDEA/inspectionProfiles/"
cp -f "$KIT/runConfigurations/"*.xml "$IDEA/runConfigurations/" 2>/dev/null || true
cp -f "$KIT/dictionaries/"*.xml "$IDEA/dictionaries/" 2>/dev/null || true

# Project-level option overlays
cp -f "$KIT/options/ktlint-plugin.xml" "$IDEA/ktlint-plugin.xml"
cp -f "$KIT/options/misc.xml" "$IDEA/misc.xml"
cp -f "$KIT/options/compiler.xml" "$IDEA/compiler.xml" 2>/dev/null || true
cp -f "$KIT/options/vcs.xml" "$IDEA/vcs.xml"

# Actions on Save + editor UX live in workspace-ish files; drop project copies IntelliJ honors
cp -f "$KIT/options/workspaceActionsOnSave.xml" "$IDEA/workspaceActionsOnSave.xml"
cp -f "$KIT/options/editorCodeInsight.xml" "$IDEA/editorCodeInsight.xml"

# Remove Android project-system contamination if present
rm -f "$IDEA/AndroidProjectSystem.xml"

# Live templates: install hint file (IDE-global import is safer)
mkdir -p "$IDEA/liveTemplates"
cp -f "$KIT/templates/Nexus.xml" "$IDEA/liveTemplates/Nexus.xml"

cat <<EOF
Applied IntelliJ kit → $IDEA

Next in IntelliJ:
  1. File → Reload Gradle Project
  2. Settings → Tools → Actions on Save → enable Reformat / Optimize imports / Code cleanup
  3. Settings → Editor → Live Templates → Import: intellij/templates/Nexus.xml
     (or use .idea/liveTemplates/Nexus.xml if your build picks it up)
  4. Open intellij/AGENTS.md in AI Assistant / Junie chat for project wisdom

EOF
