const term = new Terminal({
    cols: 80,
    rows: 24,
    cursorBlink: true,
    cursorStyle: "block",
    scrollback: 1000,
    tabStopWidth: 4
});
const socket = new WebSocket('ws://localhost:8080/terminal');
const attachAddon = new AttachAddon.AttachAddon(socket);
//
term.loadAddon(attachAddon);
term.open(document.getElementById('terminal'));