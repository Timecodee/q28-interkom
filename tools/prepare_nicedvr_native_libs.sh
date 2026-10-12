#!/usr/bin/env bash
set -euo pipefail

if [ "${1:-}" = "" ]; then
  echo "Usage: tools/prepare_nicedvr_native_libs.sh /path/to/NiceDVR.xapk"
  exit 1
fi

XAPK="$1"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
WORK_DIR="${ROOT_DIR}/build/nicedvr_extract"
OUT_DIR="${ROOT_DIR}/app/src/main/jniLibs/arm64-v8a"

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR" "$OUT_DIR"

unzip -q "$XAPK" -d "$WORK_DIR"

SPLIT_APK="$(find "$WORK_DIR" -maxdepth 1 -name '*arm64*.apk' -print -quit)"
BASE_APK="$(find "$WORK_DIR" -maxdepth 1 -name 'com.vidure.nicedvr.apk' -print -quit)"

if [ -z "$SPLIT_APK" ]; then
  echo "arm64 split APK not found in XAPK."
  exit 1
fi

unzip -q "$SPLIT_APK" 'lib/arm64-v8a/*' -d "$WORK_DIR/arm64"

for name in libipcamera.so libIOTCAPIs.so libPPCS_API.so libAVAPIs.so libaw_net_client.so libviigoplus.so; do
  found="$(find "$WORK_DIR/arm64" -name "$name" -print -quit)"
  if [ -n "$found" ]; then
    cp "$found" "$OUT_DIR/$name"
    echo "copied $name"
  else
    echo "missing $name"
  fi
done

echo "Done. Base APK inspected at: ${BASE_APK:-not found}"
