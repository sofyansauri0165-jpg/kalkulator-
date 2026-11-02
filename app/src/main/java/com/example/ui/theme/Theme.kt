package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode

data class CalculatorColors(
  val numberBtn: Color,
  val numberText: Color,
  val operatorBtn: Color,
  val operatorText: Color,
  val functionBtn: Color,
  val functionText: Color,
  val equalsBtn: Color,
  val equalsText: Color,
  val displayPreview: Color,
  val historyCard: Color
)

val LocalCalculatorColors = staticCompositionLocalOf {
  CalculatorColors(
    numberBtn = DarkNumberBtn,
    numberText = DarkNumberText,
    operatorBtn = DarkOperatorBtn,
    operatorText = DarkOperatorText,
    functionBtn = DarkFunctionBtn,
    functionText = DarkFunctionText,
    equalsBtn = DarkEqualsBtn,
    equalsText = DarkEqualsText,
    displayPreview = DarkOnSurfaceVariant,
    historyCard = DarkSurfaceVariant
  )
}

private val DarkColorScheme = darkColorScheme(
  primary = AccentAmber,
  secondary = DarkFunctionText,
  tertiary = AccentCyan,
  background = DarkBackground,
  surface = DarkSurface,
  surfaceVariant = DarkSurfaceVariant,
  onPrimary = Color.Black,
  onSecondary = Color.Black,
  onBackground = DarkOnSurface,
  onSurface = DarkOnSurface,
  onSurfaceVariant = DarkOnSurfaceVariant,
  error = AccentRed
)

private val LightColorScheme = lightColorScheme(
  primary = LightEqualsBtn,
  secondary = LightFunctionText,
  tertiary = AccentCyan,
  background = LightBackground,
  surface = LightSurface,
  surfaceVariant = LightSurfaceVariant,
  onPrimary = Color.White,
  onSecondary = Color.White,
  onBackground = LightOnSurface,
  onSurface = LightOnSurface,
  onSurfaceVariant = LightOnSurfaceVariant,
  error = AccentRed
)

@Composable
fun CalculatorTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  val calculatorColors = if (isDark) {
    CalculatorColors(
      numberBtn = DarkNumberBtn,
      numberText = DarkNumberText,
      operatorBtn = DarkOperatorBtn,
      operatorText = DarkOperatorText,
      functionBtn = DarkFunctionBtn,
      functionText = DarkFunctionText,
      equalsBtn = DarkEqualsBtn,
      equalsText = DarkEqualsText,
      displayPreview = DarkOnSurfaceVariant,
      historyCard = DarkSurfaceVariant
    )
  } else {
    CalculatorColors(
      numberBtn = LightNumberBtn,
      numberText = LightNumberText,
      operatorBtn = LightOperatorBtn,
      operatorText = LightOperatorText,
      functionBtn = LightFunctionBtn,
      functionText = LightFunctionText,
      equalsBtn = LightEqualsBtn,
      equalsText = LightEqualsText,
      displayPreview = LightOnSurfaceVariant,
      historyCard = LightSurfaceVariant
    )
  }

  CompositionLocalProvider(LocalCalculatorColors provides calculatorColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
