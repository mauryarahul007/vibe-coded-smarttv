#!/usr/bin/env bash
# Restore a package removed by debloat.sh. Usage: restore.sh <tv-ip> <package|--all>
set -euo pipefail
TV="${1:?usage: restore.sh <tv-ip> <package|--all>}"
adb connect "$TV" >/dev/null
HERE="$(cd "$(dirname "$0")" && pwd)"
if [[ "${2:?}" == "--all" ]]; then
  while read -r pkg; do
    [[ -z "$pkg" || "$pkg" == \#* ]] && continue
    adb -s "$TV" shell cmd package install-existing --user 0 "$pkg" || true
  done < "$HERE/bloat-packages.txt"
else
  adb -s "$TV" shell cmd package install-existing --user 0 "$2"
fi
