#!/usr/bin/env bash
set -euo pipefail
pkg=com.voxelforge.wildlight.preview
adb wait-for-device
adb logcat -c
adb install dist/VoxelForge-Wildlight-0.3.1.apk | tee dist/install.txt
grep -Fx Success dist/install.txt
adb shell am start -W -n "$pkg/com.voxelforge.game.MainActivity" --ez qa true | tee dist/launch.txt
grep -F 'Status: ok' dist/launch.txt
for i in $(seq 1 45); do
  adb logcat -d > dist/device-log.txt
  if grep -q 'VF_QA_READY' dist/device-log.txt; then break; fi
  sleep 2
done
grep 'VF_QA_READY' dist/device-log.txt
adb shell pidof "$pkg"
! grep -E 'VF_GL_FAILED|VF_RENDER_FAILED|VF_MESH_FAILED' dist/device-log.txt
adb exec-out screencap -p > dist/device-render.png
