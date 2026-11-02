package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import com.example.model.CalculatorKey
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {

  private val viewModel: CalculatorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val uiState by viewModel.uiState.collectAsState()

      // Handle Android system Back button
      BackHandler(enabled = uiState.isHistoryOpen || uiState.isScientificOpen) {
        if (uiState.isHistoryOpen) {
          viewModel.closeHistory()
        } else if (uiState.isScientificOpen) {
          viewModel.toggleScientific()
        }
      }

      CalculatorTheme(themeMode = uiState.themeMode) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          CalculatorScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    event?.let {
      val unicodeChar = it.unicodeChar.toChar()
      if (viewModel.handleKeyboardInput(unicodeChar)) {
        return true
      }
      when (keyCode) {
        KeyEvent.KEYCODE_DEL -> {
          viewModel.onKeyPressed(CalculatorKey.Backspace)
          return true
        }
        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_EQUALS -> {
          viewModel.onKeyPressed(CalculatorKey.Equals)
          return true
        }
        KeyEvent.KEYCODE_ESCAPE -> {
          viewModel.onKeyPressed(CalculatorKey.Clear)
          return true
        }
      }
    }
    return super.onKeyDown(keyCode, event)
  }
}
