/* SPDX-License-Identifier: AGPL-3.0-or-later */
package app.video.player.ui.player.controls

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.video.player.R
import app.video.player.preferences.PlaybackControlsPosition
import app.video.player.preferences.SeekbarPlacement
import app.video.player.ui.theme.Spacing

internal enum class BottomSlot { Buttons, Playback, Seekbar }

internal enum class VideoControlsLayoutPreset(
  @StringRes val titleRes: Int,
  val seekbarPlacement: SeekbarPlacement,
  val playbackPosition: PlaybackControlsPosition,
) {
  CenterSeekbarAboveActions(
    R.string.pref_layout_center_seekbar_above,
    SeekbarPlacement.AboveButtons,
    PlaybackControlsPosition.Center,
  ),
  CenterSeekbarBelowActions(
    R.string.pref_layout_center_seekbar_below,
    SeekbarPlacement.BelowButtons,
    PlaybackControlsPosition.Center,
  ),
  PlaybackAboveSeekbar(
    R.string.pref_layout_playback_above,
    SeekbarPlacement.BelowButtons,
    PlaybackControlsPosition.AboveSeekbar,
  ),
  PlaybackBelowSeekbar(
    R.string.pref_layout_playback_below,
    SeekbarPlacement.AboveButtons,
    PlaybackControlsPosition.BelowSeekbar,
  ),
  ;

  companion object {
    // Keep existing saved keys; older three-row combinations collapse toward their playback position.
    fun fromPreferences(
      seekbarPlacement: SeekbarPlacement,
      playbackPosition: PlaybackControlsPosition,
    ): VideoControlsLayoutPreset = when (playbackPosition) {
      PlaybackControlsPosition.AboveSeekbar -> PlaybackAboveSeekbar
      PlaybackControlsPosition.BelowSeekbar -> PlaybackBelowSeekbar
      PlaybackControlsPosition.Center ->
        if (seekbarPlacement == SeekbarPlacement.AboveButtons) CenterSeekbarAboveActions else CenterSeekbarBelowActions
    }
  }
}

internal data class BottomControlsLayout(
  val bottomToTop: List<BottomSlot>,
)

internal data class PlaybackControlsGeometry(
  val playPauseSize: Dp,
  val skipButtonSize: Dp,
  val buttonGap: Dp,
)

internal fun resolvePlaybackControlsGeometry(
  width: Dp,
  spacing: Spacing,
  landscape: Boolean = false,
): PlaybackControlsGeometry {
  if (landscape) return PlaybackControlsGeometry(56.dp, 48.dp, 8.dp)
  val compact = width < 360.dp
  return PlaybackControlsGeometry(
    playPauseSize = if (compact) 56.dp else 68.dp,
    skipButtonSize = if (compact) 48.dp else 60.dp,
    buttonGap = if (compact) 4.dp else spacing.large,
  )
}

internal fun resolveBottomControlsLayout(
  seekbarPlacement: SeekbarPlacement,
  hasButtons: Boolean = true,
  playbackControlsPlacement: PlaybackControlsPosition = PlaybackControlsPosition.Center,
  sharedLandscapeRow: Boolean = false,
): BottomControlsLayout =
  BottomControlsLayout(
    bottomToTop = buildList {
      if (sharedLandscapeRow) {
        if (seekbarPlacement == SeekbarPlacement.AboveButtons) {
          add(BottomSlot.Playback)
          add(BottomSlot.Seekbar)
        } else {
          add(BottomSlot.Seekbar)
          add(BottomSlot.Playback)
        }
        return@buildList
      }
      if (hasButtons && seekbarPlacement == SeekbarPlacement.AboveButtons) add(BottomSlot.Buttons)
      if (playbackControlsPlacement == PlaybackControlsPosition.BelowSeekbar) add(BottomSlot.Playback)
      add(BottomSlot.Seekbar)
      if (playbackControlsPlacement == PlaybackControlsPosition.AboveSeekbar) add(BottomSlot.Playback)
      if (hasButtons && seekbarPlacement == SeekbarPlacement.BelowButtons) add(BottomSlot.Buttons)
    },
  )
