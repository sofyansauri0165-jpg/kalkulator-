package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentRed
import com.example.ui.theme.LocalCalculatorColors
import kotlinx.coroutines.delay

@Composable
fun CalculatorDisplay(
  expression: String,
  livePreview: String?,
  errorMessage: String?,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val colors = LocalCalculatorColors.current
  val scrollState = rememberScrollState()

  var copySuccess by remember { mutableStateOf(false) }

  // Auto-scroll to end of expression when it changes
  LaunchedEffect(expression) {
    if (expression.isNotEmpty()) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }
  }

  // Hide copied indicator after 2 seconds
  LaunchedEffect(copySuccess) {
    if (copySuccess) {
      delay(1800)
      copySuccess = false
    }
  }

  // Dynamic font sizing based on length to keep text readable
  val expressionFontSize = when {
    expression.length <= 8 -> 44.sp
    expression.length <= 14 -> 34.sp
    expression.length <= 20 -> 28.sp
    else -> 22.sp
  }

  fun copyToClipboard(text: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Calculator", text)
    clipboard.setPrimaryClip(clip)
    copySuccess = true
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("calculator_display")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          val toCopy = if (!livePreview.isNullOrBlank()) livePreview else expression
          copyToClipboard(toCopy)
        },
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.Bottom
    ) {

      // Copy confirmation chip
      AnimatedVisibility(
        visible = copySuccess,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(bottom = 6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Copied",
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Tersalin ke papan klip",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }

      // Error message banner
      if (errorMessage != null) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.15f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("error_banner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Error",
              tint = AccentRed,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = errorMessage,
              color = AccentRed,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Main expression row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (expression.isEmpty()) "0" else expression,
          fontSize = expressionFontSize,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.End,
          maxLines = 1,
          fontFamily = FontFamily.SansSerif,
          modifier = Modifier.testTag("expression_text")
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Live result preview
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!livePreview.isNullOrBlank()) {
          Text(
            text = "= $livePreview",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = colors.displayPreview,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.testTag("live_preview_text")
          )
        } else {
          // Placeholder space to prevent jumpy layout
          Spacer(modifier = Modifier.height(28.dp))
        }
      }
    }
  }
}
