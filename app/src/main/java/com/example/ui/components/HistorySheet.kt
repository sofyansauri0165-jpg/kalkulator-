package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.HistoryItem
import com.example.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
  isOpen: Boolean,
  history: List<HistoryItem>,
  onClose: () -> Unit,
  onRecallItem: (HistoryItem) -> Unit,
  onClearHistory: () -> Unit
) {
  if (!isOpen) return

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val colors = LocalCalculatorColors.current

  fun copyText(text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Calculator History", text))
  }

  ModalBottomSheet(
    onDismissRequest = onClose,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("history_sheet")
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Row {
          if (history.isNotEmpty()) {
            IconButton(
              onClick = onClearHistory,
              modifier = Modifier.testTag("btn_clear_history")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = stringResource(R.string.clear_history),
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("btn_close_history")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (history.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = stringResource(R.string.history_empty),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(history, key = { it.id }) { item ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = colors.historyCard),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onRecallItem(item) }
                .testTag("history_item_${item.id}")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = item.formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  IconButton(
                    onClick = { copyText("${item.expression} = ${item.result}") },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copy equation",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = item.expression,
                  fontSize = 18.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = "= ${item.result}",
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
