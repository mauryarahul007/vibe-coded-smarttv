#!/usr/bin/env bash
# Install CleanTV and disable stock Google TV home so CleanTV becomes Home.
# Usage: set-launcher.sh <tv-ip> <path/to/app-release.apk>   |   set-launcher.sh <tv-ip> --revert
set -euo pipefail
TV="${1:?}"; ARG="${2:?}"
adb connect "$TV" >/dev/null
A="adb -s $TV"
STOCK="com.google.android.apps.tv.launcherx"   # Google TV home
if [[ "$ARG" == "--revert" ]]; then
  $A shell pm enable --user 0 $STOCK
  $A shell pm enable --user 0 com.google.android.tvlauncher 2>/dev/null || true
  echo "Stock launcher re-enabled."
  exit 0
fi
$A install -r "$ARG"
$A shell pm disable-user --user 0 $STOCK || true
$A shell pm disable-user --user 0 com.google.android.tvlauncher 2>/dev/null || true
$A shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
echo "Done. Press Home on remote. Pick CleanTV 'Always' if asked."
