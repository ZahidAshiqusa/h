'use strict';
module.exports = {
  name: 'help',
  category: 'Core',
  usage: '.help [command]',
  description: 'List all enabled automation commands and usage syntax',
  permissionLevel: 'PUBLIC',
  enabled: true,
  execute: (ctx) => `📘 *ZAID ASHIQ X ULTRA V. CODER HELP*\nPrefix: ${ctx.prefix || '.'}\nAvailable: help, menu, ping, info, owner, time, date, status, schedule, cancel, autoreply`
};
