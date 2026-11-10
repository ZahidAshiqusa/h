'use strict';

const fs = require('fs');
const path = require('path');

class LocalDatabase {
  constructor(storageDir) {
    this.storageDir = storageDir;
    if (!fs.existsSync(this.storageDir)) {
      fs.mkdirSync(this.storageDir, { recursive: true });
    }
  }

  readJson(fileName, fallback = {}) {
    const target = path.join(this.storageDir, fileName);
    if (!fs.existsSync(target)) return fallback;
    return JSON.parse(fs.readFileSync(target, 'utf8'));
  }

  writeJson(fileName, data) {
    const target = path.join(this.storageDir, fileName);
    fs.writeFileSync(target, JSON.stringify(data, null, 2), 'utf8');
  }
}

module.exports = { LocalDatabase };
