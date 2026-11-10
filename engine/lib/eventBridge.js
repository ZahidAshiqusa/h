'use strict';

const EventEmitter = require('events');

class EventBridge extends EventEmitter {
  constructor(port = 19480) {
    super();
    this.port = port;
  }

  emitState(state, meta = {}) {
    this.emit('stateChange', { state, timestamp: Date.now(), ...meta });
  }

  log(level, message) {
    this.emit('log', { level, message, timestamp: Date.now() });
  }
}

module.exports = { EventBridge };
