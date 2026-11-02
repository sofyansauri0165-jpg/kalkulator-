package com.example

import com.example.engine.CalculatorEngine
import com.example.engine.EvaluationResult
import com.example.model.AngleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

  @Test
  fun testBasicAdditionAndSubtraction() {
    val res1 = CalculatorEngine.evaluate("15 + 27")
    assertTrue(res1 is EvaluationResult.Success)
    assertEquals("42", (res1 as EvaluationResult.Success).formatted)

    val res2 = CalculatorEngine.evaluate("100 − 35")
    assertTrue(res2 is EvaluationResult.Success)
    assertEquals("65", (res2 as EvaluationResult.Success).formatted)
  }

  @Test
  fun testMultiplicationAndDivision() {
    val res1 = CalculatorEngine.evaluate("12 × 8")
    assertTrue(res1 is EvaluationResult.Success)
    assertEquals("96", (res1 as EvaluationResult.Success).formatted)

    val res2 = CalculatorEngine.evaluate("144 ÷ 12")
    assertTrue(res2 is EvaluationResult.Success)
    assertEquals("12", (res2 as EvaluationResult.Success).formatted)
  }

  @Test
  fun testOperatorPrecedence() {
    val res = CalculatorEngine.evaluate("2 + 3 × 4")
    assertTrue(res is EvaluationResult.Success)
    assertEquals("14", (res as EvaluationResult.Success).formatted)

    val resParen = CalculatorEngine.evaluate("(2 + 3) × 4")
    assertTrue(resParen is EvaluationResult.Success)
    assertEquals("20", (resParen as EvaluationResult.Success).formatted)
  }

  @Test
  fun testDivisionByZeroPrevention() {
    val res = CalculatorEngine.evaluate("42 ÷ 0")
    assertTrue(res is EvaluationResult.Error)
    assertEquals("Tidak dapat membagi dengan nol", (res as EvaluationResult.Error).message)
  }

  @Test
  fun testFloatingPointPrecision() {
    val res = CalculatorEngine.evaluate("0.1 + 0.2")
    assertTrue(res is EvaluationResult.Success)
    assertEquals("0.3", (res as EvaluationResult.Success).formatted)
  }

  @Test
  fun testPercentage() {
    val res = CalculatorEngine.evaluate("50 % 20")
    assertTrue(res is EvaluationResult.Success)
    assertEquals("10", (res as EvaluationResult.Success).formatted)
  }

  @Test
  fun testPowerAndSqrt() {
    val resPow = CalculatorEngine.evaluate("2 ^ 8")
    assertTrue(resPow is EvaluationResult.Success)
    assertEquals("256", (resPow as EvaluationResult.Success).formatted)

    val resSqrt = CalculatorEngine.evaluate("√(144)")
    assertTrue(resSqrt is EvaluationResult.Success)
    assertEquals("12", (resSqrt as EvaluationResult.Success).formatted)
  }

  @Test
  fun testTrigonometryDegAndRad() {
    val resSin90 = CalculatorEngine.evaluate("sin(90)", AngleMode.DEG)
    assertTrue(resSin90 is EvaluationResult.Success)
    assertEquals("1", (resSin90 as EvaluationResult.Success).formatted)

    val resCos0 = CalculatorEngine.evaluate("cos(0)", AngleMode.DEG)
    assertTrue(resCos0 is EvaluationResult.Success)
    assertEquals("1", (resCos0 as EvaluationResult.Success).formatted)
  }

  @Test
  fun testLivePreview() {
    val preview = CalculatorEngine.livePreview("25 × 4 +")
    assertNotNull(preview)
    assertEquals("100", preview)
  }
}
