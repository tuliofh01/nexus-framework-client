builds/framework/
=================

Default output root for scaffolded native apps (desktop/android from the
generator). One subdirectory per project name, e.g.:

  builds/framework/MyApp/

Those project trees are git-ignored. Only this README, .gitkeep, and the
optional _fixture/ helper config are tracked.

Generate an app (examples):

  ./gradlew :cli:run --args="generate --type desktop --name MyApp"
  # or use the Compose client Generate Project flow

Build/run inside the generated project (Zig or CMake per its README).
Do not commit generated apps back into this repo.
