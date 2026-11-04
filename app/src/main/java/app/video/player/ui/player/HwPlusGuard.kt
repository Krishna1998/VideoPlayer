/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.player

import android.content.Context
import androidx.annotation.StringRes
import app.video.player.R
import app.video.player.ui.components.AppToast
import app.video.player.ui.components.AppToastType

/**
 * HW+ (direct MediaCodec) hands frames to the renderer as GPU images, so mpv skips CPU video
 * filters (`vf`) and shader passes hooked on the LUMA/CHROMA planes for them.
 */
internal object HwPlusGuard {
  private fun inUse(): Boolean = PlaybackSession.getPropertyString("hwdec-current") == Decoder.HWPlus.value

  /**
   * Returns true, and says why in a toast, when HW+ would silently ignore [feature].
   */
  fun blocks(context: Context, @StringRes feature: Int, retry: () -> Unit = {}): Boolean {
    if (!inUse()) return false
    AppToast.makeText(
      context,
      context.getString(R.string.player_hwplus_unsupported, context.getString(feature)),
      AppToast.LENGTH_LONG,
    ).withType(AppToastType.Warning).show()
    return true
  }
}
