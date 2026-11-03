/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.browser.sheets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import app.video.player.presentation.components.AppTextField as OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import app.video.player.presentation.components.AppDialogTextButton as TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.video.player.presentation.components.AppAlertDialog
import app.video.player.ui.icons.Icon
import app.video.player.ui.icons.Icons

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistActionSheet(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onCreatePlaylist: suspend (String) -> Long,
  onCreateZipPlaylist: suspend (Uri) -> Result<Long>,
  context: android.content.Context,
  modifier: Modifier = Modifier,
  onCreateM3UPlaylistFromFile: (suspend (Uri) -> Result<Long>)? = null,
  onCreateM3UPlaylist: (suspend (String, String?) -> Result<Long>)? = null,
  onCreateXtreamPlaylist: (suspend (String, String, String) -> Result<Long>)? = null,
  savedXtreamServerUrls: List<String> = emptyList(),
) {
  var showCreateDialog by remember { mutableStateOf(false) }
  val zipScope = rememberCoroutineScope()
  var zipImportJob by remember { mutableStateOf<Job?>(null) }
  var isImportingZip by remember { mutableStateOf(false) }
  val zipPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null && !isImportingZip) {
      isImportingZip = true
      zipImportJob = zipScope.launch {
        try {
          onCreateZipPlaylist(uri).onSuccess {
            app.video.player.ui.components.AppToast.makeText(context, app.video.player.R.string.playlist_zip_import_success, app.video.player.ui.components.AppToast.LENGTH_SHORT).show()
            onDismiss()
          }.onFailure {
            app.video.player.ui.components.AppToast.makeText(context, app.video.player.R.string.playlist_zip_import_failed, app.video.player.ui.components.AppToast.LENGTH_LONG).show()
          }
        } finally {
          isImportingZip = false
        }
      }
    }
  }
  val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
    if (uri != null) {
      runCatching {
        context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }.onSuccess {
        app.video.player.utils.media.MediaLibraryEvents.notifyChanged()
        onDismiss()
      }.onFailure { error ->
        app.video.player.ui.components.AppToast.makeText(
          context,
          context.getString(app.video.player.R.string.playlist_local_access_failed, error.localizedMessage.orEmpty()),
          app.video.player.ui.components.AppToast.LENGTH_LONG,
        ).show()
      }
    }
  }

  if (!isOpen) return

  val sheetState = rememberBottomSheetState(
    initialValue = SheetValue.Hidden,
    enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
  )

  ModalBottomSheet(
    onDismissRequest = {
      zipImportJob?.cancel()
      onDismiss()
    },
    sheetState = sheetState,
    dragHandle = { BottomSheetDefaults.DragHandle() },
    modifier = modifier,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp)
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Title
      Text(
        text =
          androidx.compose.ui.res
            .stringResource(app.video.player.R.string.ui_playlist_options),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(modifier = Modifier.height(4.dp))

      Card(
        onClick = { zipPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/x-zip", "application/octet-stream")) },
        enabled = !isImportingZip,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.RoundedFilled.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
          Text(
            text = androidx.compose.ui.res.stringResource(app.video.player.R.string.playlist_add_zip),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
          )
        }
        if (isImportingZip) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }

      // Action cards
      Card(
        onClick = {
          showCreateDialog = true
        },
        modifier = Modifier.fillMaxWidth(),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
          ),
      ) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.PlaylistAdd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text =
                androidx.compose.ui.res
                  .stringResource(app.video.player.R.string.playlist_create_empty),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
            )
            Text(
              text =
                androidx.compose.ui.res
                  .stringResource(app.video.player.R.string.ui_create_a_new_blank_playlist),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Card(
        onClick = { folderPickerLauncher.launch(null) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.RoundedFilled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
          Text(
            text = androidx.compose.ui.res.stringResource(app.video.player.R.string.playlist_add_local_folder),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }

  // Create Playlist Dialog
  if (showCreateDialog) {
    var playlistName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    AppAlertDialog(
      onDismissRequest = { showCreateDialog = false },
      title = {
        Text(
          text =
            androidx.compose.ui.res
              .stringResource(app.video.player.R.string.ui_create_playlist),
          style = MaterialTheme.typography.headlineSmall,
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = {
              Text(
                androidx.compose.ui.res
                  .stringResource(app.video.player.R.string.ui_playlist_name),
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showCreateDialog = false },
          shape = MaterialTheme.shapes.extraLarge,
        ) {
          Text(
            androidx.compose.ui.res
              .stringResource(app.video.player.R.string.generic_cancel),
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (playlistName.isNotBlank()) {
              coroutineScope.launch {
                try {
                  onCreatePlaylist(playlistName.trim())
                  app.video.player.ui.components.AppToast
                    .makeText(
                      context,
                      context.getString(app.video.player.R.string.ui_playlist_created_successfully),
                      app.video.player.ui.components.AppToast.LENGTH_SHORT,
                    ).show()
                  showCreateDialog = false
                } catch (e: Exception) {
                  app.video.player.ui.components.AppToast
                    .makeText(
                      context,
                      "Failed to create playlist: ${e.message}",
                      app.video.player.ui.components.AppToast.LENGTH_LONG,
                    ).show()
                }
              }
            }
          },
          enabled = playlistName.isNotBlank(),
          shape = MaterialTheme.shapes.extraLarge,
        ) {
          Text(
            androidx.compose.ui.res
              .stringResource(app.video.player.R.string.ui_create),
          )
        }
      },
    )
  }

}
