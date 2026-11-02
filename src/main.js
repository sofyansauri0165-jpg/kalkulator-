/**
 * Kalkulator Modern - Web App
 */

// DOM Elements
const expressionDisplay = document.getElementById('expression-display');
const previewDisplay = document.getElementById('preview-display');
const errorBanner = document.getElementById('error-banner');
const errorText = document.getElementById('error-text');
const copyToast = document.getElementById('copy-toast');
const displayContainer = document.getElementById('display-container');

const btnToggleScientific = document.getElementById('btn-toggle-scientific');
const scientificPanel = document.getElementById('scientific-panel');
const btnAngle = document.getElementById('btn-angle');

const btnToggleTheme = document.getElementById('btn-toggle-theme');
const btnToggleHistory = document.getElementById('btn-toggle-history');
const historyBadge = document.getElementById('history-badge');
const historyDrawer = document.getElementById('history-drawer');
const btnCloseHistory = document.getElementById('btn-close-history');
const btnClearHistory = document.getElementById('btn-clear-history');
const historyList = document.getElementById('history-list');

// State
let expression = '';
let isCalculated = false;
let angleMode = 'DEG'; // 'DEG' or 'RAD'
let history = JSON.parse(localStorage.getItem('calc_history') || '[]');

// Web Audio API for subtle key click sound
let audioCtx = null;
function playClickSound() {
  try {
    if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(600, audioCtx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(150, audioCtx.currentTime + 0.03);
    gain.gain.setValueAtTime(0.04, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.03);
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start();
    osc.stop(audioCtx.currentTime + 0.03);
  } catch (e) {
    // Ignore audio errors
  }
}

// Initial history badge update
updateHistoryBadge();

// Event Listeners for buttons
document.querySelectorAll('.key').forEach(button => {
  button.addEventListener('click', () => {
    playClickSound();
    const action = button.dataset.action;
    const val = button.dataset.val;

    switch (action) {
      case 'digit':
        handleDigit(val);
        break;
      case 'decimal':
        handleDecimal();
        break;
      case 'operator':
        handleOperator(val);
        break;
      case 'equals':
        handleEquals();
        break;
      case 'clear':
        handleClear();
        break;
      case 'backspace':
        handleBackspace();
        break;
      case 'plus-minus':
        handlePlusMinus();
        break;
      case 'percent':
        handlePercent();
        break;
      case 'function':
        handleFunction(val);
        break;
      case 'paren':
        handleParen(val);
        break;
      case 'constant':
        handleConstant(val);
        break;
      case 'toggle-angle':
        angleMode = angleMode === 'DEG' ? 'RAD' : 'DEG';
        btnAngle.textContent = angleMode;
        updateLivePreview();
        break;
    }
  });
});

function handleDigit(digit) {
  clearError();
  if (isCalculated || expression === '0') {
    expression = digit;
    isCalculated = false;
  } else {
    expression += digit;
  }
  updateDisplay();
}

function handleDecimal() {
  clearError();
  if (isCalculated || !expression) {
    expression = '0.';
    isCalculated = false;
  } else {
    // Check if the current number token already has a dot
    const tokens = expression.split(/[\s+\-×÷^()%]/);
    const lastToken = tokens[tokens.length - 1];
    if (!lastToken.includes('.')) {
      if (isLastCharOperator(expression) || expression.endsWith('(')) {
        expression += '0.';
      } else {
        expression += '.';
      }
    }
  }
  updateDisplay();
}

function handleOperator(op) {
  clearError();
  if (!expression) {
    if (op === '−' || op === '-') {
      expression = '−';
      updateDisplay();
    }
    return;
  }

  isCalculated = false;
  const trimmed = expression.trim();
  const lastChar = trimmed.slice(-1);

  if (['+', '−', '×', '÷', '^', '%'].includes(lastChar)) {
    // Replace trailing operator
    expression = trimmed.slice(0, -1).trim() + ' ' + op + ' ';
  } else {
    expression = expression + ' ' + op + ' ';
  }
  updateDisplay();
}

function handlePlusMinus() {
  clearError();
  if (!expression) return;

  // Toggle negative inside parentheses (-X)
  const parenRegex = /\(-(\d+(\.\d+)?)\)$/;
  if (parenRegex.test(expression)) {
    expression = expression.replace(parenRegex, '$1');
    updateDisplay();
    return;
  }

  // Toggle trailing number
  const numRegex = /(\d+(\.\d+)?)$/;
  const match = expression.match(numRegex);
  if (match) {
    const num = match[0];
    const prefix = expression.slice(0, -num.length);
    expression = prefix + `(-${num})`;
    updateDisplay();
  }
}

function handlePercent() {
  clearError();
  if (!expression || isLastCharOperator(expression)) return;
  expression += ' % ';
  updateDisplay();
}

function handleFunction(fn) {
  clearError();
  if (isCalculated) {
    expression = '';
    isCalculated = false;
  }
  expression += `${fn}(`;
  updateDisplay();
}

function handleParen(paren) {
  clearError();
  if (isCalculated && paren === '(') {
    expression = '';
    isCalculated = false;
  }
  expression += paren;
  updateDisplay();
}

function handleConstant(c) {
  clearError();
  if (isCalculated) {
    expression = '';
    isCalculated = false;
  }
  expression += c;
  updateDisplay();
}

function handleClear() {
  clearError();
  expression = '';
  isCalculated = false;
  updateDisplay();
}

function handleBackspace() {
  clearError();
  if (isCalculated) {
    expression = '';
    isCalculated = false;
    updateDisplay();
    return;
  }

  // Check multi-character function tokens
  const funcs = ['sin(', 'cos(', 'tan(', 'log(', 'ln('];
  for (const fn of funcs) {
    if (expression.endsWith(fn)) {
      expression = expression.slice(0, -fn.length);
      updateDisplay();
      return;
    }
  }

  expression = expression.trimEnd().slice(0, -1).trimEnd();
  updateDisplay();
}

function handleEquals() {
  if (!expression) return;
  try {
    const result = evaluateExpression(expression);
    if (!isFinite(result) || isNaN(result)) {
      showError('Tidak dapat membagi dengan nol');
      return;
    }

    const formattedResult = formatResult(result);
    // Add to history
    addHistoryItem(expression, formattedResult);

    expression = formattedResult;
    isCalculated = true;
    previewDisplay.textContent = '';
    updateDisplay(false);
  } catch (err) {
    showError(err.message || 'Format ekspresi tidak valid');
  }
}

function updateDisplay(triggerPreview = true) {
  expressionDisplay.textContent = expression || '0';
  adjustFontSize();
  if (triggerPreview) {
    updateLivePreview();
  }
}

function updateLivePreview() {
  if (!expression) {
    previewDisplay.textContent = '';
    return;
  }

  try {
    let clean = expression.trim();
    while (clean && isTrailingIncomplete(clean.slice(-1))) {
      clean = clean.slice(0, -1).trim();
    }
    if (!clean) {
      previewDisplay.textContent = '';
      return;
    }

    // Auto-balance open parentheses
    const openP = (clean.match(/\(/g) || []).length;
    const closeP = (clean.match(/\)/g) || []).length;
    if (openP > closeP) {
      clean += ')'.repeat(openP - closeP);
    }

    const val = evaluateExpression(clean);
    if (isFinite(val) && !isNaN(val)) {
      const formatted = formatResult(val);
      if (formatted !== clean.replace(/\s+/g, '')) {
        previewDisplay.textContent = `= ${formatted}`;
      } else {
        previewDisplay.textContent = '';
      }
    } else {
      previewDisplay.textContent = '';
    }
  } catch (e) {
    previewDisplay.textContent = '';
  }
}

function isTrailingIncomplete(c) {
  return ['+', '−', '-', '×', '*', '÷', '/', '^', '(', '%'].includes(c);
}

function isLastCharOperator(str) {
  const t = str.trimEnd();
  if (!t) return false;
  return ['+', '−', '-', '×', '*', '÷', '/', '^'].includes(t.slice(-1));
}

function adjustFontSize() {
  const len = (expression || '0').length;
  if (len <= 8) {
    expressionDisplay.style.fontSize = '2.3rem';
  } else if (len <= 14) {
    expressionDisplay.style.fontSize = '1.8rem';
  } else if (len <= 20) {
    expressionDisplay.style.fontSize = '1.4rem';
  } else {
    expressionDisplay.style.fontSize = '1.15rem';
  }
}

function clearError() {
  errorBanner.classList.add('hidden');
}

function showError(msg) {
  errorText.textContent = msg;
  errorBanner.classList.remove('hidden');
  previewDisplay.textContent = '';
}

// Math Evaluator
function evaluateExpression(expr) {
  let s = expr
    .replace(/×/g, '*')
    .replace(/÷/g, '/')
    .replace(/−/g, '-')
    .replace(/\s+/g, '');

  if (!s) return 0;

  // Replace constants
  s = s.replace(/π/g, `${Math.PI}`).replace(/e/g, `${Math.E}`);

  // Replace functions with Math counterparts
  s = s.replace(/sin\(([^)]+)\)/g, (_, arg) => {
    const val = evaluateExpression(arg);
    const rad = angleMode === 'DEG' ? (val * Math.PI) / 180 : val;
    return Math.sin(rad);
  });

  s = s.replace(/cos\(([^)]+)\)/g, (_, arg) => {
    const val = evaluateExpression(arg);
    const rad = angleMode === 'DEG' ? (val * Math.PI) / 180 : val;
    return Math.cos(rad);
  });

  s = s.replace(/tan\(([^)]+)\)/g, (_, arg) => {
    const val = evaluateExpression(arg);
    const rad = angleMode === 'DEG' ? (val * Math.PI) / 180 : val;
    return Math.tan(rad);
  });

  s = s.replace(/√\(([^)]+)\)/g, (_, arg) => {
    const val = evaluateExpression(arg);
    if (val < 0) throw new Error('Akar bilangan negatif tidak valid');
    return Math.sqrt(val);
  });

  s = s.replace(/log\(([^)]+)\)/g, (_, arg) => {
    const val = evaluateExpression(arg);
    if (val <= 0) throw new Error('Logaritma harus positif');
    return Math.log10(val);
  });

  s = s.replace(/ln\(([^)]+)\)/g, (_, arg) => {
    const val = evaluateExpression(arg);
    if (val <= 0) throw new Error('Logaritma harus positif');
    return Math.log(val);
  });

  // Power operator ^
  while (s.includes('^')) {
    s = s.replace(/(-?\d+(\.\d+)?)\^(-?\d+(\.\d+)?)/, (_, a, __, b) => Math.pow(Number(a), Number(b)));
  }

  // Percent operator % (e.g. 100 * 20% -> 100 * 0.20)
  s = s.replace(/(\d+(\.\d+)?)%/g, '($1/100)');

  // Safely evaluate standard infix expression using Function
  // Ensure expression only contains digits, parentheses, and math operators
  if (/[^0-9+\-*/().]/.test(s)) {
    throw new Error('Format ekspresi tidak valid');
  }

  // Check division by zero before evaluation
  if (/\/0(?!\.)|\/0\.0+(?![\d])/.test(s)) {
    throw new Error('Tidak dapat membagi dengan nol');
  }

  const result = Function(`'use strict'; return (${s})`)();
  if (result === Infinity || result === -Infinity) {
    throw new Error('Tidak dapat membagi dengan nol');
  }
  return result;
}

