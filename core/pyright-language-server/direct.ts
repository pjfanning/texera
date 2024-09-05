import { resolve } from 'node:path';
import { runPythonServer } from './main.js';
import { getLocalDirectory } from './server-commons.js';
import fs from 'fs';
import yaml from 'js-yaml';

const baseDir = getLocalDirectory(import.meta.url);
const relativeDir = "./node_modules/pyright/dist/pyright-langserver.js";

const configFilePath = resolve(baseDir, 'pythonLanguageServerConfig.json');
const config = JSON.parse(fs.readFileSync(configFilePath, 'utf-8'));

const amberConfigFilePath = resolve(baseDir, config.amberConfigFilePath);
const amberConfigContent = fs.readFileSync(amberConfigFilePath, 'utf-8');
const applicationConfig = yaml.load(amberConfigContent) as Record<string, any>;

const pythonLanguageServerPort = applicationConfig['python-language-server'].port;

runPythonServer(baseDir, relativeDir, pythonLanguageServerPort);