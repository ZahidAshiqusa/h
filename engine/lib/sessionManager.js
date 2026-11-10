'use strict';

const fs = require('fs');
const path = require('path');

class SessionManager {
  constructor(authDir) {
    this.authDir = authDir;
    if (!fs.existsSync(this.authDir)) {
      fs.mkdirSync(this.authDir, { recursive: true });
    }
  }

  getCredsPath() {
    return path.join(this.authDir, 'creds.json.enc');
  }

  hasSavedCredentials() {
    return fs.existsSync(this.getCredsPath());
  }

  deleteSession() {
    if (fs.existsSync(this.getCredsPath())) {
      fs.unlinkSync(this.getCredsPath());
    }
  }
}

module.exports = { SessionManager };
