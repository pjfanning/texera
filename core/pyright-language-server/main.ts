import { resolve } from 'node:path';
import { IncomingMessage } from 'node:http';
import { runLanguageServer } from './language-server-runner.js';
import { LanguageName } from './server-commons.js';

export const runPythonServer = (baseDir: string, relativeDir: string, serverPort: number) => {
    const processRunPath = resolve(baseDir, relativeDir);
    runLanguageServer({
        serverName: 'PYRIGHT',
        pathName: '/pyright',
        serverPort: serverPort,
        runCommand: LanguageName.node,
        runCommandArgs: [
            processRunPath,
            '--stdio'
        ],
        wsServerOptions: {
            noServer: true,
            perMessageDeflate: false,
            clientTracking: true,
        }
    });
};