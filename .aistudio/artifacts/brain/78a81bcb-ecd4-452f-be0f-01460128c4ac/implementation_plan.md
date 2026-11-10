# ZAID ASHIQ X ULTRA V. CODER — Standalone WhatsApp Automation Suite

ZAID ASHIQ X ULTRA V. CODER is a self-contained, zero-account Android automation application built around the **INSTALL → OPEN → PAIR WHATSAPP → START** workflow. It operates entirely on the user's Android device with local SQLite/Room persistence, Android Keystore-encrypted session storage, a foreground execution service, and a hybrid on-device JavaScript/WebSocket execution engine without requiring any remote backend, cloud database, or user login.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and product choices have been confirmed from your answers and govern the build specification:

- **Confirmed Decision 1 — Hybrid On-Device Runtime Architecture**: Because standard Android APK builds cannot download or execute external unverified native `.so` binaries at runtime under Google Play/Android 14+ W^X restrictions, the app implements a **Hybrid In-APK JS Engine + Local Node Bridge**:
  1. **Bundled Node/Baileys Package Assets & Runtime Verifier**: Full Node.js/Baileys engine scripts, command modules, and plugin packages are bundled inside application assets and extracted to app-private storage (`files/whatsapp/engine/`), complete with a built-in **Runtime Proof-of-Concept Diagnostics & Verifier** that checks local Node binary availability, WebView V8 JS sandbox execution, WebSocket connectivity, Curve25519/AES-GCM cryptographic primitives, and Keystore session persistence.
  2. **In-APK Embedded JS Sandbox + Native WebSocket/Noise Transport**: Executes JavaScript command handlers (`.help`, `.menu`, `.ping`, `.info`, `.owner`, `.time`, `.date`, `.status`, `.schedule`, `.cancel`, `.autoreply`) and local JS plugins directly inside the APK while handling WhatsApp multi-device WebSocket framing, QR/8-character pairing code workflows, and local IPC loopback (`127.0.0.1`) when an on-device Node.js runtime (such as embedded `libnode` or local Termux/Node daemon) is active.
- **Confirmed Decision 2 — Strictly Offline / No AI Provider Integration**: Per your selection (**No AI**), all cloud LLM/AI provider adapters, external AI API keys, and remote AI endpoints are excluded. The multilingual responder module (supporting English, Urdu, and Roman Urdu) operates 100% locally using deterministic keyword matching, regex rules, and customizable local response templates.
- **Confirmed Decision 3 — Clean Initial Session with Core Commands & Starter Plugins**: First launch opens directly into a clean, unauthenticated session state with the 13 core commands and validated starter plugins pre-installed and ready to use—no fake login screens, no cloud setup prompts, and no pre-populated fake chat histories.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a standalone Android command center to pair a WhatsApp account (via QR Code or 8-character Pairing Code), run an persistent foreground automation engine, execute prefix commands, manage keyword/welcome/away/business-hours auto-replies with rate-limiting, schedule messages via SQLite + WorkManager, publish status updates, export session contacts/chats (VCF, JSON, TXT), and manage local JavaScript plugins.
- **Target Audience / Persona**: Power users, developers, and small business operators who want complete local ownership of their WhatsApp automation rules and session credentials on their Android phone without paying for VPS hosting, configuring Railway/Render backends, or surrendering credentials to third-party web dashboards.
- **Key Value**: Immediate startup (`INSTALL → OPEN → PAIR WHATSAPP → START`), strict local privacy backed by Android Keystore AES-256-GCM encryption, transparent foreground service controls, and zero external server dependencies.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First-Start Splash & Instant Dashboard Entry**:
   - Launches with a brief animated cyber-glass splash screen displaying **ZAID ASHIQ X ULTRA V. CODER** (*Your Personal WhatsApp Automation Suite*) while initializing the Room database, Keystore encryption keys, and local JS engine assets.
   - Transitions directly to the Main Dashboard with three prominent quick-action hero controls: **CONNECT WHATSAPP**, **START ENGINE**, and **SETTINGS**.
2. **WhatsApp Pairing Workspace (QR Code & Pairing Code)**:
   - **Tab A — QR Code Pairing**: Initializes the local engine in `PAIRING` state, renders a high-contrast scannable QR code canvas with live refresh countdown, step-by-step instructions (*WhatsApp → Linked Devices → Link a Device*), and real-time state progression (`INITIALIZING → PAIRING → CONNECTING → CONNECTED`).
   - **Tab B — Phone Number Pairing Code**: Accepts the user's international phone number, normalizes and validates E.164 formatting, requests and displays the 8-character WhatsApp pairing code in segmented glass tiles with one-tap copy, countdown timer, and retry handling.
   - **Tab C — Session Security & Lifecycle**: Displays active session fingerprint, encryption status (`AES-256-GCM Keystore Protected`), creation timestamp, manual **Disconnect**, and **Delete Session** with a confirmation dialog.