function formatResult(num) {
  if (num === 0) return '0';
  const abs = Math.abs(num);
  if (abs >= 1e12 || (abs < 1e-6 && abs > 0)) {
    return num.toExponential(6).replace('e+', 'e');
  }
  // Eliminate float precision artifacts (0.1 + 0.2 = 0.3)
  const rounded = parseFloat(num.toPrecision(12));
  return rounded.toLocaleString('en-US', { maximumFractionDigits: 10 });
}

// Copy to Clipboard
displayContainer.addEventListener('click', () => {
  const textToCopy = previewDisplay.textContent.replace(/^=\s*/, '') || expression;
  if (!textToCopy || textToCopy === '0') return;
  navigator.clipboard.writeText(textToCopy).then(() => {
    copyToast.classList.remove('hidden');
    setTimeout(() => copyToast.classList.add('hidden'), 2000);
  });
});

// Scientific Panel Toggle
btnToggleScientific.addEventListener('click', () => {
  scientificPanel.classList.toggle('hidden');
});

// Theme Toggle
btnToggleTheme.addEventListener('click', () => {
  const body = document.body;
  if (body.classList.contains('theme-dark')) {
    body.classList.replace('theme-dark', 'theme-light');
    localStorage.setItem('calc_theme', 'light');
  } else {
    body.classList.replace('theme-light', 'theme-dark');
    localStorage.setItem('calc_theme', 'dark');
  }
});

