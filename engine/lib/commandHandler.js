'use strict';

class CommandHandler {
  constructor(prefix = '.') {
    this.prefix = prefix;
    this.registry = new Map();
    this.registerDefaults();
  }

  registerDefaults() {
    const names = [
      'help', 'menu', 'ping', 'info', 'owner',
      'time', 'date', 'status', 'schedule', 'cancel',
      'autoreply', 'autoreply on', 'autoreply off'
    ];
    names.forEach((name) => {
      this.registry.set(name, { name, enabled: true });
    });
  }

  isCommand(text) {
    return typeof text === 'string' && text.trim().startsWith(this.prefix);
  }
}

module.exports = { CommandHandler };
