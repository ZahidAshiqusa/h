'use strict';

class LocalScheduler {
  constructor(db, eventBridge) {
    this.db = db;
    this.eventBridge = eventBridge;
    this.timer = null;
  }

  start() {
    if (this.timer) return;
    this.timer = setInterval(() => {
      this.eventBridge.log('INFO', 'Scheduler watchdog tick.');
    }, 15000);
  }

  stop() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }
}

module.exports = { LocalScheduler };
