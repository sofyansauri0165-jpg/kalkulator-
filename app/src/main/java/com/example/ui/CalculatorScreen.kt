package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CalculatorKey
import com.example.model.ThemeMode
import com.example.ui.components.CalculatorButton
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.HistorySheet
import com.example.ui.components.ScientificKeypad

@Composable
fun CalculatorScreen(
  viewModel: CalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isWideScreen = maxWidth > 600.dp || maxHeight < 500.dp

      if (isWideScreen) {
        // Landscape or Tablet Layout: side-by-side panes
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Left Pane: Header + Display + Scientific Panel
          Column(
            modifier = Modifier
              .weight(1.1f)
              .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            CalculatorTopBar(
              uiState = uiState,
              onToggleHistory = viewModel::toggleHistory,
              onToggleScientific = viewModel::toggleScientific,
              onCycleTheme = viewModel::cycleTheme,
              onToggleHaptics = viewModel::toggleHaptics
            )

            CalculatorDisplay(
              expression = uiState.expression,
              livePreview = uiState.livePreview,
              errorMessage = uiState.errorMessage,
              modifier = Modifier.weight(1f)
            )

            ScientificKeypad(
              isOpen = true,
              angleMode = uiState.angleMode,
              onToggleAngleMode = viewModel::toggleAngleMode,
              onKeyClick = viewModel::onKeyPressed,
              hapticsEnabled = uiState.hapticsEnabled
            )
          }

          // Right Pane: Standard Keypad
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            StandardKeypad(
              onKeyClick = viewModel::onKeyPressed,
              hapticsEnabled = uiState.hapticsEnabled,
              modifier = Modifier.fillMaxSize()
            )
          }
        }
      } else {
        // Portrait Mobile Layout: Vertical Stack
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          // Top Bar with Actions
          CalculatorTopBar(
            uiState = uiState,
            onToggleHistory = viewModel::toggleHistory,
            onToggleScientific = viewModel::toggleScientific,
            onCycleTheme = viewModel::cycleTheme,
            onToggleHaptics = viewModel::toggleHaptics
          )

          // Display Area
          CalculatorDisplay(
            expression = uiState.expression,
            livePreview = uiState.livePreview,
            errorMessage = uiState.errorMessage,
            modifier = Modifier.weight(1f)
          )

          // Collapsible Scientific Keypad
          ScientificKeypad(
            isOpen = uiState.isScientificOpen,
            angleMode = uiState.angleMode,
            onToggleAngleMode = viewModel::toggleAngleMode,
            onKeyClick = viewModel::onKeyPressed,
            hapticsEnabled = uiState.hapticsEnabled
          )

          // Primary Calculator Grid
          StandardKeypad(
            onKeyClick = viewModel::onKeyPressed,
            hapticsEnabled = uiState.hapticsEnabled,
            modifier = Modifier
              .fillMaxWidth()
              .height(380.dp)
          )
        }
      }
    }

    // History Modal Sheet
    HistorySheet(
      isOpen = uiState.isHistoryOpen,
      history = uiState.history,
      onClose = viewModel::closeHistory,
      onRecallItem = { item -> viewModel.recallHistoryItem(item) },
      onClearHistory = viewModel::clearHistory
    )
  }
}

@Composable
fun CalculatorTopBar(
  uiState: CalculatorUiState,
  onToggleHistory: () -> Unit,
  onToggleScientific: () -> Unit,
  onCycleTheme: () -> Unit,
  onToggleHaptics: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // App Title & Brand
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.Calculate,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Top action toolbar
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Scientific toggle
      IconButton(
        onClick = onToggleScientific,
        modifier = Modifier.testTag("btn_toggle_scientific")
      ) {
        Icon(
          imageVector = Icons.Default.Functions,
          contentDescription = "Scientific functions",
          tint = if (uiState.isScientificOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Haptics toggle
      IconButton(
        onClick = onToggleHaptics,
        modifier = Modifier.testTag("btn_toggle_haptics")
      ) {
        Icon(
          imageVector = Icons.Default.Vibration,
          contentDescription = stringResource(R.string.haptics),
          tint = if (uiState.hapticsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
      }

      // Theme cycle
      IconButton(
        onClick = onCycleTheme,
        modifier = Modifier.testTag("btn_cycle_theme")
      ) {
        val (themeIcon, themeDesc) = when (uiState.themeMode) {
          ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto to stringResource(R.string.theme_system)
          ThemeMode.DARK -> Icons.Default.DarkMode to stringResource(R.string.theme_dark)
          ThemeMode.LIGHT -> Icons.Default.LightMode to stringResource(R.string.theme_light)
        }
        Icon(
          imageVector = themeIcon,
          contentDescription = themeDesc,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // History with badge
      IconButton(
        onClick = onToggleHistory,
        modifier = Modifier.testTag("btn_toggle_history")
      ) {
        if (uiState.history.isNotEmpty()) {
          BadgedBox(
            badge = {
              Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ) {
                Text(text = uiState.history.size.toString())
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = stringResource(R.string.history_title),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(R.string.history_title),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun StandardKeypad(
  onKeyClick: (CalculatorKey) -> Unit,
  hapticsEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Row 1: C, ⌫, %, ÷
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      CalculatorButton(
        key = CalculatorKey.Clear,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Backspace,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Percent,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Divide,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        fontSize = 28.sp,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      CalculatorButton(
        key = CalculatorKey.Seven,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Eight,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Nine,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Multiply,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        fontSize = 28.sp,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      CalculatorButton(
        key = CalculatorKey.Four,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Five,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Six,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Subtract,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        fontSize = 28.sp,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      CalculatorButton(
        key = CalculatorKey.One,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Two,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Three,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Add,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        fontSize = 28.sp,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
    }

    // Row 5: ±, 0, ., =
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      CalculatorButton(
        key = CalculatorKey.PlusMinus,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Zero,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Decimal,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        fontSize = 28.sp,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
      CalculatorButton(
        key = CalculatorKey.Equals,
        onClick = onKeyClick,
        hapticsEnabled = hapticsEnabled,
        fontSize = 30.sp,
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
      )
    }
  }
}
