#!/usr/bin/env node

const readline = require('readline');
const fs = require('fs');
const { spawn } = require('child_process');

// === Configuration ===

const HISTORY_FILE = '.run.history.json';
const MAX_HISTORY = 10;

const LOG_FILTERS = [
  'Unable to find an exact match for CDP version',
  'org.openqa.selenium.devtools.CdpVersionFinder findNearestMatch'
];

// === ANSI ===

const ESC = '\x1b';
const CLEAR_LINE = `${ESC}[2K`;
const CURSOR_UP = (n) => `${ESC}[${n}A`;
const CURSOR_DOWN = (n) => `${ESC}[${n}B`;
const HIDE_CURSOR = `${ESC}[?25l`;
const SHOW_CURSOR = `${ESC}[?25h`;
const REVERSE = `${ESC}[7m`;
const RESET = `${ESC}[0m`;
const DIM = `${ESC}[2m`;
const BOLD = `${ESC}[1m`;
const GREEN = `${ESC}[32m`;
const RED = `${ESC}[31m`;
const CYAN = `${ESC}[36m`;

// === Modules ===

const pom = fs.readFileSync('pom.xml', 'utf8');
const modules = [...pom.matchAll(/module>vaadin-([^<]+)-flow-parent<\/module/g)].map((m) => m[1]).sort();

const itModule = (m) => `vaadin-${m}-flow-parent/vaadin-${m}-flow-integration-tests`;
const flowModule = (m) => `vaadin-${m}-flow-parent/vaadin-${m}-flow`;

// === Mode Definitions ===

const indexWidth = String(modules.length).length;
const maxModuleLen = Math.max(...modules.map((m) => m.length));
const moduleItems = modules.map((m, i) => {
  const paddedIndex = `${String(i + 1).padStart(indexWidth)}`;
  const paddedModule = m.padEnd(maxModuleLen);
  return { label: `${paddedIndex}  ${paddedModule}`, module: m };
});

const modes = [
  { key: 'history', name: 'Recent', items: [], emptyMessage: 'No history' },
  { key: 'jetty', name: 'Jetty', items: moduleItems, emptyMessage: 'No matches' },
  { key: 'unit', name: 'Unit tests', items: moduleItems, emptyMessage: 'No matches', promptPattern: true },
  { key: 'integration', name: 'Integration tests', items: moduleItems, emptyMessage: 'No matches', promptPattern: true }
];

function modeByKey(key) {
  return modes.find((m) => m.key === key);
}

// === History ===

let history = [];

function loadHistory() {
  try {
    if (fs.existsSync(HISTORY_FILE)) {
      history = JSON.parse(fs.readFileSync(HISTORY_FILE, 'utf8'));
    }
  } catch (e) {
    history = [];
  }
}

function saveHistory(module, modeKey, exitCode = null) {
  history = history.filter((h) => !(h.module === module && h.mode === modeKey));
  history.unshift({ module, mode: modeKey, exitCode });
  history = history.slice(0, MAX_HISTORY);
  fs.writeFileSync(HISTORY_FILE, JSON.stringify(history, null, 2));
}

function generateHistoryItems() {
  const maxMod = Math.max(...history.map((h) => h.module.length), 0);
  const maxMode = Math.max(...history.map((h) => (modeByKey(h.mode)?.name || h.mode).length), 0);

  return history.map((h) => {
    const modeName = modeByKey(h.mode)?.name || h.mode;
    let label = `${h.module.padEnd(maxMod)}  ${modeName.padEnd(maxMode)}`;
    if (h.exitCode === 0) label += `  ${GREEN}✓ Success${RESET}`;
    else if (h.exitCode != null) label += `  ${RED}✗ Failed${RESET}`;
    return { label, module: h.module, modeKey: h.mode };
  });
}

// === Maven Execution ===