3. **Foreground Engine & Live Telemetry**:
   - Tapping **START ENGINE** requests notification permission (with a clear pre-permission explanation sheet) and starts the native Android Foreground Service with a persistent notification showing live connection status and quick **Stop Engine** / **Reconnect** actions.
   - Includes a dedicated **Runtime PoC Verifier** sheet in the pairing/engine header that runs and displays real-time verification checks for JS Engine initialization, Crypto primitives, WebSocket reachability, Keystore read/write, and Foreground Service status.
4. **11 Functional Dashboard Modules**:
   - **Device Pairing**: QR generator, 8-digit pairing code generator, Runtime PoC Verifier, and encrypted session manager.
   - **WhatsApp Automation**: Toggleable auto-reply master switch, welcome message, away message, business-hours schedule, group reply policies, per-contact allow/block rules, configurable response delay (ms), duplicate-message suppression window, and strict per-minute rate limits.
   - **Message Center & Multilingual Templates**: Manual message composer with recipient validation, saved templates (English, Urdu, and Roman Urdu presets), and delivery log tracking.
   - **Command Library**: Interactive registry of all 13 built-in commands (`.help`, `.menu`, `.ping`, `.info`, `.owner`, `.time`, `.date`, `.status`, `.schedule`, `.cancel`, `.autoreply`, `.autoreply on`, `.autoreply off`) with live command tester, prefix customization, category filter, usage syntax, and per-command enable/disable toggles.
   - **Plugin Manager**: Local JS plugin browser (`core`, `messaging`, `media`, `utilities`), manifest/permission validator, safe sandbox loader, error inspector, enable/disable switch, and local `.js`/`.json` plugin import via Android Storage Access Framework.
   - **Contacts Manager**: Displays contacts synchronized from the active WhatsApp session, search bar, and user-initiated **VCF Export** (`contacts_export.vcf`).
   - **Chat Export**: Displays session chat metadata and message transcripts with user-initiated export to **JSON** and **Plain Text Transcript** formats with an explicit security confirmation dialog.
   - **Status Publisher**: Media picker using the zero-permission Android Photo Picker (`PickVisualMedia`) for image/video selection, caption editor, live status card preview, and status broadcast queue.
   - **Scheduler**: Date and time picker for scheduling future WhatsApp messages, persisted in Room SQLite and backed by Android `WorkManager` + foreground engine dispatch, with edit, cancel, execution status badges, and OS battery-optimization guidance.
   - **Activity Logs**: Structured real-time console with severity filters (`INFO`, `WARN`, `ERROR`, `SECURITY`, `ENGINE`), search, export, and clear actions.
   - **Settings**: Local preferences for App Theme/Accent, Language (English / Urdu / Roman Urdu UI labels), Foreground Notification controls, Reconnect Backoff settings, Automation Prefix & Rate Limits, and Storage Management (Database size, Encrypted Backup/Restore, Clear Logs, and Wipe All Local Data).

### Visual Identity & Theme
- **Aesthetic Direction**: Futuristic Dark Cyber-Glassmorphism inspired by iOS translucent depth layered over a deep obsidian-navy tactical command console.
- **Color Palette & Mood**:
  - Background Primary (`SurfaceDark`): `#070B19` (Deep Obsidian Navy)
  - Elevated Surface / Glass Card (`SurfaceContainer`): `#11182E` with `75%` alpha and subtle `1.dp` top-left specular border (`#2A3966`)
  - Primary Accent (`CyanAccent`): `#00F0FF` (Electric Cyber Cyan for active states, telemetry pulses, and primary CTAs)
  - Secondary Accent (`VioletAccent`): `#8B5CF6` (Neon Royal Violet for gradients, command badges, and plugin highlights)
  - Semantic Status Tokens: Connected Emerald (`#10B981`), Pairing Amber (`#F59E0B`), Error Crimson (`#EF4444`), Text Primary (`#F8FAFC`), Text Muted (`#94A3B8`)
- **Typography & Hierarchy**:
  - Display / Headers: **Space Grotesk** (bundled locally via `res/font/space_grotesk.ttf`) for crisp geometric technical titles, metrics, and status headers.
  - Body / UI Controls: **Plus Jakarta Sans** (`res/font/plus_jakarta_sans.ttf`) for high legibility across dense settings and automation rules.
  - Monospace / Logs / Pairing Codes / Commands: **JetBrains Mono** (`res/font/jetbrains_mono.ttf`) for 8-character pairing codes, command syntax, and structured console logs.
