/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import app.video.player.presentation.components.AppTextField as TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.video.player.R
import app.video.player.preferences.AudioChannels
import app.video.player.preferences.AudioPreferences
import app.video.player.preferences.preference.collectAsState
import app.video.player.presentation.Screen
import app.video.player.ui.preferences.components.SwitchPreference
import app.video.player.ui.utils.LocalBackStack
import app.video.player.ui.utils.currentMpvConfigOverrideOptions
import app.video.player.ui.utils.popSafely
import kotlinx.serialization.Serializable
import app.video.player.ui.preferences.components.AppListPreference as ListPreference
import app.video.player.ui.preferences.components.AppSliderPreference as SliderPreference
import app.video.player.ui.preferences.components.AppTextFieldPreference as TextFieldPreference
import org.koin.compose.koinInject

@Serializable
object AudioPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val resources = LocalResources.current
    val backstack = LocalBackStack.current
    val playerPanel = LocalPlayerSettingsPanel.current
    val preferences = koinInject<AudioPreferences>()
    val configOwnedOptions = currentMpvConfigOverrideOptions()

    Scaffold(
      contentWindowInsets = settingsContentWindowInsets(),
      topBar = {
        SettingsTopAppBar(
          title = stringResource(R.string.pref_audio),
          onBack = { backstack.popSafely() },
        )
      },
    ) { padding ->
      ProvideExpressivePreferenceLocals {
        val (settingsListState, settingsHighlight) =
          rememberSettingsSearchList(AudioPreferencesScreen, MaterialTheme.colorScheme.primary)
        LazyColumn(
          state = settingsListState,
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding)
              .then(settingsHighlight),
        ) {
          if (!playerPanel) {
            item {
              PreferenceSectionHeader(
                position = PreferenceSectionPosition.Single,
                title = stringResource(R.string.pref_audio),
                modifier = Modifier.settingsSearchTarget(R.string.pref_audio),
              )
            }
          }

          item {
            PreferenceCard {
              val preferredLanguages by preferences.preferredLanguages.collectAsState()
              ExpressivePreferenceItem {
                TextFieldPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_preferred_languages),
                  value = preferredLanguages,
                  enabled = "alang" !in configOwnedOptions,
                  onValueChange = { preferences.preferredLanguages.set(it) },
                  textToValue = { input ->
                    input
                      .split(",")
                      .map { it.trim().lowercase() }
                      .filter { it.isNotEmpty() }
                      .joinToString(",")
                  },
                  title = { Text(stringResource(R.string.pref_preferred_languages)) },
                  summary = {
                    if (preferredLanguages.isNotBlank()) {
                      Text(
                        preferredLanguages,
                        color = MaterialTheme.colorScheme.outline,
                      )
                    } else {
                      Text(
                        stringResource(R.string.not_set_video_default),
                        color = MaterialTheme.colorScheme.outline,
                      )
                    }
                  },
                  textField = { value, onValueChange, _ ->
                    Column {
                      Text(stringResource(R.string.pref_audio_preferred_language))
                      TextField(
                        value,
                        onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                      )
                    }
                  },
                )
              }

              PreferenceDivider()
              val audioPitchCorrection by preferences.audioPitchCorrection.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_audio_pitch_correction_title),
                value = audioPitchCorrection,
                enabled = "audio-pitch-correction" !in configOwnedOptions,
                onValueChange = { preferences.audioPitchCorrection.set(it) },
                title = { Text(stringResource(R.string.pref_audio_pitch_correction_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_audio_pitch_correction_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()
              val volumeNormalization by preferences.volumeNormalization.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_audio_volume_normalization_title),
                value = volumeNormalization,
                enabled = "af" !in configOwnedOptions,
                onValueChange = { preferences.volumeNormalization.set(it) },
                title = { Text(stringResource(R.string.pref_audio_volume_normalization_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_audio_volume_normalization_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()
              val spatialAudio by preferences.spatialAudio.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_audio_spatial_title),
                value = spatialAudio,
                enabled = "af" !in configOwnedOptions,
                onValueChange = { preferences.spatialAudio.set(it) },
                title = { Text(stringResource(R.string.pref_audio_spatial_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_audio_spatial_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()
              val drcEnabled by preferences.drcEnabled.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_audio_drc_title),
                value = drcEnabled,
                enabled = "af" !in configOwnedOptions,
                onValueChange = { preferences.drcEnabled.set(it) },
                title = { Text(stringResource(R.string.pref_audio_drc_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_audio_drc_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()
              val ignoreAudioFocus by preferences.ignoreAudioFocus.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_ignore_audio_focus_title),
                value = ignoreAudioFocus,
                onValueChange = preferences.ignoreAudioFocus::set,
                title = { Text(stringResource(R.string.pref_ignore_audio_focus_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_ignore_audio_focus_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()
              val audioChannel by preferences.audioChannels.collectAsState()
              ExpressivePreferenceItem {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_audio_channels),
                  value = audioChannel,
                  onValueChange = { preferences.audioChannels.set(it) },
                  values = AudioChannels.entries,
                  enabled = setOf("audio-channels", "af").none(configOwnedOptions::contains),
                  valueToText = { AnnotatedString(resources.getString(it.title)) },
                  title = { Text(text = stringResource(id = R.string.pref_audio_channels)) },
                  summary = {
                    Text(
                      text = stringResource(audioChannel.title),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
              }

              PreferenceDivider()
              val volumeBoostCap by preferences.volumeBoostCap.collectAsState()
              ExpressivePreferenceItem {
                SliderPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_audio_volume_boost_cap),
                  value = volumeBoostCap.toFloat(),
                  enabled = "volume-max" !in configOwnedOptions,
                  onValueChange = { preferences.volumeBoostCap.set(it.toInt()) },
                  title = { Text(stringResource(R.string.pref_audio_volume_boost_cap)) },
                  valueRange = 0f..200f,
                  summary = {
                    Text(
                      if (volumeBoostCap == 0) {
                        stringResource(R.string.generic_disabled)
                      } else {
                        volumeBoostCap.toString()
                      },
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  onSliderValueChange = { preferences.volumeBoostCap.set(it.toInt()) },
                  sliderValue = volumeBoostCap.toFloat(),
                )
              }
            }
          }
        }
      }
    }
  }
}
