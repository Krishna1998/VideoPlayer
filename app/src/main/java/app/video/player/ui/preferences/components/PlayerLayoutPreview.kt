/* SPDX-License-Identifier: AGPL-3.0-or-later */
package app.video.player.ui.preferences.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.video.player.R
import app.video.player.preferences.AppearancePreferences
import app.video.player.preferences.PlaybackControlsPlacement
import app.video.player.preferences.PlayerButton
import app.video.player.preferences.PlayerPreferences
import app.video.player.preferences.SeekbarStyle
import app.video.player.preferences.preference.collectAsState
import app.video.player.presentation.components.captureLiquidGlassBackdrop
import app.video.player.presentation.components.rememberLiquidGlassBackdrop
import app.video.player.ui.icons.AppIcon
import app.video.player.ui.icons.Icon
import app.video.player.ui.icons.Icons
import app.video.player.ui.icons.LocalIconShadow
import app.video.player.ui.liquidglass.AdaptiveControlsContainer
import app.video.player.ui.liquidglass.LocalKyantPlayerBackdrop
import app.video.player.ui.player.controls.LandscapeActionGroup
import app.video.player.ui.player.controls.LandscapeActionSize
import app.video.player.ui.player.controls.LandscapeControlsRow
import app.video.player.ui.player.controls.BottomSlot
import app.video.player.ui.player.controls.PlaybackControlsGeometry
import app.video.player.ui.player.controls.PlayerSectionGap
import app.video.player.ui.player.controls.PlayerButtonTheme
import app.video.player.ui.player.controls.PlayerControlsContentTheme
import app.video.player.ui.player.controls.VideoControlsLayoutPreset
import app.video.player.ui.player.controls.components.AbLoopIcon
import app.video.player.ui.player.controls.components.LocalBorderlessPlayerControls
import app.video.player.ui.player.controls.components.LocalForceDarkPlayerButtonsBackground
import app.video.player.ui.player.controls.components.LocalPlayerButtonAlpha
import app.video.player.ui.player.controls.components.LocalPlayerButtonsOpacity
import app.video.player.ui.player.controls.components.SeekbarStylePreview
import app.video.player.ui.player.controls.resolveBottomControlsLayout
import app.video.player.ui.player.controls.resolvePlaybackControlsGeometry
import app.video.player.ui.preferences.ControlRegion
import app.video.player.ui.theme.spacing
import org.koin.compose.koinInject

