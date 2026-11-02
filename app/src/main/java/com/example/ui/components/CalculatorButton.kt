package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ButtonCategory
import com.example.model.CalculatorKey
import com.example.ui.theme.LocalCalculatorColors

@Composable
fun CalculatorButton(
  key: CalculatorKey,
  onClick: (CalculatorKey) -> Unit,
  modifier: Modifier = Modifier,
  hapticsEnabled: Boolean = true,
  fontSize: TextUnit = 24.sp
) {
  val view = LocalView.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Spring animation on button press
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.90f else 1.0f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
    label = "button_press_scale"
  )

  val colors = LocalCalculatorColors.current

  val (backgroundColor, textColor, fontWeight) = when (key.category) {
    ButtonCategory.NUMBER -> Triple(
      colors.numberBtn,
      colors.numberText,
      FontWeight.Medium
    )
    ButtonCategory.OPERATOR -> Triple(
      colors.operatorBtn,
      colors.operatorText,
      FontWeight.SemiBold
    )
    ButtonCategory.FUNCTION -> Triple(
      colors.functionBtn,
      colors.functionText,
      FontWeight.SemiBold
    )
    ButtonCategory.EQUALS -> Triple(
      colors.equalsBtn,
      colors.equalsText,
      FontWeight.Bold
    )
    ButtonCategory.SCIENTIFIC -> Triple(
      colors.functionBtn.copy(alpha = 0.7f),
      colors.functionText,
      FontWeight.Normal
    )
  }

  val shape = RoundedCornerShape(24.dp)

  Box(
    modifier = modifier
      .padding(4.dp)
      .scale(scale)
      .clip(shape)
      .background(backgroundColor)
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) {
        if (hapticsEnabled) {
          view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        onClick(key)
      }
      .testTag(key.testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = key.label,
      color = textColor,
      fontSize = fontSize,
      fontWeight = fontWeight,
      textAlign = TextAlign.Center
    )
  }
}
