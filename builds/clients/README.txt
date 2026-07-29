builds/clients/
===============

Deploy target for the Compose Desktop Nexus Framework client (unified
Gradle module — no :app submodule).

.gitignore keeps this directory via .gitkeep. Contents below are ignored:

  NexusFrameworkClient-<version>/   Runnable distribution (bin/, lib/, …)
    packages/                       Platform packages (.deb, .rpm, .dmg, …)

Populate locally with:

  ./build_client.sh
  ./gradlew deployToBuildsClient
  ./gradlew deployPackageToBuildsClient

Example output path (v1.1.0):

  builds/clients/NexusFrameworkClient-1.1.0/

Gradle intermediates remain under build/compose/binaries/; deploy tasks
copy finished artifacts here. See ../LAYOUT.md for the table of tasks
and paths.

Legacy note: builds/client/ (singular) is a redirect stub — use this
builds/clients/ tree.