function runMvn(module, modeKey, args, message, onOutput) {
  const command = `mvn ${args.join(' ')}`;
  args = [...args, '-Dstyle.color=always'];
  console.log(`\n${message}\n`);

  const mvn = spawn('mvn', args, { stdio: ['inherit', 'pipe', 'pipe'] });

  const handleOutput = (data) => {
    const lines = data.toString().split('\n');
    const filtered = lines.filter((line) => !LOG_FILTERS.some((f) => line.includes(f)));
    process.stdout.write(filtered.join('\n'));
    if (onOutput) onOutput(filtered.join('\n'));
  };

  mvn.stdout.on('data', handleOutput);
  mvn.stderr.on('data', handleOutput);

  const sigintHandler = () => {};
  process.on('SIGINT', sigintHandler);

  mvn.on('close', (code, signal) => {
    process.off('SIGINT', sigintHandler);
    const exitCode = signal === 'SIGINT' || code === 130 ? null : code;
    saveHistory(module, modeKey, exitCode);
    console.log(`\n${DIM}Command:${RESET}\n${command}\n`);
    listView.start();
  });
}

function runJetty(module, modeKey) {
  const args = [
    'package',
    'jetty:run',
    '-Dvaadin.pnpm.enable',
    '-Dvaadin.frontend.hotdeploy=true',
    '-am',
    '-B',
    '-q',
    '-DskipTests',
    '-pl',
    itModule(module)
  ];
  runMvn(module, modeKey, args, `Starting Jetty for ${module}...`, (text) => {
    if (text.includes('Frontend compiled successfully')) {
      console.log(`\n${DIM}Server is running at:${RESET} http://localhost:8080/\n`);
    }
  });
}

function runUnitTests(module, modeKey, pattern = '') {
  const args = ['test', '-pl', flowModule(module)];
  if (pattern) args.push(`-Dtest=${pattern}`);
  runMvn(module, modeKey, args, `Running unit tests for ${module}...`);
}

function runIntegrationTests(module, modeKey, pattern = '') {
  const args = [
    'verify',
    '-Dvaadin.pnpm.enable',
    '-Dvaadin.frontend.hotdeploy=true',
    '-am',
    '-B',
    '-DskipUnitTests',
    '-pl',
    itModule(module)
  ];
  if (pattern) args.push(`-Dit.test=${pattern}`);
  runMvn(module, modeKey, args, `Running integration tests for ${module}...`);
}

function executeMode(module, modeKey, pattern = '') {
  switch (modeKey) {
    case 'jetty':
      runJetty(module, modeKey);
      break;
    case 'unit':
      runUnitTests(module, modeKey, pattern);
      break;
    case 'integration':
      runIntegrationTests(module, modeKey, pattern);
      break;
    default:
      console.error(`Unknown mode: ${modeKey}`);
  }
}

// === Views ===

function setRawMode(enabled) {
  if (process.stdin.isTTY) {
    process.stdin.setRawMode(enabled);
  }
}

// === List View ===

