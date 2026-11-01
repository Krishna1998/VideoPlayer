/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.domain.download

import android.content.Context
import app.video.player.preferences.YtdlPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class YtdlpDownloadEngine(
  private val context: Context,
  private val preferences: YtdlPreferences,
  private val outputReservations: DownloadOutputReservations,
) {
  enum class JobState { QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED }

  data class Job(
    val id: Int,
    val url: String,
    val title: String,
    val directory: String,
    val formatSelector: String? = null,
    val mergeSeparateStreams: Boolean = false,
    val posterUrl: String? = null,
    val state: JobState = JobState.FAILED,
    val progressPercent: Float = 0f,
    val detail: String = "",
    val error: String? = "yt-dlp has been removed",
    val outputFile: String? = null,
    val artifactFiles: Set<String> = emptySet(),
  ) {
    val isActive: Boolean get() = false
  }

  private val nextId = AtomicInteger(1)
  private val _jobs = MutableStateFlow<List<Job>>(emptyList())
  val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

  fun enqueue(
    url: String,
    title: String,
    directory: File,
    formatSelector: String? = null,
    mergeSeparateStreams: Boolean = false,
    posterUrl: String? = null,
  ): Int = nextId.getAndIncrement()

  fun cancel(jobId: Int) {}
  fun retry(jobId: Int) {}
  fun remove(jobId: Int, deleteFiles: Boolean = true) {}
  fun clearAll(deleteFiles: Boolean = true) {}
}
