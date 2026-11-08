/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import app.video.player.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import app.video.player.presentation.components.AppDialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.video.player.BuildConfig
import app.video.player.R
import app.video.player.preferences.AdvancedPreferences
import app.video.player.preferences.DecoderPreferences
import app.video.player.preferences.MpvConfigOverride
import app.video.player.preferences.MpvConfigControlledFeatures
import app.video.player.preferences.preference.collectAsState
import app.video.player.presentation.Screen
import app.video.player.ui.icons.Icon
import app.video.player.ui.icons.Icons
import app.video.player.domain.motion.MotionInterpolation
import app.video.player.ui.player.Debanding
import app.video.player.ui.player.HwPlusGuard
import app.video.player.ui.player.MPVProfile
import app.video.player.ui.player.PlaybackSession
import app.video.player.ui.player.RendererBackendPolicy
import app.video.player.ui.preferences.components.SwitchPreference
import app.video.player.ui.utils.LocalBackStack
import app.video.player.ui.utils.popSafely
import app.video.player.utils.device.VulkanCapabilities
import kotlinx.serialization.Serializable
import app.video.player.ui.preferences.components.AppListPreference as ListPreference
import org.koin.compose.koinInject

@Serializable
object DecoderPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val preferences = koinInject<DecoderPreferences>()
    val embeddedInPlayer = LocalPlayerSettingsPanel.current
    val advancedPreferences = koinInject<AdvancedPreferences>()
    val storedConfigOverrides by advancedPreferences.mpvConfOverrides.collectAsState()
    val configOwnedOptions =
      remember(storedConfigOverrides) { MpvConfigOverride.resolveOptionNames(storedConfigOverrides) }
    val profileConfigOwned = "profile" in configOwnedOptions
    val rendererBackendConfigOwned = setOf("gpu-api", "gpu-context").any(configOwnedOptions::contains)
    val decoderConfigOwned = MpvConfigControlledFeatures.HARDWARE_DECODER.any(configOwnedOptions::contains)
    val debandingConfigOwned =
      setOf("vf", "deband", "deband-iterations", "deband-threshold", "deband-range", "deband-grain")
        .any(configOwnedOptions::contains)
    val yuv420ConfigOwned = "vf" in configOwnedOptions
    val backstack = LocalBackStack.current
    val context = LocalContext.current
    val isDeviceVulkanSupported = remember { VulkanCapabilities.isDeviceSupported(context) }
    val isVulkanSupported = BuildConfig.MPV_SUPPORTS_VULKAN && isDeviceVulkanSupported
    var showGpuNextWarning by remember { mutableStateOf(false) }
    Scaffold(
      contentWindowInsets = settingsContentWindowInsets(),
      topBar = {
        SettingsTopAppBar(
          title = stringResource(R.string.pref_decoder),
          onBack = { backstack.popSafely() },
        )
      },
    ) { padding ->
      ProvideExpressivePreferenceLocals {
        val (settingsListState, settingsHighlight) =
          rememberSettingsSearchList(DecoderPreferencesScreen, MaterialTheme.colorScheme.primary)
        LazyColumn(
          state = settingsListState,
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding)
              .then(settingsHighlight),
        ) {
          if (!embeddedInPlayer) item {
            PreferenceSectionHeader(position = PreferenceSectionPosition.Single, 
              title = stringResource(R.string.pref_decoder),
              modifier = Modifier.settingsSearchTarget(R.string.pref_decoder),
            )
          }

          item {
            PreferenceCard {
              val batterySaver by preferences.batterySaver.collectAsState()
              val autoBatterySaver by preferences.autoBatterySaver.collectAsState()

              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_battery_saver_title),
                value = batterySaver,
                onValueChange = { preferences.batterySaver.set(it) },
                title = { Text(stringResource(R.string.pref_battery_saver_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_battery_saver_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_auto_battery_saver_title),
                value = autoBatterySaver,
                onValueChange = { preferences.autoBatterySaver.set(it) },
                title = { Text(stringResource(R.string.pref_auto_battery_saver_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_auto_battery_saver_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
            }
          }

          item {
            PreferenceCard {
              val profile by preferences.profile.collectAsState()
              val currentProfile = MPVProfile.fromValue(profile)
              ExpressivePreferenceItem {
              ListPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_decoder_profile_title),
                value = currentProfile,
                onValueChange = { preferences.profile.set(it.value) },
                values = MPVProfile.entries,
                enabled = !profileConfigOwned,
                title = { Text(stringResource(R.string.pref_decoder_profile_title)) },
                summary = {
                  Text(
                    currentProfile.displayName,
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
              }

              PreferenceDivider()

              val tryHWDecoding by preferences.tryHWDecoding.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_decoder_try_hw_dec_title),
                value = tryHWDecoding,
                enabled = !decoderConfigOwned,
                onValueChange = {
                  preferences.tryHWDecoding.set(it)
                  if (embeddedInPlayer) {
                    val usesVulkan =
                      PlaybackSession.getPropertyString("gpu-api") == "vulkan" || preferences.useVulkan.get()
                    PlaybackSession.setPropertyString(
                      "hwdec",
                      RendererBackendPolicy.preferredHwdecMode(
                        hardwareDecodingEnabled = it,
                        usesVulkan = usesVulkan,
                        buildSupportsMediaCodecVulkan = BuildConfig.MPV_SUPPORTS_MEDIACODEC_VULKAN,
                      ),
                    )
                  }
                },
                title = { Text(stringResource(R.string.pref_decoder_try_hw_dec_title)) },
              )

              PreferenceDivider()

              val gpuNext by preferences.gpuNext.collectAsState()
              val useVulkan by preferences.useVulkan.collectAsState() // Added to check Vulkan state
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_decoder_gpu_next_title),
                value = gpuNext,
                onValueChange = { enabled ->
                  if (enabled && !gpuNext && !useVulkan) { // Only show warning if Vulkan is disabled
                    showGpuNextWarning = true
                  } else {
                    preferences.gpuNext.set(enabled)
                  }
                },
                title = { Text(stringResource(R.string.pref_decoder_gpu_next_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_decoder_gpu_next_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              if (showGpuNextWarning) {
                AlertDialog(
                  onDismissRequest = { showGpuNextWarning = false },
                  title = { Text(stringResource(R.string.pref_decoder_gpu_next_enable_title)) },
                  text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      Text(stringResource(R.string.pref_decoder_gpu_next_warning))
                      Text(stringResource(R.string.pref_decoder_gpu_next_purple_screen_fix))
                    }
                  },
                  confirmButton = {
                    Button(onClick = {
                      preferences.gpuNext.set(true)
                      showGpuNextWarning = false
                    }) {
                      Text(stringResource(R.string.pref_decoder_gpu_next_enable_anyway))
                    }
                  },
                  dismissButton = {
                    TextButton(onClick = { showGpuNextWarning = false }) {
                      Text(stringResource(R.string.generic_cancel))
                    }
                  },
                )
              }

              PreferenceDivider()

              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_decoder_vulkan_experimental_title),
                value = useVulkan && isVulkanSupported,
                onValueChange = { enabled ->
                  if (isVulkanSupported) {
                    preferences.useVulkan.set(enabled)
                  }
                },
                enabled = isVulkanSupported && !rendererBackendConfigOwned,
                title = { Text(stringResource(R.string.pref_decoder_vulkan_experimental_title)) },
                summary = {
                  Text(
                    stringResource(
                      when {
                        !BuildConfig.MPV_SUPPORTS_VULKAN && isDeviceVulkanSupported ->
                          R.string.pref_decoder_vulkan_excluded_supported_device
                        !BuildConfig.MPV_SUPPORTS_VULKAN ->
                          R.string.pref_decoder_vulkan_excluded_unsupported_device
                        isDeviceVulkanSupported -> R.string.pref_decoder_vulkan_summary
                        else -> R.string.pref_decoder_vulkan_not_supported
                      },
                    ),
                    color =
                      if (isVulkanSupported) {
                        MaterialTheme.colorScheme.outline
                      } else {
                        MaterialTheme.colorScheme.error
                      },
                  )
                },
              )

              PreferenceDivider()

              val debanding by preferences.debanding.collectAsState()
              ExpressivePreferenceItem {
              ListPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_decoder_debanding_title),
                value = debanding,
                onValueChange = {
                  val select: () -> Unit = {
                    preferences.debanding.set(it)
                    if (embeddedInPlayer) PlaybackSession.applyDebanding(it)
                  }
                  val blocked =
                    embeddedInPlayer && it == Debanding.CPU &&
                      HwPlusGuard.blocks(context, R.string.player_hwplus_cpu_deband, select)
                  if (!blocked) select()
                },
                values = Debanding.entries,
                enabled = !debandingConfigOwned,
                title = { Text(stringResource(R.string.pref_decoder_debanding_title)) },
                summary = {
                  Text(
                    debanding.name,
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
              }

              PreferenceDivider()

              val useYUV420p by preferences.useYUV420P.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_decoder_yuv420p_title),
                value = useYUV420p,
                enabled = !yuv420ConfigOwned,
                onValueChange = {
                  preferences.useYUV420P.set(it)
                },
                title = { Text(stringResource(R.string.pref_decoder_yuv420p_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_decoder_yuv420p_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
            }
          }

          if (MotionInterpolation.isBuildSupported) {
            if (!embeddedInPlayer) item {
              PreferenceSectionHeader(
                position = PreferenceSectionPosition.Single,
                title = stringResource(R.string.motion_title),
              )
            }

            item {
              MotionInterpolationPreferences(
                preferences = preferences,
                rendererConfigOwned = rendererBackendConfigOwned,
              )
            }
          }
        }
      }
    }
  }
}
