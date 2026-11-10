#!/usr/bin/env bash
set -euo pipefail
echo "Building ZAID ASHIQ X ULTRA V. CODER Release APK..."
gradle :app:assembleRelease
echo "Release APK generated at: app/build/outputs/apk/release/app-release.apk"