const listView = {
  modeIndex: 0,
  selectedIndex: 0,
  filter: '',
  filtered: [],
  lastRenderedLines: 0,
  handler: null,

  start() {
    loadHistory();
    modeByKey('history').items = generateHistoryItems();

    this.modeIndex = 0;
    this.selectedIndex = 0;
    this.lastRenderedLines = 0;
    this.filter = '';
    setRawMode(true);
    process.stdin.resume();
    process.stdout.write(HIDE_CURSOR);

    this.handler = this.handleKeypress.bind(this);
    process.stdin.on('keypress', this.handler);

    this.render();
  },

  stop() {
    this.clear();
    process.stdin.off('keypress', this.handler);
    process.stdout.write(SHOW_CURSOR);
    setRawMode(false);
  },

  maxVisible() {
    return Math.min(15, process.stdout.rows - 4);
  },

  handleKeypress(str, key) {
    if (key.name === 'escape' || (key.ctrl && key.name === 'c')) {
      this.stop();
      process.exit(1);
    }

    if (key.name === 'return') {
      if (this.filtered.length === 0) {
        this.stop();
        process.exit(1);
      }

      const item = this.filtered[this.selectedIndex];
      const modeKey = item.modeKey || modes[this.modeIndex].key;
      const mode = modeByKey(modeKey);

      this.stop();

      if (mode.promptPattern) {
        patternView.start(item.module, modeKey);
      } else {
        executeMode(item.module, modeKey);
      }
      return;
    }

    if (key.name === 'left') {
      this.modeIndex = Math.max(0, this.modeIndex - 1);
    } else if (key.name === 'right') {
      this.modeIndex = Math.min(modes.length - 1, this.modeIndex + 1);
    } else if (key.name === 'up') {
      this.selectedIndex = Math.max(0, this.selectedIndex - 1);
    } else if (key.name === 'down') {
      this.selectedIndex = Math.min(this.filtered.length - 1, this.selectedIndex + 1);
    } else if (key.name === 'pageup') {
      this.selectedIndex = Math.max(0, this.selectedIndex - this.maxVisible());
    } else if (key.name === 'pagedown') {
      this.selectedIndex = Math.min(this.filtered.length - 1, this.selectedIndex + this.maxVisible());
    } else if (key.name === 'backspace') {
      this.filter = this.filter.slice(0, -1);
      this.selectedIndex = 0;
    } else if (str && !key.ctrl && !key.meta && str.length === 1) {
      this.filter += str;
      this.selectedIndex = 0;
    }

    this.render();
  },
  
  clear() {
    if (this.lastRenderedLines > 0) {
      process.stdout.write(CURSOR_UP(this.lastRenderedLines) + CLEAR_LINE);
      for (let i = 1; i < this.lastRenderedLines; i++) {
        process.stdout.write(CURSOR_DOWN(1) + CLEAR_LINE);
      }
      process.stdout.write(CURSOR_UP(this.lastRenderedLines - 1));
    }
  },

  render() {
    const maxVisible = this.maxVisible();
    
    this.clear();

    const mode = modes[this.modeIndex];
    this.filtered = this.filter
      ? mode.items.filter((item) => item.label.toLowerCase().includes(this.filter.toLowerCase()))
      : mode.items;

    if (this.selectedIndex >= this.filtered.length) {
      this.selectedIndex = Math.max(0, this.filtered.length - 1);
    }

    const start = Math.max(
      0,
      Math.min(this.selectedIndex - Math.floor(maxVisible / 2), this.filtered.length - maxVisible)
    );
    const visible = this.filtered.slice(start, start + maxVisible);

    const modeDisplay = modes
      .map((m, i) => (i === this.modeIndex ? `${REVERSE} ${m.name} ${RESET}` : ` ${m.name} `))
      .join('  ');

    const lines = [
      `Mode: ${modeDisplay}`,
      `Filter: ${this.filter}█`,
      `${DIM}${this.filtered.length} matches (←→ mode, ↑↓ select, Enter run, Esc cancel)${RESET}`
    ];

    for (let i = 0; i < maxVisible; i++) {
      const actualIndex = start + i;
      const item = visible[i];
      if (item) {
        lines.push(
          actualIndex === this.selectedIndex ? `${CYAN}▶${RESET} ${BOLD}${item.label}${RESET}` : `  ${item.label}`
        );
      } else if (i === 0 && this.filtered.length === 0) {
        lines.push(`${DIM}  ${mode.emptyMessage}${RESET}`);
      } else {
        lines.push('');
      }
    }

    process.stdout.write(lines.join('\n') + '\n');
    this.lastRenderedLines = lines.length;
  }
};

// === Test Pattern View ===

const patternView = {
  module: null,
  modeKey: null,
  pattern: '',
  handler: null,

  start(module, modeKey) {
    this.module = module;
    this.modeKey = modeKey;
    this.pattern = '';

    setRawMode(true);
    this.handler = this.handleKeypress.bind(this);
    process.stdin.on('keypress', this.handler);

    const modeName = modeByKey(this.modeKey)?.name || this.modeKey;
    process.stdout.write(`${BOLD}${modeName}${RESET} ${DIM}>${RESET} ${this.module}\n`);
    process.stdout.write(`${DIM}Test pattern for ${this.module} (Enter for all):${RESET} `);
  },

  stop() {
    process.stdin.off('keypress', this.handler);
    setRawMode(false);
  },

  handleKeypress(str, key) {
    if (key.name === 'escape' || (key.ctrl && key.name === 'c')) {
      this.stop();
      process.stdout.write('\n');
      listView.start();
      return;
    }

    if (key.name === 'return') {
      this.stop();
      process.stdout.write('\n');
      executeMode(this.module, this.modeKey, this.pattern.trim());
      return;
    }

    if (key.name === 'backspace') {
      if (this.pattern.length > 0) {
        this.pattern = this.pattern.slice(0, -1);
        process.stdout.write('\b \b');
      }
    } else if (str && !key.ctrl && !key.meta && str.length === 1) {
      this.pattern += str;
      process.stdout.write(str);
    }
  },
};

// === Entry Point ===

readline.emitKeypressEvents(process.stdin);
listView.start();
