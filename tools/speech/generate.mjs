import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { createRequire } from 'node:module';
import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
// The Emscripten Node build expects CommonJS globals even in its ES module wrapper.
globalThis.require = createRequire(import.meta.url);
const modulePath = dirname(globalThis.require.resolve('espeak-ng'));
globalThis.__dirname = modulePath;
const { default: ESpeakNg } = await import('espeak-ng');
const base = new URL('../../src/main/resources/com/positiveocellus/', import.meta.url);
const pack = JSON.parse(readFileSync(new URL('voice-pack.json', base), 'utf8'));
const out = new URL('audio/', base);
mkdirSync(out, { recursive: true });
for (const line of pack.lines) {
  const engine = await ESpeakNg({
    arguments: ['-v', 'en-us', '-s', '145', '-p', '32', '-w', 'voice.wav', line.positive],
    wasmBinary: readFileSync(modulePath + '/espeak-ng.wasm'),
    print: () => {}, printErr: () => {}
  });
  writeFileSync(new URL(line.wav, out), engine.FS.readFile('voice.wav'));
  console.log(line.key);
}
