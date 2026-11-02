package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.engine.CalculatorEngine
import com.example.engine.EvaluationResult
import com.example.model.AngleMode
import com.example.model.CalculatorKey
import com.example.model.HistoryItem
import com.example.model.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
  val expression: String = "",
  val livePreview: String? = null,
  val errorMessage: String? = null,
  val isCalculated: Boolean = false,
  val history: List<HistoryItem> = emptyList(),
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val angleMode: AngleMode = AngleMode.DEG,
  val isScientificOpen: Boolean = false,
  val isHistoryOpen: Boolean = false,
  val hapticsEnabled: Boolean = true
)

class CalculatorViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  private val timeFormat = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())

  fun onKeyPressed(key: CalculatorKey) {
    when (key) {
      is CalculatorKey.Clear -> handleClear()
      is CalculatorKey.Backspace -> handleBackspace()
      is CalculatorKey.Equals -> handleEquals()
      is CalculatorKey.Decimal -> handleDecimal()
      is CalculatorKey.PlusMinus -> handlePlusMinus()
      is CalculatorKey.Percent -> handlePercent()
      is CalculatorKey.Add,
      is CalculatorKey.Subtract,
      is CalculatorKey.Multiply,
      is CalculatorKey.Divide,
      is CalculatorKey.Power -> handleOperator(key.label)
      is CalculatorKey.Zero,
      is CalculatorKey.One,
      is CalculatorKey.Two,
      is CalculatorKey.Three,
      is CalculatorKey.Four,
      is CalculatorKey.Five,
      is CalculatorKey.Six,
      is CalculatorKey.Seven,
      is CalculatorKey.Eight,
      is CalculatorKey.Nine -> handleDigit(key.label)
      is CalculatorKey.OpenParen,
      is CalculatorKey.CloseParen -> handleParen(key.label)
      is CalculatorKey.Pi,
      is CalculatorKey.Euler -> handleConstant(key.label)
      is CalculatorKey.Sqrt,
      is CalculatorKey.Sin,
      is CalculatorKey.Cos,
      is CalculatorKey.Tan,
      is CalculatorKey.Log,
      is CalculatorKey.Ln -> handleFunction(key.label)
    }
  }

  private fun handleDigit(digit: String) {
    _uiState.update { state ->
      val newExpr = when {
        state.errorMessage != null -> digit
        state.isCalculated -> digit
        state.expression == "0" -> digit
        else -> state.expression + digit
      }
      val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
      state.copy(
        expression = newExpr,
        livePreview = preview,
        errorMessage = null,
        isCalculated = false
      )
    }
  }

  private fun handleDecimal() {
    _uiState.update { state ->
      if (state.errorMessage != null || state.isCalculated) {
        val newExpr = "0."
        state.copy(
          expression = newExpr,
          livePreview = null,
          errorMessage = null,
          isCalculated = false
        )
      } else {
        val expr = state.expression
        val lastNumberSegment = getLastNumberSegment(expr)
        if (lastNumberSegment.contains(".")) {
          // Prevent multiple decimal points in same number
          state
        } else {
          val newExpr = when {
            expr.isEmpty() -> "0."
            isLastCharOperator(expr) || expr.endsWith("(") -> expr + "0."
            else -> expr + "."
          }
          val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
          state.copy(
            expression = newExpr,
            livePreview = preview,
            errorMessage = null,
            isCalculated = false
          )
        }
      }
    }
  }

  private fun handleOperator(op: String) {
    _uiState.update { state ->
      val expr = state.expression
      val isCalculated = state.isCalculated
      val newExpr = when {
        state.errorMessage != null -> {
          if (op == "−") "−" else ""
        }
        expr.isEmpty() -> {
          if (op == "−") "−" else ""
        }
        isCalculated -> {
          // Chain operation directly from previous result
          "$expr $op "
        }
        isLastCharOperator(expr) -> {
          // Replace trailing operator
          val trimmed = expr.trimEnd()
          val withoutLastOp = if (trimmed.endsWith("−") || trimmed.endsWith("+") ||
            trimmed.endsWith("×") || trimmed.endsWith("÷") || trimmed.endsWith("^")) {
            trimmed.dropLast(1).trimEnd()
          } else trimmed
          "$withoutLastOp $op "
        }
        else -> {
          "$expr $op "
        }
      }

      val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
      state.copy(
        expression = newExpr,
        livePreview = preview,
        errorMessage = null,
        isCalculated = false
      )
    }
  }

  private fun handlePlusMinus() {
    _uiState.update { state ->
      val expr = state.expression
      if (expr.isEmpty() || state.errorMessage != null) return@update state

      // If wrapped in (-X), unwrap it
      val negParenRegex = Regex("""\(-(\d+(\.\d+)?)\)$""")
      val matchParen = negParenRegex.find(expr)
      if (matchParen != null) {
        val num = matchParen.groupValues[1]
        val newExpr = expr.substring(0, matchParen.range.first) + num
        val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
        return@update state.copy(expression = newExpr, livePreview = preview)
      }

      // Check simple trailing number
      val numRegex = Regex("""(\d+(\.\d+)?)$""")
      val matchNum = numRegex.find(expr)
      if (matchNum != null) {
        val num = matchNum.value
        val prefix = expr.substring(0, matchNum.range.first)
        val newExpr = if (prefix.endsWith("−") || prefix.endsWith("-")) {
          prefix.dropLast(1) + num
        } else if (prefix.isEmpty()) {
          "(-$num)"
        } else {
          "$prefix(-$num)"
        }
        val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
        return@update state.copy(expression = newExpr, livePreview = preview)
      }

      state
    }
  }

  private fun handlePercent() {
    _uiState.update { state ->
      val expr = state.expression
      if (expr.isEmpty() || isLastCharOperator(expr)) return@update state

      val newExpr = "$expr % "
      val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
      state.copy(expression = newExpr, livePreview = preview, isCalculated = false)
    }
  }

  private fun handleParen(paren: String) {
    _uiState.update { state ->
      val expr = state.expression
      val newExpr = if (state.isCalculated && paren == "(") {
        "("
      } else {
        expr + paren
      }
      val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
      state.copy(expression = newExpr, livePreview = preview, isCalculated = false)
    }
  }

  private fun handleConstant(constant: String) {
    _uiState.update { state ->
      val expr = if (state.isCalculated) "" else state.expression
      val newExpr = expr + constant
      val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
      state.copy(expression = newExpr, livePreview = preview, isCalculated = false)
    }
  }

  private fun handleFunction(fnName: String) {
    _uiState.update { state ->
      val expr = if (state.isCalculated) "" else state.expression
      val suffix = if (fnName == "√") "√(" else "$fnName("
      val newExpr = expr + suffix
      val preview = CalculatorEngine.livePreview(newExpr, state.angleMode)
      state.copy(expression = newExpr, livePreview = preview, isCalculated = false)
    }
  }

  private fun handleEquals() {
    val state = _uiState.value
    if (state.expression.isBlank()) return

    when (val result = CalculatorEngine.evaluate(state.expression, state.angleMode)) {
      is EvaluationResult.Success -> {
        val historyItem = HistoryItem(
          expression = state.expression,
          result = result.formatted,
          formattedTime = timeFormat.format(Date())
        )
        _uiState.update {
          it.copy(
            expression = result.formatted,
            livePreview = null,
            errorMessage = null,
            isCalculated = true,
            history = listOf(historyItem) + it.history
          )
        }
      }
      is EvaluationResult.Error -> {
        _uiState.update {
          it.copy(
            errorMessage = result.message,
            livePreview = null
          )
        }
      }
    }
  }

  private fun handleClear() {
    _uiState.update {
      it.copy(
        expression = "",
        livePreview = null,
        errorMessage = null,
        isCalculated = false
      )
    }
  }

  private fun handleBackspace() {
    _uiState.update { state ->
      if (state.errorMessage != null) {
        return@update state.copy(errorMessage = null)
      }
      if (state.isCalculated) {
        return@update state.copy(expression = "", isCalculated = false, livePreview = null)
      }
      var expr = state.expression.trimEnd()
      if (expr.isEmpty()) return@update state

      // Check multi-character function tokens at end
      val functionNames = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")
      var removed = false
      for (fn in functionNames) {
        if (expr.endsWith(fn)) {
          expr = expr.dropLast(fn.length)
          removed = true
          break
        }
      }

      if (!removed) {
        expr = expr.dropLast(1).trimEnd()
      }

      val preview = CalculatorEngine.livePreview(expr, state.angleMode)
      state.copy(
        expression = expr,
        livePreview = preview,
        errorMessage = null,
        isCalculated = false
      )
    }
  }

  fun recallHistoryItem(item: HistoryItem, useResultOnly: Boolean = false) {
    _uiState.update { state ->
      val target = if (useResultOnly) item.result else item.expression
      val preview = CalculatorEngine.livePreview(target, state.angleMode)
      state.copy(
        expression = target,
        livePreview = preview,
        errorMessage = null,
        isCalculated = false,
        isHistoryOpen = false
      )
    }
  }

  fun clearHistory() {
    _uiState.update { it.copy(history = emptyList()) }
  }

  fun toggleHistory() {
    _uiState.update { it.copy(isHistoryOpen = !it.isHistoryOpen) }
  }

  fun closeHistory() {
    _uiState.update { it.copy(isHistoryOpen = false) }
  }

  fun cycleTheme() {
    _uiState.update { state ->
      val nextTheme = when (state.themeMode) {
        ThemeMode.SYSTEM -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.LIGHT
        ThemeMode.LIGHT -> ThemeMode.SYSTEM
      }
      state.copy(themeMode = nextTheme)
    }
  }

  fun toggleAngleMode() {
    _uiState.update { state ->
      val nextMode = if (state.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
      val preview = CalculatorEngine.livePreview(state.expression, nextMode)
      state.copy(angleMode = nextMode, livePreview = preview)
    }
  }

  fun toggleScientific() {
    _uiState.update { it.copy(isScientificOpen = !it.isScientificOpen) }
  }

  fun toggleHaptics() {
    _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
  }

  fun handleKeyboardInput(char: Char): Boolean {
    when (char) {
      in '0'..'9' -> onKeyPressed(digitToKey(char))
      '.' -> onKeyPressed(CalculatorKey.Decimal)
      '+' -> onKeyPressed(CalculatorKey.Add)
      '-', '−' -> onKeyPressed(CalculatorKey.Subtract)
      '*', '×' -> onKeyPressed(CalculatorKey.Multiply)
      '/', '÷' -> onKeyPressed(CalculatorKey.Divide)
      '%' -> onKeyPressed(CalculatorKey.Percent)
      '=', '\n', '\r' -> onKeyPressed(CalculatorKey.Equals)
      'c', 'C' -> onKeyPressed(CalculatorKey.Clear)
      '(', '[' -> onKeyPressed(CalculatorKey.OpenParen)
      ')', ']' -> onKeyPressed(CalculatorKey.CloseParen)
      '^' -> onKeyPressed(CalculatorKey.Power)
      else -> return false
    }
    return true
  }

  private fun digitToKey(char: Char): CalculatorKey = when (char) {
    '0' -> CalculatorKey.Zero
    '1' -> CalculatorKey.One
    '2' -> CalculatorKey.Two
    '3' -> CalculatorKey.Three
    '4' -> CalculatorKey.Four
    '5' -> CalculatorKey.Five
    '6' -> CalculatorKey.Six
    '7' -> CalculatorKey.Seven
    '8' -> CalculatorKey.Eight
    '9' -> CalculatorKey.Nine
    else -> CalculatorKey.Zero
  }

  private fun isLastCharOperator(expr: String): Boolean {
    val trimmed = expr.trimEnd()
    if (trimmed.isEmpty()) return false
    val last = trimmed.last()
    return last in listOf('+', '−', '-', '×', '*', '÷', '/', '^')
  }

  private fun getLastNumberSegment(expr: String): String {
    val operators = listOf('+', '−', '-', '×', '*', '÷', '/', '(', ')', '^', '%')
    var lastOpIndex = -1
    for (op in operators) {
      val idx = expr.lastIndexOf(op)
      if (idx > lastOpIndex) {
        lastOpIndex = idx
      }
    }
    return if (lastOpIndex >= 0) {
      expr.substring(lastOpIndex + 1)
    } else {
      expr
    }
  }
}