if (localStorage.getItem('calc_theme') === 'light') {
  document.body.classList.replace('theme-dark', 'theme-light');
}

// History Handling
function addHistoryItem(expr, res) {
  const item = {
    id: Date.now(),
    expr,
    res,
    time: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
  };
  history.unshift(item);
  if (history.length > 50) history.pop();
  localStorage.setItem('calc_history', JSON.stringify(history));
  updateHistoryBadge();
  renderHistory();
}

function updateHistoryBadge() {
  if (history.length > 0) {
    historyBadge.textContent = history.length;
    historyBadge.classList.remove('hidden');
  } else {
    historyBadge.classList.add('hidden');
  }
}

function renderHistory() {
  if (history.length === 0) {
    historyList.innerHTML = `
      <div class="empty-history">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
        <p>Belum ada riwayat perhitungan</p>
      </div>
    `;
    return;
  }

  historyList.innerHTML = history.map(item => `
    <div class="history-card" data-res="${item.res}">
      <div class="time">${item.time}</div>
      <div class="expr">${item.expr}</div>
      <div class="res">= ${item.res}</div>
    </div>
  `).join('');

  document.querySelectorAll('.history-card').forEach(card => {
    card.addEventListener('click', () => {
      expression = card.dataset.res;
      isCalculated = false;
      historyDrawer.classList.add('hidden');
      updateDisplay();
    });
  });
}