/** A scaled sample scene: editing never invokes native playback, Cast, PiP, or script actions. */
@Composable
internal fun PlayerLayoutPreview(
  region: ControlRegion,
  topRightButtons: List<PlayerButton>,
  bottomRightButtons: List<PlayerButton>,
  bottomLeftButtons: List<PlayerButton>,
  portraitBottomButtons: List<PlayerButton>,
  preset: VideoControlsLayoutPreset,
  playlist: Boolean,
  onPlaylistChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  expanded: Boolean = true,
  onExpandedChange: (() -> Unit)? = null,
) {
  val appearance = koinInject<AppearancePreferences>()
  val forceDark by appearance.forceDarkPlayerButtonsBackground.collectAsState()
  val opacity by appearance.playerButtonsBackgroundOpacity.collectAsState()
  val hideBackground by appearance.hidePlayerButtonsBackground.collectAsState()
  val buttonAlpha by appearance.playerButtonsOpacity.collectAsState()
  val seekbarOpacity by appearance.seekbarOpacity.collectAsState()
  val hideTitle by appearance.hideVideoTitle.collectAsState()
  val seekbarStyle by appearance.seekbarStyle.collectAsState()
  val glassEnabled by appearance.liquidGlassEnabled.collectAsState()
  val playbackPreference =
    if (region.isPortrait) appearance.portraitPlaybackControlsPlacement else appearance.landscapePlaybackControlsPlacement
  val playbackControlsPlacement by playbackPreference.collectAsState()
  val wavy by koinInject<PlayerPreferences>().useWavySeekbar.collectAsState()
  val title = stringResource(region.titleRes)
  val configuration = LocalConfiguration.current
  val shortSide = minOf(configuration.screenWidthDp, configuration.screenHeightDp).coerceAtLeast(1).dp
  val longSide = maxOf(configuration.screenWidthDp, configuration.screenHeightDp).coerceAtLeast(1).dp
  val sceneWidth = if (region.isPortrait) shortSide else longSide
  val sceneHeight = if (region.isPortrait) longSide else shortSide
  Surface(modifier, shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLow) {
    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text(stringResource(R.string.pref_layout_preview), style = MaterialTheme.typography.titleSmall)
          Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        FilterChip(
          selected = playlist,
          onClick = { onPlaylistChange(!playlist) },
          label = { Text(stringResource(R.string.ui_playlist)) },
        )
        if (onExpandedChange != null) {
          IconButton(onClick = onExpandedChange) {
            Icon(
              if (expanded) Icons.RoundedFilled.ExpandLess else Icons.RoundedFilled.ExpandMore,
              contentDescription = stringResource(if (expanded) R.string.generic_collapse else R.string.generic_expand),
            )
          }
        }
      }
      if (expanded) BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        val ratio = sceneWidth / sceneHeight
        val frameWidth = minOf(maxWidth, maxHeight * ratio)
        val frameHeight = frameWidth / ratio
        val scale = (frameWidth / sceneWidth).coerceAtLeast(0f)
        Layout(
          modifier = Modifier.size(frameWidth, frameHeight).clip(MaterialTheme.shapes.large)
            .clearAndSetSemantics { contentDescription = title },
          content = {
            CompositionLocalProvider(
              LocalLayoutDirection provides LayoutDirection.Ltr,
              LocalForceDarkPlayerButtonsBackground provides forceDark,
              LocalPlayerButtonsOpacity provides opacity,
              LocalPlayerButtonAlpha provides buttonAlpha,
              LocalBorderlessPlayerControls provides hideBackground,
              LocalIconShadow provides hideBackground,
            ) {
              PlayerButtonTheme {
                PlayerControlsContentTheme {
                  PreviewScene(
                    region, topRightButtons, bottomRightButtons, bottomLeftButtons, portraitBottomButtons,
                    preset, playbackControlsPlacement, playlist, glassEnabled, seekbarStyle, seekbarOpacity, hideTitle, wavy, sceneWidth,
                  )
                }
              }
            }
          },
        ) { measurables, constraints ->
          val scene = measurables.single().measure(Constraints.fixed(sceneWidth.roundToPx(), sceneHeight.roundToPx()))
          layout(constraints.maxWidth, constraints.maxHeight) {
            scene.placeWithLayer(0, 0) {
              scaleX = scale
              scaleY = scale
              transformOrigin = TransformOrigin(0f, 0f)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PreviewScene(
  region: ControlRegion,
  topRightButtons: List<PlayerButton>,
  bottomRightButtons: List<PlayerButton>,
  bottomLeftButtons: List<PlayerButton>,
  portraitBottomButtons: List<PlayerButton>,
  preset: VideoControlsLayoutPreset,
  playbackControlsPlacement: PlaybackControlsPlacement,
  playlist: Boolean,
  glassEnabled: Boolean,
  seekbarStyle: SeekbarStyle,
  seekbarOpacity: Float = 1.0f,
  hideTitle: Boolean = false,
  wavy: Boolean,
  sceneWidth: Dp,
) {
  val spacing = MaterialTheme.spacing
  val hasActions =
    if (region.isPortrait) portraitBottomButtons.isNotEmpty()
    else bottomRightButtons.isNotEmpty() || bottomLeftButtons.isNotEmpty()
  val sharedLandscapeRow = !region.isPortrait && playbackControlsPlacement != PlaybackControlsPlacement.Center
  val geometry = resolvePlaybackControlsGeometry(sceneWidth, spacing, landscape = sharedLandscapeRow)
  val layout = resolveBottomControlsLayout(
    preset.seekbarPlacement, hasActions, playbackControlsPlacement, sharedLandscapeRow = sharedLandscapeRow,
  )
  val highlightShape = MaterialTheme.shapes.medium
  val highlightColor = MaterialTheme.colorScheme.primary
  fun highlightRegion(selected: Boolean): Modifier =
    if (selected) Modifier.background(highlightColor.copy(alpha = 0.14f), highlightShape)
      .border(2.dp, highlightColor, highlightShape) else Modifier
  val previewLandscapeButton: @Composable (PlayerButton) -> Unit = { button ->
    PreviewIcon(button.icon, LandscapeActionSize)
  }
  val backdrop = rememberLiquidGlassBackdrop()
  val background = Brush.linearGradient(listOf(Color(0xFF112333), Color(0xFF27434A), Color(0xFF10131D)))
  Box(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize().captureLiquidGlassBackdrop(backdrop, glassEnabled).background(background))
    CompositionLocalProvider(LocalKyantPlayerBackdrop provides backdrop.takeIf { glassEnabled }) {
      Row(
        Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = spacing.large, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
      ) {
        Row(
          Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
          PreviewIcon(Icons.RoundedFilled.ArrowBack, 48.dp)
          if (!hideTitle) {
            Text(
              stringResource(R.string.ui_video_title), color = Color.White,
              maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
          }
        }
        if (!region.isPortrait) {
          LandscapeActionGroup(
            buttons = topRightButtons,
            modifier = Modifier.widthIn(
              max = (sceneWidth - spacing.large * 2 - LandscapeActionSize - spacing.small)
                .coerceAtLeast(LandscapeActionSize),
            ).then(highlightRegion(region == ControlRegion.TOP_RIGHT)),
            endAligned = true,
            fillWidth = false,
            renderButton = previewLandscapeButton,
          )
        }
      }
      if (!sharedLandscapeRow && playbackControlsPlacement == PlaybackControlsPlacement.Center) {
        PreviewPlayback(playlist, geometry, Modifier.align(Alignment.Center))
      }
      Column(
        Modifier.align(Alignment.BottomCenter).fillMaxWidth()
          .padding(horizontal = if (region.isPortrait) spacing.large else 0.dp,
            vertical = PlayerSectionGap),
        verticalArrangement = Arrangement.spacedBy(PlayerSectionGap),
      ) {
        layout.bottomToTop.asReversed().forEach { slot ->
          when (slot) {
            BottomSlot.Seekbar -> Box(
              Modifier
                .padding(horizontal = if (region.isPortrait) 0.dp else spacing.large)
                .alpha(seekbarOpacity.coerceIn(0f, 1f))
            ) {
              SeekbarStylePreview(seekbarStyle, useWavySeekbar = wavy)
            }
            BottomSlot.Playback -> {
              if (region.isPortrait) {
                PreviewPlayback(
                  playlist, geometry,
                  Modifier.fillMaxWidth().padding(vertical = if (glassEnabled) 8.dp else 0.dp),
                  evenlySpaced = true,
                )
              } else {
                LandscapeControlsRow(
                  leftButtons = bottomLeftButtons,
                  rightButtons = bottomRightButtons,
                  playbackWidth = if (playlist) geometry.playPauseSize + geometry.skipButtonSize * 2 + geometry.buttonGap * 2
                    else geometry.playPauseSize,
                  modifier = Modifier.padding(horizontal = spacing.medium),
                  leftModifier = highlightRegion(region == ControlRegion.BOTTOM_LEFT),
                  rightModifier = highlightRegion(region == ControlRegion.BOTTOM_RIGHT),
                  renderButton = previewLandscapeButton,
                  playbackControls = { PreviewPlayback(playlist, geometry) },
                )
              }
            }
            BottomSlot.Buttons -> {
              if (!region.isPortrait) {
                LandscapeControlsRow(
                  leftButtons = bottomLeftButtons,
                  rightButtons = bottomRightButtons,
                  playbackWidth = 0.dp,
                  modifier = Modifier.padding(horizontal = spacing.medium),
                  leftModifier = highlightRegion(region == ControlRegion.BOTTOM_LEFT),
                  rightModifier = highlightRegion(region == ControlRegion.BOTTOM_RIGHT),
                  renderButton = previewLandscapeButton,
                  playbackControls = {},
                )
              } else {
                val left = if (region.isPortrait) emptyList() else bottomLeftButtons
                val right = if (region.isPortrait) portraitBottomButtons else bottomRightButtons
                val actionGap = if (region.isPortrait) spacing.medium else spacing.extraSmall
                Row(
                  Modifier.fillMaxWidth().heightIn(min = 48.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(spacing.smaller),
                ) {
                  if (left.isNotEmpty()) {
                    PreviewButtons(
                      left, region == ControlRegion.BOTTOM_LEFT,
                      Modifier.weight(1f), buttonGap = actionGap,
                    )
                  }
                  if (right.isNotEmpty()) {
                    PreviewButtons(
                      right, region == ControlRegion.BOTTOM_RIGHT || region == ControlRegion.PORTRAIT_BOTTOM,
                      Modifier.weight(1f), endAligned = !region.isPortrait,
                      centered = region.isPortrait,
                      buttonGap = actionGap,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PreviewButtons(
  buttons: List<PlayerButton>,
  highlighted: Boolean,
  modifier: Modifier,
  endAligned: Boolean = false,
  centered: Boolean = false,
  buttonGap: Dp = MaterialTheme.spacing.extraSmall,
) {
  val shape = MaterialTheme.shapes.medium
  Box(
    modifier.heightIn(min = 48.dp)
      .then(
        if (highlighted) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), shape)
          .border(2.dp, MaterialTheme.colorScheme.primary, shape)
        else Modifier,
      ),
  ) {
    FlowRow(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(
        buttonGap,
        if (endAligned) Alignment.End else if (centered) Alignment.CenterHorizontally else Alignment.Start,
      ),
      verticalArrangement = Arrangement.spacedBy(buttonGap),
    ) {
      buttons.forEach { button ->
        AdaptiveControlsContainer(
          onClick = {}, isInteractive = false, buttonSize = 48.dp,
          color = Color.Unspecified,
          horizontalPadding = if (button == PlayerButton.VIDEO_TITLE || button == PlayerButton.CURRENT_CHAPTER) 12.dp else 0.dp,
          modifier = if (button == PlayerButton.VIDEO_TITLE || button == PlayerButton.CURRENT_CHAPTER) Modifier else Modifier.size(48.dp),
        ) {
          when (button) {
            PlayerButton.VIDEO_TITLE -> Text(stringResource(R.string.ui_video_title), maxLines = 1)
            PlayerButton.CURRENT_CHAPTER -> Text(stringResource(R.string.ui_1_06_chapter_1), maxLines = 1)
            PlayerButton.AB_LOOP -> AbLoopIcon(Modifier.size(30.dp))
            else -> Icon(
              button.icon, null,
              Modifier.size(24.dp).then(if (button == PlayerButton.VERTICAL_FLIP) Modifier.rotate(90f) else Modifier),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PreviewPlayback(
  playlist: Boolean,
  geometry: PlaybackControlsGeometry,
  modifier: Modifier = Modifier,
  evenlySpaced: Boolean = false,
) {
  Row(
    modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement =
      if (evenlySpaced) Arrangement.SpaceEvenly else Arrangement.spacedBy(geometry.buttonGap, Alignment.CenterHorizontally),
  ) {
    if (playlist) PreviewIcon(Icons.RoundedFilled.SkipPrevious, geometry.skipButtonSize)
    PreviewIcon(Icons.RoundedFilled.PlayArrow, geometry.playPauseSize)
    if (playlist) PreviewIcon(Icons.RoundedFilled.SkipNext, geometry.skipButtonSize)
  }
}

@Composable
private fun PreviewIcon(icon: AppIcon, size: Dp) {
  AdaptiveControlsContainer(
    onClick = {}, isInteractive = false, modifier = Modifier.size(size),
    color = Color.Unspecified,
    buttonSize = size, horizontalPadding = 0.dp,
  ) {
    Icon(icon, null, Modifier.size(size / 2))
  }
}

