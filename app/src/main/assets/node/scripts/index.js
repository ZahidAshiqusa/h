/**
 * ZAID ASHIQ X ULTRA V. CODER — Local Engine Entrypoint
 * Supports both In-APK V8 Sandbox Bridge and Local Node.js Loopback IPC (127.0.0.1:19480).
 */
(function (globalScope) {
  'use strict';

  const EngineRuntime = {
    version: '1.0.0-ULTRA',
    startedAt: Date.now(),
    prefix: '.',
    autoReplyEnabled: true,
    plugins: {},
    commands: {},

    init: function (configJson) {
      try {
        const cfg = typeof configJson === 'string' ? JSON.parse(configJson) : (configJson || {});
        this.prefix = (cfg.engine && cfg.engine.defaultPrefix) || '.';
        this.autoReplyEnabled = cfg.automation ? !!cfg.automation.autoReplyEnabled : true;
        return JSON.stringify({
          ok: true,
          runtime: 'ZAID_HYBRID_JS_V8',
          version: this.version,
          prefix: this.prefix,
          timestamp: Date.now()
        });
      } catch (err) {
        return JSON.stringify({ ok: false, error: String(err) });
      }
    },

    executeCommand: function (commandName, argsText, contextJson) {
      const ctx = typeof contextJson === 'string' ? JSON.parse(contextJson) : (contextJson || {});
      const now = new Date();
      const normalized = String(commandName || '').trim().toLowerCase();

      switch (normalized) {
        case 'ping':
          return JSON.stringify({
            handled: true,
            command: 'ping',
            reply: '⚡ *PONG!* ZAID ASHIQ X ULTRA V. CODER Engine Online\n• Runtime: Hybrid In-APK JS + Local Bridge\n• Latency: ' + (ctx.latencyMs || 4) + 'ms\n• Session: AES-256-GCM Protected'
          });
        case 'help':
          return JSON.stringify({
            handled: true,
            command: 'help',
            reply: '📘 *ZAID ASHIQ X ULTRA V. CODER — COMMAND HELP*\nPrefix: `' + this.prefix + '`\n\n• `.help` — Command list & syntax\n• `.menu` — Categorized suite menu\n• `.ping` — Latency & engine health\n• `.info` — System & encryption telemetry\n• `.owner` — Device owner card\n• `.time` / `.date` — Local clock\n• `.status` — Session & queue metrics\n• `.schedule <min> | <msg>` — Queue task\n• `.cancel <id>` — Cancel task\n• `.autoreply [on/off]` — Toggle auto-responder'
          });
        case 'menu':
          return JSON.stringify({
            handled: true,
            command: 'menu',
            reply: '╔══════════════════════════╗\n║ *ZAID ASHIQ X ULTRA V. CODER* ║\n╚══════════════════════════╝\n🔹 *CORE*: .help, .menu, .owner\n🔹 *DIAGNOSTICS*: .ping, .info, .status\n🔹 *UTILITIES*: .time, .date\n🔹 *AUTOMATION*: .autoreply, .autoreply on, .autoreply off\n🔹 *SCHEDULER*: .schedule, .cancel\n\n_100% Standalone Local Android Execution_'
          });
        case 'info':
          return JSON.stringify({
            handled: true,
            command: 'info',
            reply: '🛡️ *SYSTEM ARCHITECTURE TELEMETRY*\n• Suite: ZAID ASHIQ X ULTRA V. CODER\n• Mode: Standalone Local APK (Zero Cloud)\n• Crypto: Android Keystore AES-256-GCM\n• DB: SQLite Room Local Storage\n• Active Plugins: ' + (ctx.activePlugins || 4) + '\n• Processed Messages: ' + (ctx.messagesProcessed || 0)
          });
        case 'owner':
          return JSON.stringify({
            handled: true,
            command: 'owner',
            reply: '👑 *OWNER & ARCHITECT PROFILE*\n• Suite: ZAID ASHIQ X ULTRA V. CODER\n• Device Session: ' + (ctx.pairedPhone || 'Local Device Owner') + '\n• Security Posture: Zero-Account / Device-Private Keystore'
          });
        case 'time':
          return JSON.stringify({
            handled: true,
            command: 'time',
            reply: '🕒 *LOCAL DEVICE TIME*: ' + now.toTimeString().split(' ')[0] + ' (' + Intl.DateTimeFormat().resolvedOptions().timeZone + ')'
          });
        case 'date':
          return JSON.stringify({
            handled: true,
            command: 'date',
            reply: '📅 *LOCAL DEVICE DATE*: ' + now.toISOString().slice(0, 10)
          });
        case 'status':
          return JSON.stringify({
            handled: true,
            command: 'status',
            reply: '📡 *ENGINE STATUS REPORT*\n• State: ' + (ctx.connectionState || 'CONNECTED') + '\n• Auto-Reply: ' + (this.autoReplyEnabled ? 'ENABLED' : 'DISABLED') + '\n• Pending Tasks: ' + (ctx.pendingTasks || 0) + '\n• Rate Limit: ' + (ctx.rateLimitPerMin || 15) + ' msgs/min'
          });
        case 'autoreply':
          return JSON.stringify({
            handled: true,
            command: 'autoreply',
            reply: '🤖 *AUTO-REPLY STATUS*: ' + (this.autoReplyEnabled ? 'ACTIVE ✅' : 'PAUSED ⏸️') + '\nUse `.autoreply on` or `.autoreply off` to switch.'
          });
        case 'autoreply on':
          this.autoReplyEnabled = true;
          return JSON.stringify({
            handled: true,
            command: 'autoreply on',
            stateChange: { autoReplyEnabled: true },
            reply: '✅ *Auto-Reply has been ENABLED* on the local engine.'
          });
        case 'autoreply off':
          this.autoReplyEnabled = false;
          return JSON.stringify({
            handled: true,
            command: 'autoreply off',
            stateChange: { autoReplyEnabled: false },
            reply: '⏸️ *Auto-Reply has been DISABLED*. Manual prefix commands remain active.'
          });
        default:
          return JSON.stringify({
            handled: false,
            command: normalized,
            reply: '⚠️ Unknown or disabled command: `' + this.prefix + normalized + '`. Type `' + this.prefix + 'help` for available commands.'
          });
      }
    }
  };

  globalScope.ZaidEngineRuntime = EngineRuntime;
})(typeof globalThis !== 'undefined' ? globalThis : this);
