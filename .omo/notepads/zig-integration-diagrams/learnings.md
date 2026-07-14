# Learnings - Zig Integration v0.2.0

## Project Structure
- AGENTS.md at root level documents build commands for coding assistants
- misc/client-setup/ has platform-specific first-run installers (linux/, macos/, windows/)
- template/desktop-app/zig-services/ already exists with build.zig, main.zig, memory.zig, JNI stubs
- misc/scripts/generate-diagrams/generate-styled-diagrams.py is the SVG generator (1165 lines, produces 8 SVGs)

## Build System
- JDK 26 required for :app/:core/:cli (misc/build-logic/)
- Gradle stays for Compose + generation pipeline
- Zig goes in generated native apps only
- CMake is default today; Zig phases in beside it

## Diagram Expectations
- No ASCII diagrams allowed in any committed file
- All diagrams must be rendered SVGs via generate-styled-diagrams.py
- Style: JetBrainsMono font, layered colored boxes, professional arrows, legends
