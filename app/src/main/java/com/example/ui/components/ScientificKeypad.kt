package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.model.CalculatorKey

@Composable
fun ScientificKeypad(
  isOpen: Boolean,
  angleMode: AngleMode,
  onToggleAngleMode: () -> Unit,
  onKeyClick: (CalculatorKey) -> Unit,
  hapticsEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = isOpen,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Row 1
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // DEG / RAD Toggle Pill
        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onToggleAngleMode() }
            .testTag("btn_angle_mode"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = angleMode.name,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        CalculatorButton(
          key = CalculatorKey.Sin,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 16.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.Cos,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 16.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.Tan,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 16.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
      }

      // Row 2
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalculatorButton(
          key = CalculatorKey.Ln,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 16.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.Log,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 16.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.Sqrt,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 18.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.Power,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 18.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
      }

      // Row 3
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalculatorButton(
          key = CalculatorKey.Pi,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 18.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.Euler,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 18.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.OpenParen,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 18.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
        CalculatorButton(
          key = CalculatorKey.CloseParen,
          onClick = onKeyClick,
          hapticsEnabled = hapticsEnabled,
          fontSize = 18.sp,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
        )
      }
    }
  }
}
