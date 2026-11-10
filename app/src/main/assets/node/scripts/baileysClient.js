/**
 * ZAID ASHIQ X ULTRA V. CODER — Baileys Multi-Device Client Adapter
 * Executes inside bundled Node.js runtime or bridges with Kotlin WebSocket/Noise transport.
 */
'use strict';

class BaileysMultiDeviceAdapter {
  constructor(options = {}) {
    this.authDir = options.authDir || './auth_info_baileys';
    this.state = 'DISCONNECTED';
    this.reconnectAttempts = 0;
    this.maxReconnectDelayMs = 32000;
    this.sessionLocked = false;
  }

  computeBackoffDelay(attempt) {
    const base = 2000;
    const exp = Math.min(attempt, 4);
    return Math.min(base * Math.pow(2, exp), this.maxReconnectDelayMs);
  }

  normalizePhoneNumber(rawPhone) {
    const digits = String(rawPhone || '').replace(/[^0-9]/g, '');
    if (digits.length < 10 || digits.length > 15) {
      throw new Error('Phone number must be between 10 and 15 digits in international E.164 format.');
    }
    return digits;
  }

  formatPairingCode(rawSeed) {
    const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let out = '';
    for (let i = 0; i < 8; i++) {
      const idx = (rawSeed.charCodeAt(i % rawSeed.length) + i * 17) % alphabet.length;
      out += alphabet[idx];
    }
    return out.slice(0, 4) + '-' + out.slice(4, 8);
  }
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = { BaileysMultiDeviceAdapter };
}
