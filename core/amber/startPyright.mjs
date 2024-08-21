import express from 'express';
import { WebSocketServer } from 'ws';
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';
import { WebSocketMessageReader, WebSocketMessageWriter } from 'vscode-ws-jsonrpc';
import { createConnection, createServerProcess, forward } from 'vscode-ws-jsonrpc/server';

const __filename = fileURLToPath(import.meta.url);
const dir = dirname(__filename);

const app = express();
app.use(express.static(dir));
const server1 = app.listen(3000);

const wss = new WebSocketServer({ noServer: true });

server1.on('upgrade', (request, socket, head) => {
    wss.handleUpgrade(request, socket, head, (ws) => {
        wss.emit('connection', ws, request);
    });
});

const startPyrightServer = () => {
    const baseDir = resolve(dir);
    const relativeDir = '../../node_modules/pyright/dist/pyright-langserver.js';
    const ls = resolve(baseDir, relativeDir);
    console.log(`Resolved Pyright language server path: ${ls}`);
    return createServerProcess('pyright', 'node', [ls, '--stdio']);
};

wss.on('connection', (ws) => {
    console.log('New WebSocket connection established.');

    //start a new server each time the websocket is on
    const serverConnection = startPyrightServer();

    const socket = {
        send: (content) => ws.send(content),
        onMessage: (cb) => ws.on('message', cb),
        onError: (cb) => ws.on('error', cb),
        onClose: (cb) => {
            ws.on('close', () => {
                console.log('WebSocket connection closed.');
                cb();
            });
        },
        dispose: () => {
            if (ws.readyState === ws.OPEN) {
                ws.close();
            }
        }
    };

    const reader = new WebSocketMessageReader(socket);
    const writer = new WebSocketMessageWriter(socket);
    const socketConnection = createConnection(reader, writer, () => socket.dispose());

    forward(socketConnection, serverConnection, message => {
        return message;
    });

    ws.on('close', () => {
        console.log('WebSocket connection closed.');
        socket.dispose();
    });
});
