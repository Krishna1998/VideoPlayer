/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.player.controls.components.panels

import app.video.player.ui.player.PlaybackSession

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import app.video.player.R
import app.video.player.preferences.DecoderPreferences
import app.video.player.preferences.preference.collectAsState
import app.video.player.preferences.preference.deleteAndGet
import app.video.player.presentation.components.ExpandableCard
import app.video.player.presentation.components.SliderItem
import app.video.player.ui.icons.Icon
import app.video.player.ui.icons.Icons
import app.video.player.ui.player.DebandSettings
import app.video.player.ui.player.Debanding
import app.video.player.ui.player.controls.CARDS_MAX_WIDTH
import app.video.player.ui.player.controls.panelCardsColors
import app.video.player.ui.theme.spacing
import app.video.player.ui.theme.AppConnectedShapeTokens
import app.video.player.ui.utils.currentMpvConfigOverrideOptions
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.koin.compose.koinInject

@Composable
fun VideoSettingsDebandCard(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.large,
) {
  val decoderPreferences = koinInject<DecoderPreferences>()
  val configOwnedOptions = currentMpvConfigOverrideOptions()
  val modeOptions = setOf("deband", "vf")
  val modeEnabled = modeOptions.none(configOwnedOptions::contains)
  val debandOptions = modeOptions + DebandSettings.entries.map(DebandSettings::mpvProperty)
  val deband by decoderPreferences.debanding.collectAsState()
  var isExpanded by remember { mutableStateOf(true) }

  ExpandableCard(
    isExpanded,
    title = {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
        Icon(Icons.RoundedFilled.Gradient, null)
        Text(stringResource(R.string.player_sheets_deband_title))
      }
    },
    onExpand = { isExpanded = !isExpanded },
    modifier.widthIn(max = CARDS_MAX_WIDTH),
    colors = panelCardsColors(),
    shape = shape,
  ) {
    ProvidePreferenceLocals {
      Column(verticalArrangement = Arrangement.spacedBy(AppConnectedShapeTokens.spacing)) {
        Row(
          Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = MaterialTheme.spacing.extraSmall, end = MaterialTheme.spacing.medium),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Debanding.entries.forEach {
            IconToggleButton(
              checked = deband == it,
              enabled = modeEnabled,
              onCheckedChange = { _ ->
                decoderPreferences.debanding.set(it)
                PlaybackSession.applyDebanding(it)
              },
            ) {
              when (it) {
                Debanding.None -> Icon(Icons.RoundedFilled.NotInterested, null)
                Debanding.CPU -> Icon(Icons.RoundedFilled.Memory, null)
                Debanding.GPU -> Icon(Icons.RoundedFilled.DeveloperBoard, null)
              }
            }
          }

          Text(stringResource(deband.titleRes))

          Spacer(Modifier.weight(1f))
          TextButton(
            enabled = debandOptions.none(configOwnedOptions::contains),
            onClick = {
              decoderPreferences.debanding.set(Debanding.None)
              PlaybackSession.applyDebanding(Debanding.None)
              DebandSettings.entries.forEach {
                PlaybackSession.setPropertyInt(it.mpvProperty, it.preference(decoderPreferences).deleteAndGet())
              }
            },
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.RoundedFilled.ResetIso, null)
              Text(stringResource(R.string.generic_reset))
            }
          }
        }

        DebandSettings.entries.forEachIndexed { index, debandSettings ->
          val value by debandSettings.preference(decoderPreferences).collectAsState()
          PanelConnectedTile(index = index, itemCount = DebandSettings.entries.size) {
            SliderItem(
              label = stringResource(debandSettings.titleRes),
              value = value,
              valueText = value.toString(),
              onChange = {
                debandSettings.preference(decoderPreferences).set(it)
                PlaybackSession.setPropertyInt(debandSettings.mpvProperty, it)
              },
              min = debandSettings.start,
              max = debandSettings.end,
              enabled = debandSettings.mpvProperty !in configOwnedOptions,
            )
          }
        }
      }
    }
  }
}
