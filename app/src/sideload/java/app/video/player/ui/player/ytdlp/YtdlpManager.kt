/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.player.ytdlp

import android.content.Context
import app.video.player.preferences.SubtitlesPreferences
import app.video.player.preferences.YtdlPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object YtdlpManager {
  private val _installationInfo = MutableStateFlow<YtdlpInstallationInfo?>(null)
  val installationInfo: StateFlow<YtdlpInstallationInfo?> = _installationInfo.asStateFlow()

  fun requiresYtdlp(source: String): Boolean = false
  fun isPotentialPlaylistUrl(source: String): Boolean = false
  fun isInstalled(context: Context): Boolean = false
  fun isPlaybackRuntimeReady(context: Context): Boolean = false

  fun getYtdlDir(context: Context): File = File(context.filesDir, "ytdl")
  fun getExecutablePath(context: Context): String = ""

  suspend fun refreshInstallationInfo(context: Context): YtdlpInstallationInfo {
    val info = YtdlpInstallationInfo.NotInstalled
    _installationInfo.value = info
    return info
  }

  suspend fun extractPlaylist(
    context: Context,
    source: String,
    preferences: YtdlPreferences,
    userAgentOverride: String? = null,
  ): Result<YtdlpPlaylistMetadata> = Result.failure(UnsupportedOperationException("yt-dlp has been removed"))

  suspend fun prepareForPlayback(
    context: Context,
    source: String,
    onLog: (String) -> Unit = {},
  ): Boolean = true

  suspend fun ensureRuntimeInstalled(
    context: Context,
    onLog: (String) -> Unit = {},
  ): Boolean = false

  suspend fun runInstall(
    context: Context,
    onLog: (String) -> Unit = {},
  ): Boolean = false

  suspend fun runUpdate(
    context: Context,
    onLog: (String) -> Unit = {},
  ): Boolean = false

  suspend fun runUpdateToNightly(
    context: Context,
    onLog: (String) -> Unit = {},
  ): Boolean = false

  fun configureMpvHookBeforeInit(context: Context) {}

  fun setupMpvOptions(
    context: Context,
    ytdlPreferences: YtdlPreferences,
    subtitlesPreferences: SubtitlesPreferences,
  ) {}

  fun playPlaylist(
    context: Context,
    playlist: YtdlpPlaylistMetadata,
    sourceTag: String,
  ) {}

  fun startPythonProcess(command: List<String>): Process {
    throw UnsupportedOperationException("yt-dlp has been removed")
  }
}
