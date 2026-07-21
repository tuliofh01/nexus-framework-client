builds/
=======

Central output directory for The Nexus Framework repo.

Tracked in git
--------------
- This README.txt, LAYOUT.md (longer reference)
- Empty-dir placeholders: client/.gitkeep, framework/.gitkeep
- Optional light fixtures under framework/_fixture/ (generator/test helpers)

Not tracked (generated / large)
-------------------------------
- client/app/       Runnable Compose Desktop distribution
                    (from: ./gradlew :app:deployToBuildsClient)
- client/packages/  OS installers (.deb, .rpm, .dmg, …)
                    (from: ./gradlew :app:deployPackageToBuildsClient)
- framework/<Name>/ Scaffolded native apps from the generator/CLI
                    (e.g. Plotter2DApp, MyApp) — one folder per project

Subdirectories
--------------
client/     Packaged Compose client distributables (see client/README.txt)
framework/  Generated native apps from templates (see framework/README.txt)

Canonical templates live under template/; this tree is output only.