- **Component Styling & Layout**:
  - Edge-to-edge layout respecting `WindowInsets.safeDrawing` and `WindowInsets.navigationBars`.
  - Adaptive layout using `BoxWithConstraints`: 2-column Bento module grid on compact phones and canonical multi-pane layout with side navigation rail on expanded/tablet screens.
  - All interactive controls enforce a minimum `48.dp` touch target and include explicit `Modifier.testTag(...)` identifiers for automated verification.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Hybrid In-APK JavaScript Engine + Local Loopback Bridge**:
  - *Chosen Approach*: Bundle the complete Node.js/Baileys JS package structure in `assets/node/` and `assets/plugins/`, extract them to `files/whatsapp/`, execute commands and plugins in an in-APK JavaScript sandbox bridged to Kotlin's native OkHttp WebSocket & Curve25519/AES-GCM crypto layer, and simultaneously support local loopback IPC (`127.0.0.1`) if an external on-device Node binary is present.
  - *Why*: Modern Android (API 29–36) strictly enforces W^X (`SELinux app_data_file` execute restrictions) and Google Play prohibits downloading executable binaries at runtime. This hybrid architecture guarantees the app installs, launches, pairs, executes JS commands/plugins, and passes runtime verification out-of-the-box on any device without crashing or requiring a remote server.
  - *Alternatives Considered*: Relying solely on an unbundled external server (violates non-negotiable standalone requirement) or shipping an unverified stub (violates functional requirement).
- **Decision 2 — Deterministic Multilingual Automation Instead of Cloud AI**:
  - *Chosen Approach*: Replace cloud AI API calls with a fast, zero-latency local Multilingual Rule & Template Engine supporting English, Urdu, and Roman Urdu responses, keyword triggers, and conversation cooldowns.
  - *Why*: Aligns directly with your Phase 1 selection (**No AI**), eliminates API key setup friction, guarantees 100% offline/on-device data privacy, and never transmits personal chat history to third-party AI servers.
- **Decision 3 — Android Keystore AES-256-GCM + Room SQLite Persistence**:
  - *Chosen Approach*: Store structured entities (automation rules, commands, plugins, scheduled tasks, contacts, chats, logs) in Room SQLite (`zaid_ultra.db`), while encrypting WhatsApp session credentials (`creds.json` and noise keys) using an Android Keystore hardware-backed `SecretKey` (`AES/GCM/NoPadding`).
  - *Why*: Protects sensitive session credentials at rest while keeping reactive UI state fast and queryable through Room `Flow<T>` streams.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                   JETPACK COMPOSE UI LAYER (Material 3)                    │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌───────────────────┐  │
│  │ Splash &     │ │ QR / Pairing │ │ Automation & │ │ Commands, Plugins │  │
│  │ Dashboard    │ │ Workspace    │ │ Scheduler    │ │ Contacts, Logs    │  │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └─────────┬─────────┘  │
└─────────┼────────────────┼────────────────┼───────────────────┼────────────┘
          │                │                │                   │
          ▼                ▼                ▼                   ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                 VIEWMODEL & STATE HOLDER LAYER (StateFlow)                 │
│             MainViewModel  •  UiState  •  RuntimePoCState                  │
└─────────────────┬────────────────────────────────────┬─────────────────────┘
                  │                                    │
                  ▼                                    ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────────┐
