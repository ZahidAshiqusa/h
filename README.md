# ZAID ASHIQ X ULTRA V. CODER

**Your Personal WhatsApp Automation Suite — 100% Standalone Android Application**

## Core Philosophy

`INSTALL → OPEN → PAIR WHATSAPP → START`

ZAID ASHIQ X ULTRA V. CODER operates entirely on the user's Android device without requiring any remote backend, cloud database, Railway/Render hosting, VPS, or user account login.

## Key Capabilities

1. **Zero-Account First-Start Experience**: Opens directly to the command dashboard with **CONNECT WHATSAPP**, **START ENGINE**, and **SETTINGS**.
2. **WhatsApp Device Pairing Workspace**:
   - **QR Code Pairing**: Generates scannable multi-device QR codes with auto-refresh and Linked Devices guidance.
   - **8-Character Pairing Code**: Normalizes international E.164 phone numbers and generates formatted 8-character pairing codes (`XXXX-XXXX`).
   - **Android Keystore Session Security**: Persists authenticated session credentials encrypted with hardware-backed `AES/GCM/NoPadding` (`256-bit`) keys in `files/whatsapp/auth/creds.json.enc`.
3. **Hybrid On-Device Runtime & Proof-of-Concept Verifier**:
   - Bundles the JavaScript engine, Baileys multi-device client adapter, 13 core prefix commands, and 4 starter plugins inside APK assets.
   - Includes a real-time **Runtime Proof-of-Concept Verifier** in the Pairing & Session workspace that validates JS asset extraction, Noise/HMAC-SHA256/EC cryptography, Android Keystore AES-256-GCM read/write, OkHttp WebSocket readiness, and `127.0.0.1:19480` local Node IPC bridge status.
4. **Native Foreground Service**:
   - Persistent status notification with direct **Stop Engine** and **Reconnect** actions, bounded wake lock management, and automatic network connectivity monitoring with exponential backoff (`2s → 4s → 8s → 16s → 32s`).
5. **12 Functional Dashboard Modules**:
   - Device Pairing, WhatsApp Automation, Message Center, Contacts Manager (with VCF 3.0 export), Chat Export (JSON & TXT transcripts), Status Publisher (Zero-Permission Android Photo Picker), Local Multilingual Responder (Offline English, Urdu & Roman Urdu), Task Scheduler (SQLite + JobScheduler), Command Library (`.help`, `.menu`, `.ping`, `.info`, `.owner`, `.time`, `.date`, `.status`, `.schedule`, `.cancel`, `.autoreply`, `.autoreply on`, `.autoreply off`), Plugin Manager, Activity Logs, and Settings.
