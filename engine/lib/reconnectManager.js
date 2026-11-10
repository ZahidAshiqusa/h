'use strict';

class ReconnectManager {
  constructor(baseDelayMs = 2000, maxDelayMs = 32000) {
    this.baseDelayMs = baseDelayMs;
    this.maxDelayMs = maxDelayMs;
    this.attempts = 0;
  }

  nextDelayMs() {
    this.attempts += 1;
    const exp = Math.min(this.attempts - 1, 4);
    return Math.min(this.baseDelayMs * Math.pow(2, exp), this.maxDelayMs);
  }

  reset() {
    this.attempts = 0;
  }
}

module.exports = { ReconnectManager };
