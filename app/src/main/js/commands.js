// commands.js — Single source of truth for RPC command IDs.
// Any change here MUST be mirrored in Commands.swift.

module.exports = {
  // ── JS → Swift events (fire-and-forget) ──────────────────────────────────
  CMD_READY:             1,
  CMD_PEER_CONNECTED:    2,
  CMD_PEER_DISCONNECTED: 3,
  CMD_TRANSFER_STARTED:  4,
  CMD_TRANSFER_PROGRESS: 5,
  CMD_TRANSFER_COMPLETE: 6,
  CMD_ERROR:             7,
  CMD_SAVED_PEERS:       11,

  // ── Swift → JS requests (expect a reply) ─────────────────────────────────
  CMD_SEND_FILE:         8,
  CMD_CONNECT_PEER:      9,
  CMD_SET_DOWNLOAD_PATH: 10,
  CMD_FORGET_PEER:       12
}