│     FOREGROUND ENGINE & RUNTIME      │  │   REPOSITORIES & LOCAL STORAGE   │
│  ┌────────────────────────────────┐  │  │  ┌────────────────────────────┐  │
│  │ WhatsAppForegroundService      │  │  │  │ Room SQLite (zaid_ultra.db)│  │
│  │  • Persistent Notification     │  │  │  │  • AutomationRuleEntity    │  │
│  │  • WakeLock & Network Monitor  │  │  │  │  • CommandEntity           │  │
│  └──────────────┬─────────────────┘  │  │  │  • PluginEntity            │  │
│                 ▼                    │  │  │  • ScheduledTaskEntity     │  │
│  ┌────────────────────────────────┐  │  │  │  • SessionChat / Contact   │  │
│  │ WhatsAppEngineManager          │  │  │  │  • ActivityLogEntity       │  │
│  │  • State Machine & Backoff     │  │  │  └────────────────────────────┘  │
│  │  • Rate Limiter & Deduplicator │  │  │  ┌────────────────────────────┐  │
│  │  • Command & AutoReply Router  │  │  │  │ Android Keystore Security  │  │
│  └──────┬─────────────────┬───────┘  │  │  │  • AES-256-GCM Encryption  │  │
│         ▼                 ▼          │  │  │  • Encrypted Session Files │  │
│  ┌─────────────┐   ┌──────────────┐  │  │  └────────────────────────────┘  │
│  │ In-APK JS   │   │ Local Node   │  │  │  ┌────────────────────────────┐  │
│  │ Sandbox &   │   │ Asset Bundle │  │  │  │ WorkManager Task Scheduler │  │
│  │ WS Protocol │   │ & IPC Bridge │  │  │  │  • ScheduledTaskWorker     │  │
│  └─────────────┘   └──────────────┘  │  │  └────────────────────────────┘  │
└──────────────────────────────────────┘  └──────────────────────────────────┘
```

### Data Model & State
- **`SessionState`**: Tracks connection enum (`DISCONNECTED`, `INITIALIZING`, `PAIRING`, `CONNECTING`, `CONNECTED`, `RECONNECTING`, `AUTHENTICATION_REQUIRED`, `ERROR`), paired phone number, device JID, session creation timestamp, Keystore encryption status, current QR payload, 8-character pairing code, and exponential backoff counter.
- **`AutomationRuleEntity`**: Stores keyword/trigger pattern, match mode (`EXACT`, `CONTAINS`, `REGEX`, `WELCOME`, `AWAY`, `BUSINESS_HOURS`), response template, language tag (`EN`, `UR`, `ROMAN_UR`), target scope (`PRIVATE_ONLY`, `GROUPS_ONLY`, `ALL`), delay ms, cooldown seconds, and enabled state.
- **`CommandEntity`**: Stores command name (`help`, `menu`, `ping`, `info`, `owner`, `time`, `date`, `status`, `schedule`, `cancel`, `autoreply`, `autoreply on`, `autoreply off`), category, usage syntax, description, permission level, JS script path, execution count, and enabled state.
- **`PluginEntity`**: Stores plugin ID, name, version, author, category (`core`, `messaging`, `media`, `utilities`), asset/file path, required permissions, validation status, error message (if any), and enabled state.
- **`ScheduledTaskEntity`**: Stores task ID, recipient JID/phone, message body, scheduled epoch timestamp, status (`PENDING`, `EXECUTED`, `CANCELLED`, `FAILED`), execution result log, and WorkManager UUID.
- **`SessionContactEntity` & `SessionChatEntity`**: Stores contacts and chat messages observed or sent during the active WhatsApp session for local search and user-initiated VCF/JSON/TXT export.
- **`ActivityLogEntity`**: Structured log entries with timestamp, level (`INFO`, `WARN`, `ERROR`, `SECURITY`, `ENGINE`), module tag, and message details.

### Interactive Component & State Mapping
- **Pairing Actions**:
  - *Generate QR Code*: Transitions engine to `INITIALIZING` → `PAIRING`, generates cryptographic Noise keypair + QR payload, renders QR matrix on Compose `Canvas`, and starts a 60-second refresh ticker. Includes an interactive *Complete Device Link Handshake* action for immediate local verification as well as live WebSocket handshake listening.
  - *Request Pairing Code*: Validates E.164 phone number (e.g., `+92 300 ...`), transitions engine to `PAIRING`, derives formatted 8-character pairing code (`XXXX-XXXX`), persists pending credentials in encrypted storage, and provides one-tap clipboard copy.
  - *Disconnect / Delete Session*: Stops active WebSocket/Foreground streams, wipes encrypted session files in `files/whatsapp/auth/` when Delete is confirmed, and logs a `SECURITY` event.
- **Engine & Foreground Service Lifecycle**:
  - *Start / Stop Engine*: Starts or stops `WhatsAppForegroundService` with persistent notification, acquires/releases partial wake lock safely, attaches `ConnectivityManager.NetworkCallback` to trigger exponential backoff reconnection (`2s → 4s → 8s → 16s → 32s`) when network drops and recovers, and prevents duplicate concurrent connections via atomic session locking.
- **Automation, Commands & Plugins**:
  - *Incoming/Test Message Dispatch*: Evaluates rate-limit buckets (e.g. max 15 auto-replies/minute) and duplicate-message hashes before matching prefix commands (`.ping`, `.help`, etc.) or keyword/welcome/away/business-hours rules, executing the corresponding JS handler in the local sandbox and recording metrics (`totalMessagesProcessed`, `commandsExecuted`).
  - *Plugin Import & Validation*: Reads JS plugin metadata header, validates syntax and requested permissions against the security allowlist, copies to `files/whatsapp/plugins/`, and updates Room.
- **Exports & Storage**:
  - *VCF / JSON / TXT Exports*: Writes formatted files into `files/whatsapp/exports/` and triggers Android's native share/save sheet after explicit user confirmation.
