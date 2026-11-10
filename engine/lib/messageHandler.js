'use strict';

class MessageHandler {
  constructor({ commandHandler, pluginLoader, eventBridge }) {
    this.commandHandler = commandHandler;
    this.pluginLoader = pluginLoader;
    this.eventBridge = eventBridge;
    this.recentTimestamps = [];
    this.maxRepliesPerMin = 15;
  }

  allowRateLimit() {
    const now = Date.now();
    this.recentTimestamps = this.recentTimestamps.filter((t) => now - t < 60000);
    if (this.recentTimestamps.length >= this.maxRepliesPerMin) {
      return false;
    }
    this.recentTimestamps.push(now);
    return true;
  }
}

module.exports = { MessageHandler };
