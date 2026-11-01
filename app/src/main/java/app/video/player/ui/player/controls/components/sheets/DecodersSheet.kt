/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.player.controls.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.video.player.BuildConfig
import app.video.player.R
import app.video.player.presentation.components.PlayerSheet
import app.video.player.ui.player.Decoder
import app.video.player.ui.player.PlaybackSession
import app.video.player.ui.player.RendererBackendPolicy
import app.video.player.ui.theme.AppConnectedShapeTokens

@Composable
fun DecodersSheet(
  selectedDecoder: Decoder,
  onSelect: (Decoder) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val gpuApi by PlaybackSession.propString["gpu-api"].collectAsState()
  val isVulkanActive = gpuApi == "vulkan"
  val directMediaCodecAllowed =
    RendererBackendPolicy.canUseDirectMediaCodec(
      usesVulkan = isVulkanActive,
      buildSupportsMediaCodecVulkan = BuildConfig.MPV_SUPPORTS_MEDIACODEC_VULKAN,
    )

  PlayerSheet(onDismissRequest, title = stringResource(R.string.btn_label_decoder)) {
    val decoders = Decoder.entries.minusElement(Decoder.Auto)
    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(AppConnectedShapeTokens.spacing),
    ) {
      itemsIndexed(decoders, key = { _, decoder -> decoder.name }) { index, decoder ->
        AudioTrackRow(
          title = stringResource(R.string.player_sheets_decoder_formatted, decoder.title, decoder.value),
          isSelected = selectedDecoder == decoder,
          enabled = decoder != Decoder.HWPlus || directMediaCodecAllowed,
          onClick = { onSelect(decoder) },
          groupIndex = index,
          groupSize = decoders.size,
        )
      }
    }
  }
}
