#!/usr/bin/env bash
set -euo pipefail
echo "=== ZAID ASHIQ X ULTRA V. CODER — RUNTIME & UNIT VERIFICATION ==="
node -e "const { ZaidUltraEngine } = require('./engine/index.js'); const e = new ZaidUltraEngine(); console.log('Node Engine Module Load OK:', typeof e.start === 'function');"
gradle :app:testDebugUnitTest
echo "=== ALL RUNTIME AND ROBOLECTRIC CHECKS PASSED ==="