btnToggleHistory.addEventListener('click', () => {
  renderHistory();
  historyDrawer.classList.remove('hidden');
});

btnCloseHistory.addEventListener('click', () => {
  historyDrawer.classList.add('hidden');
});

btnClearHistory.addEventListener('click', () => {
  history = [];
  localStorage.removeItem('calc_history');
  updateHistoryBadge();
  renderHistory();
});

// Keyboard Support
window.addEventListener('keydown', (e) => {
  if (e.key >= '0' && e.key <= '9') {
    playClickSound();
    handleDigit(e.key);
  } else if (e.key === '.') {
    playClickSound();
    handleDecimal();
  } else if (e.key === '+') {
    playClickSound();
    handleOperator('+');
  } else if (e.key === '-') {
    playClickSound();
    handleOperator('−');
  } else if (e.key === '*' || e.key === 'x') {
    playClickSound();
    handleOperator('×');
  } else if (e.key === '/') {
    e.preventDefault();
    playClickSound();
    handleOperator('÷');
  } else if (e.key === '%') {
    playClickSound();
    handlePercent();
  } else if (e.key === 'Enter' || e.key === '=') {
    e.preventDefault();
    playClickSound();
    handleEquals();
  } else if (e.key === 'Backspace') {
    playClickSound();
    handleBackspace();
  } else if (e.key === 'Escape' || e.key.toLowerCase() === 'c') {
    playClickSound();
    handleClear();
  } else if (e.key === '(' || e.key === ')') {
    playClickSound();
    handleParen(e.key);
  } else if (e.key === '^') {
    playClickSound();
    handleOperator('^');
  }
});
