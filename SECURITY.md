# SECURITY ARCHITECTURE — ZAID ASHIQ X ULTRA V. CODER

## 1. Session Credential Protection
- All WhatsApp session credentials (`creds.json.enc`) are encrypted at rest inside app-private storage (`files/whatsapp/auth/`) using `AES/GCM/NoPadding` with a 256-bit `SecretKey` managed by the **Android Keystore** (`zaid_ultra_whatsapp_session_aes256`).
- Every encrypted payload uses a unique 12-byte random IV and 128-bit authentication tag.
- Never stores credentials in plain-text shared storage.

## 2. Strictly Offline / Zero External Cloud Transmission
- No login, signup, or cloud database is used.
- No external AI provider API calls are made; multilingual automation (English, Urdu, Roman Urdu) executes 100% locally on-device.

## 3. Least-Privilege Android Permissions
- `INTERNET` & `ACCESS_NETWORK_STATE`: Required for WhatsApp connection and network recovery monitoring.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: Keeps the local automation engine running visibly with a persistent notification.
- `POST_NOTIFICATIONS`: Requested at runtime with a clear explanation dialog on Android 13+.
- Media selection uses the zero-permission Android Photo Picker (`PickVisualMedia`).
- Does **not** request SMS, Call Logs, Accessibility, Device Contacts, or Location permissions.

## 4. Prohibited Surveillance Exclusions
- Contains zero SIM database scraping, CNIC lookup, view-once interception, deleted-message interception, or hidden tracking features.
- All contact (`.vcf`) and chat (`.json` / `.txt`) exports are strictly user-initiated and require confirmation.
