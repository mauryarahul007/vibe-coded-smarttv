#!/usr/bin/env bash
# CleanTV debloat for TCL Google TV (55C61B). Reversible: uses --user 0, not root.
# Usage: ./debloat.sh <tv-ip>[:5555] [--apply]
# Default = dry run. Review output before --apply.
set -euo pipefail
TV="${1:?usage: debloat.sh <tv-ip> [--apply]}"
APPLY="${2:-}"
HERE="$(cd "$(dirname "$0")" && pwd)"
adb connect "$TV" >/dev/null
ADB="adb -s $TV"

mkdir -p "$HERE/backup"
$ADB shell pm list packages > "$HERE/backup/packages-$(date +%Y%m%d-%H%M%S).txt"

# Candidates: (1) known-safe list file, (2) TCL vendor patterns for REVIEW only.
echo "== Known list =="
while read -r pkg; do
  [[ -z "$pkg" || "$pkg" == \#* ]] && continue
  if $ADB shell pm list packages "$pkg" | grep -q "package:$pkg$"; then
    echo "FOUND  $pkg"
    [[ "$APPLY" == "--apply" ]] && $ADB shell pm uninstall -k --user 0 "$pkg" || true
  fi
done < "$HERE/bloat-packages.txt"

echo "== TCL/vendor packages on your TV (review, add to bloat-packages.txt) =="
$ADB shell pm list packages | grep -Ei 'tcl|ads|adservice|telemetry|statistic|upgrade|hotel|promo' || true
