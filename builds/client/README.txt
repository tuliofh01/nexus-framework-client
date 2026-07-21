builds/client/
==============

Deploy target for the Compose Desktop Nexus client (:app).

.gitignore keeps this directory via .gitkeep. Contents below are ignored:

  app/       Full runnable distribution (bin/, lib/, …)
  packages/  Platform packages (.deb, .rpm, .dmg, …)

Populate locally with:

  ./gradlew :app:deployToBuildsClient
  ./gradlew :app:deployPackageToBuildsClient

Gradle intermediates remain under app/build/; deploy tasks copy finished
artifacts here. See ../LAYOUT.md for the table of tasks and paths.
