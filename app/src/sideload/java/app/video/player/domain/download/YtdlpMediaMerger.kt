/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.domain.download

import java.io.File

internal object YtdlpMediaMerger {
  fun merge(videoFile: File, audioFile: File, outputFile: File): Boolean = false
}
