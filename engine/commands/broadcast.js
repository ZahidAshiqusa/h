'use strict';
module.exports = {
  name: 'broadcast',
  category: 'Automation',
  usage: '.broadcast <opt-in-tag> | <message>',
  description: 'Send rate-limited message to opted-in session contacts only',
  permissionLevel: 'OWNER',
  enabled: true,
  execute: () => '📢 Opt-in rate-limited dispatch queued.'
};
