/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.video.player.ui.background

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Animated background stub - animations removed to reduce app size.
 */
@Composable
fun AnimatedBackground(
  type: BackgroundType,
  parallax: Boolean,
  modifier: Modifier = Modifier,
  paused: Boolean = false,
) {
}

data class ActiveBackground(
  val type: BackgroundType,
  val parallax: Boolean,
  val overWallpaper: Boolean,
  val event: SeasonalEvent?,
)

@Composable
fun rememberActiveBackground(): ActiveBackground =
  ActiveBackground(BackgroundType.NONE, parallax = false, overWallpaper = false, event = null)

@Composable
fun rememberSeasonalEvent(): SeasonalEvent? = null

object BackdropGate {
  var covers: Int = 0
}
