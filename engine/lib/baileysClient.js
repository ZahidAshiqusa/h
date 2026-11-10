'use strict';

const crypto = require('crypto');

class BaileysClient {
  constructor({ sessionManager, reconnectManager, eventBridge }) {
    this.sessionManager = sessionManager;
    this.reconnectManager = reconnectManager;
    this.eventBridge = eventBridge;
    this.state = 'DISCONNECTED';
    this.locked = false;
  }

  async connect() {
    if (this.locked) {
      this.eventBridge.log('WARN', 'Duplicate connection prevented by session lock.');
      return;
    }
    this.locked = true;
    this.state = 'CONNECTING';
    this.eventBridge.emitState(this.state);

    const hasSession = this.sessionManager.hasSavedCredentials();
    if (!hasSession) {
      this.state = 'AUTHENTICATION_REQUIRED';
      this.eventBridge.emitState(this.state);
      return;
    }

    this.state = 'CONNECTED';
    this.reconnectManager.reset();
    this.eventBridge.emitState(this.state);
  }

  requestPairingCode(e164Phone) {
    const clean = String(e164Phone || '').replace(/[^0-9]/g, '');
    if (clean.length < 10 || clean.length > 15) {
      throw new Error('Invalid E.164 phone number length.');
    }
    const digest = crypto.createHash('sha256').update(clean + ':' + Date.now()).digest('hex').toUpperCase();
    const code = digest.slice(0, 4) + '-' + digest.slice(4, 8);
    this.state = 'PAIRING';
    this.eventBridge.emitState(this.state, { pairingCode: code, phone: '+' + clean });
    return code;
  }

  async disconnect() {
    this.locked = false;
    this.state = 'DISCONNECTED';
    this.eventBridge.emitState(this.state);
  }
}

module.exports = { BaileysClient };
