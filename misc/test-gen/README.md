# test-gen — generate tests for built apps

Flat layout under `misc/test-gen/` (depth-2 misc tree). Emits smoke stubs into a generated app under `builds/framework/<name>/`.

```bash
./misc/test-gen/linux-generic.sh --project MyApp
./misc/test-gen/linux-debian.sh builds/framework/MyApp
./misc/test-gen/linux-generic.sh --dry-run --project _fixture
./misc/test-gen/linux-generic.sh --force --project MyApp
```

| Host | Entry |
|------|--------|
| Linux (Arch) | [linux-arch.sh](linux-arch.sh) |
| Linux (Debian/Ubuntu) | [linux-debian.sh](linux-debian.sh) |
| Linux (Fedora/RHEL) | [linux-fedora.sh](linux-fedora.sh) |
| Linux (POSIX fallback) | [linux-generic.sh](linux-generic.sh) |
| macOS | [macos-darwin.sh](macos-darwin.sh) |
| Windows | [windows-win32.ps1](windows-win32.ps1) (needs bash) |

Core: [generate-tests.sh](generate-tests.sh). Templates (`*.tpl`) live in this same directory.

Generated files include a `nexus-test-gen: generated` marker. Re-running skips existing files unless `--force` is passed.
