package app.video.player.ui.preferences.components

import app.video.player.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.Text
import app.video.player.presentation.components.AppDialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.video.player.R

@Composable
fun RestartRequiredDialog(
  title: String,
  message: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = { Text(message) },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.generic_cancel))
      }
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(confirmLabel)
      }
    },
  )
}
