'use strict';

const fs = require('fs');
const path = require('path');

class PluginLoader {
  constructor(pluginsDir) {
    this.pluginsDir = pluginsDir;
    this.loaded = [];
  }

  discoverPlugins() {
    if (!fs.existsSync(this.pluginsDir)) return [];
    const files = fs.readdirSync(this.pluginsDir).filter((f) => f.endsWith('.js'));
    this.loaded = files.map((file) => path.join(this.pluginsDir, file));
    return this.loaded;
  }
}

module.exports = { PluginLoader };
