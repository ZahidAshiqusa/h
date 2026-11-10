'use strict';

const { BaileysClient } = require('./lib/baileysClient');
const { SessionManager } = require('./lib/sessionManager');
const { ReconnectManager } = require('./lib/reconnectManager');
const { EventBridge } = require('./lib/eventBridge');
const { CommandHandler } = require('./lib/commandHandler');
const { MessageHandler } = require('./lib/messageHandler');
const { LocalScheduler } = require('./lib/scheduler');
const { PluginLoader } = require('./lib/pluginLoader');
const { LocalDatabase } = require('./lib/localDatabase');

class ZaidUltraEngine {
  constructor(config = {}) {
    this.db = new LocalDatabase(config.storageDir || './data');
    this.eventBridge = new EventBridge(config.ipcPort || 19480);
    this.sessionManager = new SessionManager(config.authDir || './auth');
    this.reconnectManager = new ReconnectManager();
    this.pluginLoader = new PluginLoader(config.pluginsDir || './plugins');
    this.commandHandler = new CommandHandler(config.prefix || '.');
    this.scheduler = new LocalScheduler(this.db, this.eventBridge);
    this.baileysClient = new BaileysClient({
      sessionManager: this.sessionManager,
      reconnectManager: this.reconnectManager,
      eventBridge: this.eventBridge
    });
    this.messageHandler = new MessageHandler({
      commandHandler: this.commandHandler,
      pluginLoader: this.pluginLoader,
      eventBridge: this.eventBridge
    });
  }

  async start() {
    this.pluginLoader.discoverPlugins();
    this.eventBridge.emitState('INITIALIZING');
    await this.baileysClient.connect();
    this.scheduler.start();
  }

  async stop() {
    this.scheduler.stop();
    await this.baileysClient.disconnect();
    this.eventBridge.emitState('DISCONNECTED');
  }
}

if (require.main === module) {
  const engine = new ZaidUltraEngine();
  engine.start().catch((err) => {
    console.error('[ZaidUltraEngine] Fatal startup error:', err);
  });
}

module.exports = { ZaidUltraEngine };
