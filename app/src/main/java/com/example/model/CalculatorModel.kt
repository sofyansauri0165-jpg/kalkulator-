package com.example.model

enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

enum class AngleMode {
  DEG,
  RAD
}

enum class ButtonCategory {
  NUMBER,
  OPERATOR,
  FUNCTION,
  EQUALS,
  SCIENTIFIC
}

data class HistoryItem(
  val id: Long = System.currentTimeMillis(),
  val expression: String,
  val result: String,
  val formattedTime: String
)

sealed class CalculatorKey(
  val label: String,
  val category: ButtonCategory,
  val testTag: String
) {
  // Numbers
  data object Zero : CalculatorKey("0", ButtonCategory.NUMBER, "btn_0")
  data object One : CalculatorKey("1", ButtonCategory.NUMBER, "btn_1")
  data object Two : CalculatorKey("2", ButtonCategory.NUMBER, "btn_2")
  data object Three : CalculatorKey("3", ButtonCategory.NUMBER, "btn_3")
  data object Four : CalculatorKey("4", ButtonCategory.NUMBER, "btn_4")
  data object Five : CalculatorKey("5", ButtonCategory.NUMBER, "btn_5")
  data object Six : CalculatorKey("6", ButtonCategory.NUMBER, "btn_6")
  data object Seven : CalculatorKey("7", ButtonCategory.NUMBER, "btn_7")
  data object Eight : CalculatorKey("8", ButtonCategory.NUMBER, "btn_8")
  data object Nine : CalculatorKey("9", ButtonCategory.NUMBER, "btn_9")
  data object Decimal : CalculatorKey(".", ButtonCategory.NUMBER, "btn_decimal")

  // Functions
  data object Clear : CalculatorKey("C", ButtonCategory.FUNCTION, "btn_clear")
  data object Backspace : CalculatorKey("⌫", ButtonCategory.FUNCTION, "btn_backspace")
  data object PlusMinus : CalculatorKey("±", ButtonCategory.FUNCTION, "btn_plus_minus")
  data object Percent : CalculatorKey("%", ButtonCategory.FUNCTION, "btn_percent")

  // Operators
  data object Divide : CalculatorKey("÷", ButtonCategory.OPERATOR, "btn_divide")
  data object Multiply : CalculatorKey("×", ButtonCategory.OPERATOR, "btn_multiply")
  data object Subtract : CalculatorKey("−", ButtonCategory.OPERATOR, "btn_subtract")
  data object Add : CalculatorKey("+", ButtonCategory.OPERATOR, "btn_add")
  data object Equals : CalculatorKey("=", ButtonCategory.EQUALS, "btn_equals")

  // Scientific
  data object OpenParen : CalculatorKey("(", ButtonCategory.SCIENTIFIC, "btn_open_paren")
  data object CloseParen : CalculatorKey(")", ButtonCategory.SCIENTIFIC, "btn_close_paren")
  data object Sqrt : CalculatorKey("√", ButtonCategory.SCIENTIFIC, "btn_sqrt")
  data object Power : CalculatorKey("^", ButtonCategory.SCIENTIFIC, "btn_power")
  data object Sin : CalculatorKey("sin", ButtonCategory.SCIENTIFIC, "btn_sin")
  data object Cos : CalculatorKey("cos", ButtonCategory.SCIENTIFIC, "btn_cos")
  data object Tan : CalculatorKey("tan", ButtonCategory.SCIENTIFIC, "btn_tan")
  data object Log : CalculatorKey("log", ButtonCategory.SCIENTIFIC, "btn_log")
  data object Ln : CalculatorKey("ln", ButtonCategory.SCIENTIFIC, "btn_ln")
  data object Pi : CalculatorKey("π", ButtonCategory.SCIENTIFIC, "btn_pi")
  data object Euler : CalculatorKey("e", ButtonCategory.SCIENTIFIC, "btn_euler")
}
