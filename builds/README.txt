builds/
=======

Central output directory for The Nexus Framework repo.

Tracked in git
--------------
- This README.txt, LAYOUT.md (longer reference)
- Empty-dir placeholders: clients/.gitkeep, framework/.gitkeep
- clients/README.txt; legacy client/README.txt (redirect)
- Optional light fixtures under framework/_fixture/ (generator/test helpers)

Not tracked (generated / large)
-------------------------------
- clients/NexusFrameworkClient-<ver>/  Runnable Compose Desktop distribution
                                       (from: ./build_client.sh or
                                        ./gradlew deployToBuildsClient)
- clients/.../packages/  OS installers (.deb, .rpm, .dmg, …)
                         (from: ./gradlew deployPackageToBuildsClient)
- framework/<Name>/ Scaffolded native apps from the generator/CLI
                    (e.g. Plotter2DApp, MyApp) — one folder per project

Subdirectories
--------------
clients/    Packaged Compose client distributables (see clients/README.txt)
client/     Legacy redirect → clients/
framework/  Generated native apps from templates (see framework/README.txt)

Canonical templates live under template/; this tree is output only.
