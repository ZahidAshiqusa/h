#!/usr/bin/env bash
set -euo pipefail
echo "Building ZAID ASHIQ X ULTRA V. CODER Debug APK..."
gradle :app:assembleDebug
echo "Debug APK generated at: app/build/outputs/apk/debug/app-debug.apk"
