package com.example.engine

import com.example.model.AngleMode
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.ArrayDeque
import java.util.Locale
import kotlin.math.*

sealed class EvaluationResult {
  data class Success(val value: Double, val formatted: String) : EvaluationResult()
  data class Error(val message: String) : EvaluationResult()
}

object CalculatorEngine {

  private val DISPLAY_FORMAT = DecimalFormat("#,##0.##########", DecimalFormatSymbols(Locale.US).apply {
    decimalSeparator = '.'
    groupingSeparator = ','
  })

  private val SCIENTIFIC_FORMAT = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))

  /**
   * Evaluates a mathematical expression string.
   */
  fun evaluate(rawExpr: String, angleMode: AngleMode = AngleMode.DEG): EvaluationResult {
    val sanitized = sanitizeExpression(rawExpr)
    if (sanitized.isBlank()) {
      return EvaluationResult.Success(0.0, "0")
    }

    return try {
      val tokens = tokenize(sanitized)
      val rpn = toRpn(tokens)
      val result = evaluateRpn(rpn, angleMode)
      if (result.isInfinite() || result.isNaN()) {
        EvaluationResult.Error("Tidak dapat membagi dengan nol")
      } else {
        EvaluationResult.Success(result, formatResult(result))
      }
    } catch (e: ArithmeticException) {
      EvaluationResult.Error(e.message ?: "Kesalahan matematika")
    } catch (e: Exception) {
      EvaluationResult.Error("Format ekspresi tidak valid")
    }
  }

  /**
   * Live preview evaluation: tries to evaluate expression even if incomplete,
   * by stripping trailing uncompleted operators or brackets.
   */
  fun livePreview(rawExpr: String, angleMode: AngleMode = AngleMode.DEG): String? {
    if (rawExpr.isBlank()) return null
    var cleaned = rawExpr.trim()
    // Remove trailing operators like +, -, ×, ÷, (, ^
    while (cleaned.isNotEmpty() && isTrailingIncomplete(cleaned.last())) {
      cleaned = cleaned.dropLast(1).trim()
    }
    if (cleaned.isBlank()) return null

    // Balance open parentheses if any
    val openCount = cleaned.count { it == '(' }
    val closeCount = cleaned.count { it == ')' }
    if (openCount > closeCount) {
      cleaned += ")".repeat(openCount - closeCount)
    }

    return when (val res = evaluate(cleaned, angleMode)) {
      is EvaluationResult.Success -> {
        // Only return if it's not a trivial single number matching input
        val cleanInput = rawExpr.replace(",", "").trim()
        if (cleanInput != res.formatted && cleanInput != formatPlain(res.value)) {
          res.formatted
        } else {
          null
        }
      }
      is EvaluationResult.Error -> null
    }
  }

  private fun isTrailingIncomplete(c: Char): Boolean {
    return c in listOf('+', '−', '-', '×', '*', '÷', '/', '^', '(', '√', '%')
  }

  fun sanitizeExpression(expr: String): String {
    return expr
      .replace("×", "*")
      .replace("÷", "/")
      .replace("−", "-")
      .replace(" ", "")
  }

  fun formatResult(value: Double): String {
    if (value == 0.0) return "0"
    val absVal = abs(value)
    return if (absVal >= 1e12 || (absVal < 1e-6 && absVal > 0)) {
      SCIENTIFIC_FORMAT.format(value).replace("E", "e")
    } else {
      // Fix floating point inaccuracies like 0.1 + 0.2 = 0.30000000000000004
      val bd = try {
        BigDecimal.valueOf(value).round(MathContext(12, RoundingMode.HALF_UP)).stripTrailingZeros()
      } catch (e: Exception) {
        BigDecimal.valueOf(value)
      }
      val doubleVal = bd.toDouble()
      if (doubleVal == floor(doubleVal) && !doubleVal.isInfinite() && absVal < 1e12) {
        DISPLAY_FORMAT.format(doubleVal.toLong())
      } else {
        DISPLAY_FORMAT.format(doubleVal)
      }
    }
  }

  private fun formatPlain(value: Double): String {
    return if (value == floor(value) && !value.isInfinite()) {
      value.toLong().toString()
    } else {
      value.toString()
    }
  }

  private sealed class Token {
    data class Number(val value: Double) : Token()
    data class Operator(val op: Char, val precedence: Int, val rightAssociative: Boolean) : Token()
    data class Function(val name: String) : Token()
    data object OpenParen : Token()
    data object CloseParen : Token()
  }

  private fun tokenize(expr: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    var lastToken: Token? = null

    while (i < expr.length) {
      val c = expr[i]

      when {
        c.isWhitespace() -> i++

        c.isDigit() || c == '.' -> {
          // If previous was number or close paren, insert implicit multiplication
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          val start = i
          while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
            i++
          }
          val numStr = expr.substring(start, i)
          val numVal = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
          val token = Token.Number(numVal)
          tokens.add(token)
          lastToken = token
        }

        c == 'π' || c == 'e' -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          val value = if (c == 'π') Math.PI else Math.E
          val token = Token.Number(value)
          tokens.add(token)
          lastToken = token
          i++
        }

        c == '(' -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          tokens.add(Token.OpenParen)
          lastToken = Token.OpenParen
          i++
        }

        c == ')' -> {
          tokens.add(Token.CloseParen)
          lastToken = Token.CloseParen
          i++
        }

        c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' -> {
          // Unary minus check
          if (c == '-' && (lastToken == null || lastToken is Token.OpenParen || lastToken is Token.Operator)) {
            // Negative number following
            i++
            val start = i
            if (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
              while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                i++
              }
              val numStr = expr.substring(start, i)
              val numVal = -(numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: -$numStr"))
              val token = Token.Number(numVal)
              tokens.add(token)
              lastToken = token
            } else if (i < expr.length && expr[i] == '(') {
              tokens.add(Token.Number(-1.0))
              tokens.add(Token.Operator('*', 2, false))
              tokens.add(Token.OpenParen)
              lastToken = Token.OpenParen
              i++
            } else {
              throw IllegalArgumentException("Unexpected operator after minus")
            }
          } else {
            val prec = when (c) {
              '+', '-' -> 1
              '*', '/', '%' -> 2
              '^' -> 3
              else -> 1
            }
            val rightAssoc = c == '^'
            val token = Token.Operator(c, prec, rightAssoc)
            tokens.add(token)
            lastToken = token
            i++
          }
        }

        c == '√' -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          val token = Token.Function("sqrt")
          tokens.add(token)
          lastToken = token
          i++
        }

        expr.startsWith("sin", i) -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          tokens.add(Token.Function("sin"))
          lastToken = Token.Function("sin")
          i += 3
        }

        expr.startsWith("cos", i) -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          tokens.add(Token.Function("cos"))
          lastToken = Token.Function("cos")
          i += 3
        }

        expr.startsWith("tan", i) -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          tokens.add(Token.Function("tan"))
          lastToken = Token.Function("tan")
          i += 3
        }

        expr.startsWith("log", i) -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          tokens.add(Token.Function("log"))
          lastToken = Token.Function("log")
          i += 3
        }

        expr.startsWith("ln", i) -> {
          if (lastToken is Token.Number || lastToken is Token.CloseParen) {
            tokens.add(Token.Operator('*', 2, false))
          }
          tokens.add(Token.Function("ln"))
          lastToken = Token.Function("ln")
          i += 2
        }

        else -> throw IllegalArgumentException("Karakter tidak dikenal: $c")
      }
    }
    return tokens
  }

  private fun toRpn(tokens: List<Token>): List<Token> {
    val output = mutableListOf<Token>()
    val stack = ArrayDeque<Token>()

    for (token in tokens) {
      when (token) {
        is Token.Number -> output.add(token)
        is Token.Function -> stack.push(token)
        is Token.Operator -> {
          while (stack.isNotEmpty()) {
            val top = stack.peek()
            val shouldPop = when (top) {
              is Token.Function -> true
              is Token.Operator -> {
                if (token.rightAssociative) {
                  token.precedence < top.precedence
                } else {
                  token.precedence <= top.precedence
                }
              }
              else -> false
            }
            if (shouldPop) {
              output.add(stack.pop())
            } else {
              break
            }
          }
          stack.push(token)
        }
        is Token.OpenParen -> stack.push(token)
        is Token.CloseParen -> {
          var foundOpen = false
          while (stack.isNotEmpty()) {
            val top = stack.pop()
            if (top is Token.OpenParen) {
              foundOpen = true
              break
            } else {
              output.add(top)
            }
          }
          if (!foundOpen) throw IllegalArgumentException("Tanda kurung tidak seimbang")
          if (stack.isNotEmpty() && stack.peek() is Token.Function) {
            output.add(stack.pop())
          }
        }
      }
    }

    while (stack.isNotEmpty()) {
      val top = stack.pop()
      if (top is Token.OpenParen || top is Token.CloseParen) {
        throw IllegalArgumentException("Tanda kurung tidak seimbang")
      }
      output.add(top)
    }

    return output
  }

  private fun evaluateRpn(rpn: List<Token>, angleMode: AngleMode): Double {
    val stack = ArrayDeque<Double>()

    for (token in rpn) {
      when (token) {
        is Token.Number -> stack.push(token.value)
        is Token.Operator -> {
          if (stack.size < 2) throw IllegalArgumentException("Argumen operator kurang")
          val b = stack.pop()
          val a = stack.pop()
          val result = when (token.op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
              if (abs(b) < 1e-15) throw ArithmeticException("Tidak dapat membagi dengan nol")
              a / b
            }
            '%' -> {
              // Percentage logic: a % b is either modulo or percentage
              a * (b / 100.0)
            }
            '^' -> a.pow(b)
            else -> throw IllegalArgumentException("Operator tidak didukung: ${token.op}")
          }
          stack.push(result)
        }
        is Token.Function -> {
          if (stack.isEmpty()) throw IllegalArgumentException("Argumen fungsi kurang")
          val arg = stack.pop()
          val res = when (token.name) {
            "sqrt" -> {
              if (arg < 0) throw ArithmeticException("Akar bilangan negatif tidak valid")
              sqrt(arg)
            }
            "sin" -> {
              val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
              sin(rad)
            }
            "cos" -> {
              val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
              cos(rad)
            }
            "tan" -> {
              val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
              val c = cos(rad)
              if (abs(c) < 1e-15) throw ArithmeticException("Nilai tan tidak terdefinisi")
              tan(rad)
            }
            "log" -> {
              if (arg <= 0) throw ArithmeticException("Logaritma harus bernilai positif")
              log10(arg)
            }
            "ln" -> {
              if (arg <= 0) throw ArithmeticException("Logaritma harus bernilai positif")
              ln(arg)
            }
            else -> throw IllegalArgumentException("Fungsi tidak dikenal: ${token.name}")
          }
          stack.push(res)
        }
        else -> Unit
      }
    }

    if (stack.size != 1) throw IllegalArgumentException("Ekspresi tidak valid")
    return stack.pop()
  }
}
