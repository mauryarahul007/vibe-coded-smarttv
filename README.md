# CleanTV (vibe-coded-smarttv)

Ad-free minimal launcher + ADB debloat for TCL 55C61B (Google TV). No root, fully reversible.

## 1. Enable ADB on TV
Settings > System > About > tap *Android TV OS build* 7x.
Settings > System > Developer options > USB debugging + Network debugging on.
Note IP: Settings > Network. Accept the prompt on TV on first connect.

## 2. Build APK
Needs JDK 17 + Android SDK (platform 34). Android Studio: open folder, Build > Build APK.
CLI: `gradle wrapper && ./gradlew assembleRelease`
Output: `app/build/outputs/apk/release/app-release.apk` (debug-signed, fine for sideload).

## 3. Install and set Home
`scripts/set-launcher.sh <tv-ip> app/build/outputs/apk/release/app-release.apk`
Undo: `scripts/set-launcher.sh <tv-ip> --revert`

## 4. Debloat
Dry run: `scripts/debloat.sh <tv-ip>` — lists matches and TCL-like packages.
Add confirmed TCL packages to `scripts/bloat-packages.txt`, then `--apply`.
Undo: `scripts/restore.sh <tv-ip> --all`

## Notes
- Kept: Play Store, Play Services, Assistant, Cast, Settings, remote/CEC.
- Long-press OK on an app = app info / uninstall.
- Factory reset also restores everything.
- TCL package names are NOT guessed; the script lists what's on your TV.
