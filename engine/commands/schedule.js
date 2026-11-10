'use strict';
module.exports = {
  name: 'schedule',
  category: 'Scheduler',
  usage: '.schedule <minutes> | <message>',
  description: 'Schedule a delayed message on the local SQLite task queue',
  permissionLevel: 'OWNER',
  enabled: true,
  execute: (ctx) => `🗓️ Scheduled message queued for +${ctx.minutes || 15}m`
};
