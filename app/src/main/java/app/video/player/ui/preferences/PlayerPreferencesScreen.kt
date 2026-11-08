/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.preferences

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import app.video.player.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import app.video.player.presentation.components.AppTextField as OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import app.video.player.presentation.components.AppDialogTextButton as TextButton
import app.video.player.presentation.components.AppTextField as TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.video.player.R
import app.video.player.preferences.AdvancedPreferences
import app.video.player.preferences.AudioPreferences
import app.video.player.preferences.BackgroundPlaybackBehavior
import app.video.player.preferences.IntroSegmentProvider
import app.video.player.preferences.PlayerPreferences
import app.video.player.preferences.preference.collectAsState
import app.video.player.presentation.Screen
import app.video.player.ui.player.NotificationStyle
import app.video.player.ui.player.PlaybackPhase
import app.video.player.ui.player.PlaybackSession
import app.video.player.ui.player.PlayerOrientation
import app.video.player.ui.player.ResumePlaybackMode
import app.video.player.ui.preferences.components.SwitchPreference
import app.video.player.ui.player.PlayerActivity
import app.video.player.ui.utils.LocalBackStack
import app.video.player.ui.utils.currentMpvConfigOverrideOptions
import app.video.player.ui.utils.popSafely
import kotlinx.serialization.Serializable
import app.video.player.ui.preferences.components.AppListPreference as ListPreference
import app.video.player.ui.preferences.components.AppPreference as Preference
import app.video.player.ui.preferences.components.AppSliderPreference as SliderPreference
import app.video.player.ui.preferences.components.AppTextFieldPreference as TextFieldPreference
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Serializable
object PlayerPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val context = LocalContext.current
    val resources = LocalResources.current
    val preferences = koinInject<PlayerPreferences>()
    val configOwnedOptions = currentMpvConfigOverrideOptions()
    val audioPreferences = koinInject<AudioPreferences>()
    val backgroundPlaybackBehavior by audioPreferences.backgroundPlaybackBehavior.collectAsState()
    val playbackState by PlaybackSession.state.collectAsState()
    val advancedPreferences = koinInject<AdvancedPreferences>()
    val notificationPermissionLauncher =
      rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
      ) { _ -> }
    Scaffold(
      contentWindowInsets = settingsContentWindowInsets(),
      topBar = {
        SettingsTopAppBar(
          title = stringResource(id = R.string.pref_player),
          onBack = { backstack.popSafely() },
        )
      },
    ) { padding ->
      ProvideExpressivePreferenceLocals {
        val (settingsListState, settingsHighlight) =
          rememberSettingsSearchList(PlayerPreferencesScreen, MaterialTheme.colorScheme.primary)
        LazyColumn(
          state = settingsListState,
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding)
              .then(settingsHighlight),
        ) {
          // ── General ───────────────────────────────────────────────────────
          item {
            PreferenceSectionHeader(position = PreferenceSectionPosition.First,
              title = stringResource(R.string.pref_section_general),
              modifier = Modifier.settingsSearchTarget(R.string.pref_player),
            )
          }
          item {
            PreferenceCard {
              DevicePreference(R.string.pref_player_orientation) {
                val orientation by preferences.orientation.collectAsState()
                ExpressivePreferenceItem {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_orientation),
                  value = orientation,
                  onValueChange = preferences.orientation::set,
                  values = PlayerOrientation.entries,
                  valueToText = { AnnotatedString(resources.getString(it.titleRes)) },
                  title = { Text(stringResource(R.string.pref_player_orientation)) },
                  summary = { Text(stringResource(orientation.titleRes), color = MaterialTheme.colorScheme.outline) },
                )
                }

                PreferenceDivider()
              }

              val resumePlaybackMode by preferences.resumePlaybackMode.collectAsState()
              ExpressivePreferenceItem {
              ListPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_resume_playback_title),
                value = resumePlaybackMode,
                onValueChange = preferences.resumePlaybackMode::set,
                values = ResumePlaybackMode.entries,
                valueToText = { AnnotatedString(resources.getString(it.titleRes)) },
                title = { Text(stringResource(R.string.pref_player_resume_playback_title)) },
                summary = {
                  Text(
                    stringResource(resumePlaybackMode.summaryRes),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
              }

              if (resumePlaybackMode == ResumePlaybackMode.MinimumDuration) {
                PreferenceDivider()
                val minimumDuration by preferences.minimumResumeDurationSeconds.collectAsState()
                ExpressivePreferenceItem {
                SliderPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_resume_min_duration),
                  value = minimumDuration.coerceIn(0, 3600).toFloat(),
                  onValueChange = { preferences.minimumResumeDurationSeconds.set(it.roundToInt().coerceIn(0, 3600)) },
                  title = { Text(stringResource(R.string.pref_player_resume_min_duration)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_player_resume_min_duration_summary, minimumDuration),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  valueRange = 0f..3600f,
                  valueSteps = 359,
                  onSliderValueChange = { preferences.minimumResumeDurationSeconds.set(it.roundToInt().coerceIn(0, 3600)) },
                  sliderValue = minimumDuration.coerceIn(0, 3600).toFloat(),
                )
                }
              }

PreferenceDivider()

val savePositionOnQuit by preferences.savePositionOnQuit.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_save_position_on_quit),
                value = savePositionOnQuit,
                onValueChange = preferences.savePositionOnQuit::set,
                title = { Text(stringResource(R.string.pref_player_save_position_on_quit)) },
              )

              PreferenceDivider()

              val closeAfterEndOfVideo by preferences.closeAfterReachingEndOfVideo.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_close_after_eof),
                value = closeAfterEndOfVideo,
                onValueChange = preferences.closeAfterReachingEndOfVideo::set,
                title = { Text(stringResource(R.string.pref_player_close_after_eof)) },
              )

              PreferenceDivider()

              val videoBackgroundPlayback by PlaybackSession.videoBackgroundPlaybackEnabled.collectAsState(
                initial = PlaybackSession.isVideoBackgroundPlaybackEnabled(),
              )
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_video_background_playback_title),
                value = videoBackgroundPlayback,
                enabled = backgroundPlaybackBehavior == BackgroundPlaybackBehavior.Remember ||
                  (PlaybackSession.videoBackgroundPlaybackId(playbackState) != null &&
                    playbackState.phase in setOf(PlaybackPhase.LOADING, PlaybackPhase.READY, PlaybackPhase.BACKGROUND)),
                onValueChange = { enabled ->
                  PlaybackSession.setVideoBackgroundPlaybackEnabled(enabled)
                  if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
                  ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                },
                title = { Text(stringResource(R.string.pref_video_background_playback_title)) },
                summary = {
                  Text(
                    stringResource(
                      if (backgroundPlaybackBehavior == BackgroundPlaybackBehavior.PerVideo) {
                        R.string.pref_background_playback_per_video_summary
                      } else {
                        R.string.pref_video_background_playback_summary
                      },
                    ),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              ExpressivePreferenceItem {
              ListPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_background_playback_behavior),
                value = backgroundPlaybackBehavior,
                onValueChange = audioPreferences.backgroundPlaybackBehavior::set,
                values = app.video.player.preferences.BackgroundPlaybackBehavior.entries,
                valueToText = { AnnotatedString(resources.getString(it.titleRes)) },
                title = { Text(stringResource(R.string.pref_background_playback_behavior)) },
                summary = { Text(stringResource(backgroundPlaybackBehavior.summaryRes), color = MaterialTheme.colorScheme.outline) },
              )
              }

              PreferenceDivider()

              DevicePreference(R.string.pref_player_external_display_projection_title) {
                val externalDisplayProjection by preferences.externalDisplayProjection.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_external_display_projection_title),
                  value = externalDisplayProjection,
                  onValueChange = { enabled ->
                    preferences.externalDisplayProjection.set(enabled)
                    (context as? PlayerActivity)?.setExternalDisplayProjectionEnabled(enabled)
                  },
                  title = { Text(stringResource(R.string.pref_player_external_display_projection_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_player_external_display_projection_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val notificationStyle by advancedPreferences.notificationStyle.collectAsState()
                val supportedNotificationStyles =
                  remember {
                    NotificationStyle.entries.filter { it.isSupportedOn(Build.VERSION.SDK_INT) }
                  }
                val selectedNotificationStyle =
                  notificationStyle.takeIf { it.isSupportedOn(Build.VERSION.SDK_INT) }
                    ?: NotificationStyle.Media
                ExpressivePreferenceItem {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_advanced_notification_style),
                  value = selectedNotificationStyle,
                  onValueChange = advancedPreferences.notificationStyle::set,
                  values = supportedNotificationStyles,
                  valueToText = { AnnotatedString(it.displayName) },
                  title = { Text(text = stringResource(R.string.pref_advanced_notification_style)) },
                  summary = {
                    Text(
                      text = selectedNotificationStyle.displayName,
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
                }

                PreferenceDivider()
              }

              val autoplayNextVideo by preferences.autoplayNextVideo.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_autoplay_next_video_title),
                value = autoplayNextVideo,
                onValueChange = preferences.autoplayNextVideo::set,
                title = { Text(stringResource(R.string.pref_autoplay_next_video_title)) },
                summary = {
                  Text(
                    if (autoplayNextVideo) {
                      stringResource(R.string.pref_autoplay_next_video_summary)
                    } else {
                      stringResource(R.string.pref_autoplay_next_video_summary_disabled)
                    },
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val playlistMode by preferences.playlistMode.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_playlist_mode_title),
                value = playlistMode,
                onValueChange = preferences.playlistMode::set,
                title = { Text(stringResource(R.string.pref_playlist_mode_title)) },
                summary = {
                  Text(
                    if (playlistMode) {
                      stringResource(R.string.pref_playlist_mode_summary)
                    } else {
                      stringResource(R.string.pref_playlist_mode_summary_disabled)
                    },
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              DevicePreference(R.string.pref_player_remember_brightness) {
                val rememberBrightness by preferences.rememberBrightness.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_remember_brightness),
                  value = rememberBrightness,
                  onValueChange = preferences.rememberBrightness::set,
                  title = { Text(stringResource(R.string.pref_player_remember_brightness)) },
                )

                PreferenceDivider()
              }

              val autoPiPOnNavigation by preferences.autoPiPOnNavigation.collectAsState()
              DevicePreference(R.string.pref_auto_pip_title) {
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_auto_pip_title),
                  value = autoPiPOnNavigation,
                  onValueChange = preferences.autoPiPOnNavigation::set,
                  title = { Text(stringResource(R.string.pref_auto_pip_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_auto_pip_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()
              }

              DevicePreference(R.string.pref_auto_pip_home_only_title) {
                val pipOnHomeGestureOnly by preferences.pipOnHomeGestureOnly.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_auto_pip_home_only_title),
                  value = pipOnHomeGestureOnly,
                  onValueChange = preferences.pipOnHomeGestureOnly::set,
                  enabled = autoPiPOnNavigation,
                  title = { Text(stringResource(R.string.pref_auto_pip_home_only_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_auto_pip_home_only_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()
              }

              DevicePreference(R.string.pref_enable_video_mini_player_title) {
                val enableVideoMiniPlayer by preferences.enableVideoMiniPlayer.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_enable_video_mini_player_title),
                  value = enableVideoMiniPlayer,
                  onValueChange = preferences.enableVideoMiniPlayer::set,
                  title = { Text(stringResource(R.string.pref_enable_video_mini_player_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_enable_video_mini_player_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()
              }

              val keepScreenOnWhenPaused by preferences.keepScreenOnWhenPaused.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_keep_screen_on_when_paused_title),
                value = keepScreenOnWhenPaused,
                onValueChange = preferences.keepScreenOnWhenPaused::set,
                title = { Text(stringResource(R.string.pref_player_keep_screen_on_when_paused_title)) },
                summary = {
                  Text(
                    if (keepScreenOnWhenPaused) {
                      stringResource(R.string.pref_player_keep_screen_on_when_paused_summary)
                    } else {
                      stringResource(R.string.pref_player_keep_screen_on_when_paused_summary_disabled)
                    },
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              DevicePreference(R.string.pref_player_autoplay_after_screen_unlock_title) {
                val autoplayAfterScreenUnlock by preferences.autoplayAfterScreenUnlock.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_autoplay_after_screen_unlock_title),
                  value = autoplayAfterScreenUnlock,
                  onValueChange = preferences.autoplayAfterScreenUnlock::set,
                  title = { Text(stringResource(R.string.pref_player_autoplay_after_screen_unlock_title)) },
                  summary = {
                    Text(
                      if (autoplayAfterScreenUnlock) {
                        stringResource(R.string.pref_player_autoplay_after_screen_unlock_summary)
                      } else {
                        stringResource(R.string.pref_player_autoplay_after_screen_unlock_summary_disabled)
                      },
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()
              }

              val enableMediaInfoIntent by preferences.enableMediaInfoIntent.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.ui_show_media_info_in_chooser),
                value = enableMediaInfoIntent,
                onValueChange = { enabled ->
                  preferences.enableMediaInfoIntent.set(enabled)
                  val componentName = ComponentName(context, "app.video.player.ui.mediainfo.MediaInfoActivityAlias")
                  val newState =
                    if (enabled) {
                      PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    } else {
                      PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    }
                  try {
                    context.packageManager.setComponentEnabledSetting(
                      componentName,
                      newState,
                      PackageManager.DONT_KILL_APP,
                    )
                  } catch (e: Exception) {
                    android.util.Log.e("PlayerPreferencesScreen", "Failed to set alias state", e)
                  }
                },
                title = {
                  Text(
                    androidx.compose.ui.res
                      .stringResource(app.video.player.R.string.ui_show_media_info_in_chooser),
                  )
                },
                summary = {
                  Text(
                    androidx.compose.ui.res
                      .stringResource(app.video.player.R.string.ui_show_media_info_in_system),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              if (app.video.player.BuildConfig.ENABLE_YTDLP) {
                PreferenceDivider()

                val enableWebStreamLinkIntents by preferences.enableWebStreamLinkIntents.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_web_stream_links_title),
                  value = enableWebStreamLinkIntents,
                  onValueChange = { enabled ->
                    preferences.enableWebStreamLinkIntents.set(enabled)
                    val componentName = ComponentName(context, "app.video.player.ui.player.WebStreamLinksActivityAlias")
                    val newState =
                      if (enabled) {
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                      } else {
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                      }
                    try {
                      context.packageManager.setComponentEnabledSetting(
                        componentName,
                        newState,
                        PackageManager.DONT_KILL_APP,
                      )
                    } catch (e: Exception) {
                      android.util.Log.e("PlayerPreferencesScreen", "Failed to set alias state", e)
                    }
                  },
                  title = { Text(stringResource(R.string.pref_player_web_stream_links_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_player_web_stream_links_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
              }
            }
          }

          // ── Seeking ───────────────────────────────────────────────────────
          item { PreferenceSectionHeader(position = PreferenceSectionPosition.Middle, title = stringResource(R.string.pref_player_seeking_title)) }
          item {
            PreferenceCard {
              DevicePreference(R.string.show_splash_ovals_on_double_tap_to_seek) {
                val showDoubleTapOvals by preferences.showDoubleTapOvals.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.show_splash_ovals_on_double_tap_to_seek),
                  value = showDoubleTapOvals,
                  onValueChange = preferences.showDoubleTapOvals::set,
                  title = { Text(stringResource(R.string.show_splash_ovals_on_double_tap_to_seek)) },
                )

                PreferenceDivider()

                val showSeekTimeWhileSeeking by preferences.showSeekTimeWhileSeeking.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.show_time_on_double_tap_to_seek),
                  value = showSeekTimeWhileSeeking,
                  onValueChange = preferences.showSeekTimeWhileSeeking::set,
                  title = { Text(stringResource(R.string.show_time_on_double_tap_to_seek)) },
                )

                PreferenceDivider()
              }

              val showBufferedRange by preferences.showBufferedRange.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_show_buffered_range_title),
                value = showBufferedRange,
                onValueChange = preferences.showBufferedRange::set,
                title = { Text(stringResource(R.string.pref_player_show_buffered_range_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_player_show_buffered_range_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val showChapterIndicators by preferences.showChapterIndicators.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_show_chapter_indicators_title),
                value = showChapterIndicators,
                onValueChange = preferences.showChapterIndicators::set,
                title = { Text(stringResource(R.string.pref_player_show_chapter_indicators_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_player_show_chapter_indicators_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val usePreciseSeeking by preferences.usePreciseSeeking.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_use_precise_seeking),
                value = usePreciseSeeking,
                enabled = setOf("hr-seek", "hr-seek-framedrop").none(configOwnedOptions::contains),
                onValueChange = preferences.usePreciseSeeking::set,
                title = { Text(stringResource(R.string.pref_player_use_precise_seeking)) },
              )

              PreferenceDivider()

              val customSkipDuration by preferences.customSkipDuration.collectAsState()
              ExpressivePreferenceItem {
              SliderPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_custom_skip_duration_title),
                value = customSkipDuration.toFloat(),
                onValueChange = { preferences.customSkipDuration.set(it.roundToInt()) },
                title = { Text(stringResource(R.string.pref_player_custom_skip_duration_title)) },
                valueRange = 5f..180f,
                summary = {
                  Text(
                    "${stringResource(R.string.pref_player_custom_skip_duration_summary)} ($customSkipDuration s)",
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                onSliderValueChange = { preferences.customSkipDuration.set(it.roundToInt()) },
                sliderValue = customSkipDuration.toFloat(),
              )
              }

              PreferenceDivider()

              val enableIntroDb by preferences.enableIntroDb.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_online_skip_markers_title),
                value = enableIntroDb,
                onValueChange = preferences.enableIntroDb::set,
                title = { Text(stringResource(R.string.pref_online_skip_markers_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_online_skip_markers_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              if (enableIntroDb) {
                PreferenceDivider()

                val introSegmentProvider by preferences.introSegmentProvider.collectAsState()
                ExpressivePreferenceItem {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_marker_provider_title),
                  value = introSegmentProvider,
                  onValueChange = preferences.introSegmentProvider::set,
                  values = IntroSegmentProvider.entries,
                  valueToText = { AnnotatedString(it.displayName) },
                  title = { Text(stringResource(R.string.pref_marker_provider_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_marker_provider_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
                }
              }

              PreferenceDivider()

              val detectFromChapters by preferences.detectIntroOutroFromChapters.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_chapter_detect_title),
                value = detectFromChapters,
                onValueChange = preferences.detectIntroOutroFromChapters::set,
                title = { Text(stringResource(R.string.pref_chapter_detect_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_chapter_detect_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              if (detectFromChapters) {
                PreferenceDivider()

                val customIntroKeywordsEnabled by preferences.customIntroKeywordsEnabled.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_custom_intro_keywords_enabled),
                  value = customIntroKeywordsEnabled,
                  onValueChange = preferences.customIntroKeywordsEnabled::set,
                  title = { Text(stringResource(R.string.pref_custom_intro_keywords_enabled)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_custom_intro_keywords_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                if (customIntroKeywordsEnabled) {
                  PreferenceDivider()

                  val customIntroKeywords by preferences.customIntroKeywords.collectAsState()
                  ExpressivePreferenceItem {
                  TextFieldPreference(
                    modifier = Modifier.settingsSearchTarget(R.string.pref_custom_intro_keywords_title),
                    value = customIntroKeywords,
                    onValueChange = preferences.customIntroKeywords::set,
                    textToValue = { it },
                    title = { Text(stringResource(R.string.pref_custom_intro_keywords_title)) },
                    summary = {
                      Text(
                        customIntroKeywords.ifBlank { "Not set" },
                        color = MaterialTheme.colorScheme.outline,
                      )
                    },
                    textField = { value, onValueChange, _ ->
                      Column {
                        Text(stringResource(R.string.pref_custom_intro_keywords_dialog_desc))
                        TextField(
                          value = value,
                          onValueChange = onValueChange,
                          modifier = Modifier.fillMaxWidth(),
                          placeholder = {
                            Text(
                              androidx.compose.ui.res
                                .stringResource(app.video.player.R.string.ui_e_g_intro_opening_op),
                            )
                          },
                          singleLine = true,
                        )
                      }
                    },
                  )
                  }
                }

                PreferenceDivider()

                val customOutroKeywordsEnabled by preferences.customOutroKeywordsEnabled.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_custom_outro_keywords_enabled),
                  value = customOutroKeywordsEnabled,
                  onValueChange = preferences.customOutroKeywordsEnabled::set,
                  title = { Text(stringResource(R.string.pref_custom_outro_keywords_enabled)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_custom_outro_keywords_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                if (customOutroKeywordsEnabled) {
                  PreferenceDivider()

                  val customOutroKeywords by preferences.customOutroKeywords.collectAsState()
                  ExpressivePreferenceItem {
                  TextFieldPreference(
                    modifier = Modifier.settingsSearchTarget(R.string.pref_custom_outro_keywords_title),
                    value = customOutroKeywords,
                    onValueChange = preferences.customOutroKeywords::set,
                    textToValue = { it },
                    title = { Text(stringResource(R.string.pref_custom_outro_keywords_title)) },
                    summary = {
                      Text(
                        customOutroKeywords.ifBlank { "Not set" },
                        color = MaterialTheme.colorScheme.outline,
                      )
                    },
                    textField = { value, onValueChange, _ ->
                      Column {
                        Text(stringResource(R.string.pref_custom_outro_keywords_dialog_desc))
                        TextField(
                          value = value,
                          onValueChange = onValueChange,
                          modifier = Modifier.fillMaxWidth(),
                          placeholder = {
                            Text(
                              androidx.compose.ui.res
                                .stringResource(app.video.player.R.string.ui_e_g_outro_ending_ed),
                            )
                          },
                          singleLine = true,
                        )
                      }
                    },
                  )
                  }
                }
              }

              PreferenceDivider()

              val autoSkipIntro by preferences.autoSkipIntro.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_auto_skip_intro_title),
                value = autoSkipIntro,
                onValueChange = preferences.autoSkipIntro::set,
                title = { Text(stringResource(R.string.pref_auto_skip_intro_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_auto_skip_intro_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val autoSkipOutro by preferences.autoSkipOutro.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_auto_skip_outro_title),
                value = autoSkipOutro,
                onValueChange = preferences.autoSkipOutro::set,
                title = { Text(stringResource(R.string.pref_auto_skip_outro_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_auto_skip_outro_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
            }
          }

          // ── Display & Controls ────────────────────────────────────────────
          item { PreferenceSectionHeader(position = PreferenceSectionPosition.Middle, title = stringResource(R.string.pref_section_display_controls)) }
          item {
            PreferenceCard {
              DevicePreference(R.string.pref_player_controls_drawer_title) {
                val showControlsDrawer by preferences.showControlsDrawer.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_controls_drawer_title),
                  value = showControlsDrawer,
                  onValueChange = preferences.showControlsDrawer::set,
                  title = { Text(stringResource(R.string.pref_player_controls_drawer_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_player_controls_drawer_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val showSystemStatusBar by preferences.showSystemStatusBar.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_display_show_status_bar),
                  value = showSystemStatusBar,
                  onValueChange = preferences.showSystemStatusBar::set,
                  title = { Text(stringResource(R.string.pref_player_display_show_status_bar)) },
                )

                PreferenceDivider()

                val showSystemNavigationBar by preferences.showSystemNavigationBar.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_nav_bar_title),
                  value = showSystemNavigationBar,
                  onValueChange = preferences.showSystemNavigationBar::set,
                  title = { Text(stringResource(R.string.pref_nav_bar_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_nav_bar_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val safeAreaWindow by preferences.safeAreaWindow.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_safe_area_window_title),
                  value = safeAreaWindow,
                  onValueChange = preferences.safeAreaWindow::set,
                  title = { Text(stringResource(R.string.pref_player_safe_area_window_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_player_safe_area_window_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()
              }

              val reduceMotion by preferences.reduceMotion.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_display_reduce_player_animation),
                value = reduceMotion,
                onValueChange = preferences.reduceMotion::set,
                title = { Text(stringResource(R.string.pref_player_display_reduce_player_animation)) },
              )

              PreferenceDivider()

              val showLoadingCircle by preferences.showLoadingCircle.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_player_controls_show_loading_circle),
                value = showLoadingCircle,
                onValueChange = preferences.showLoadingCircle::set,
                title = { Text(stringResource(R.string.pref_player_controls_show_loading_circle)) },
              )

              PreferenceDivider()

              DevicePreference(R.string.pref_player_controls_allow_gestures_in_panels) {
                val allowGesturesInPanels by preferences.allowGesturesInPanels.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_player_controls_allow_gestures_in_panels),
                  value = allowGesturesInPanels,
                  onValueChange = preferences.allowGesturesInPanels::set,
                  title = { Text(stringResource(R.string.pref_player_controls_allow_gestures_in_panels)) },
                )

                PreferenceDivider()

                val swapVolumeAndBrightness by preferences.swapVolumeAndBrightness.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.swap_the_volume_and_brightness_slider),
                  value = swapVolumeAndBrightness,
                  onValueChange = preferences.swapVolumeAndBrightness::set,
                  title = { Text(stringResource(R.string.swap_the_volume_and_brightness_slider)) },
                )
              }

            }
          }

          // ── Overlays ─────────────────────────────────────────────────────
          item { PreferenceSectionHeader(position = PreferenceSectionPosition.Last, title = stringResource(R.string.pref_section_overlays)) }
          item {
            PreferenceCard {
              val showVolumeGestureOverlay by preferences.showVolumeGestureOverlay.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_volume_overlay_title),
                value = showVolumeGestureOverlay,
                onValueChange = preferences.showVolumeGestureOverlay::set,
                title = { Text(stringResource(R.string.pref_volume_overlay_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_volume_overlay_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              DevicePreference(R.string.pref_brightness_overlay_title) {
                PreferenceDivider()

                val showBrightnessGestureOverlay by preferences.showBrightnessGestureOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_brightness_overlay_title),
                  value = showBrightnessGestureOverlay,
                  onValueChange = preferences.showBrightnessGestureOverlay::set,
                  title = { Text(stringResource(R.string.pref_brightness_overlay_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_brightness_overlay_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val showHoldSpeedOverlay by preferences.showHoldSpeedOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_hold_speed_overlay_pref_title),
                  value = showHoldSpeedOverlay,
                  onValueChange = preferences.showHoldSpeedOverlay::set,
                  title = { Text(stringResource(R.string.pref_hold_speed_overlay_pref_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_hold_speed_overlay_pref_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val showAspectRatioOverlay by preferences.showAspectRatioOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_aspect_ratio_overlay_title),
                  value = showAspectRatioOverlay,
                  onValueChange = preferences.showAspectRatioOverlay::set,
                  title = { Text(stringResource(R.string.pref_aspect_ratio_overlay_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_aspect_ratio_overlay_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val showZoomLevelOverlay by preferences.showZoomLevelOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_zoom_overlay_title),
                  value = showZoomLevelOverlay,
                  onValueChange = preferences.showZoomLevelOverlay::set,
                  title = { Text(stringResource(R.string.pref_zoom_overlay_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_zoom_overlay_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val showRepeatShuffleOverlay by preferences.showRepeatShuffleOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_repeat_shuffle_overlay_title),
                  value = showRepeatShuffleOverlay,
                  onValueChange = preferences.showRepeatShuffleOverlay::set,
                  title = { Text(stringResource(R.string.pref_repeat_shuffle_overlay_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_repeat_shuffle_overlay_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

                val showActionFeedbackOverlay by preferences.showActionFeedbackOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_action_feedback_overlay_title),
                  value = showActionFeedbackOverlay,
                  onValueChange = preferences.showActionFeedbackOverlay::set,
                  title = { Text(stringResource(R.string.pref_action_feedback_overlay_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_action_feedback_overlay_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )

                PreferenceDivider()

  val showResumeIndicatorOverlay by preferences.showResumeIndicatorOverlay.collectAsState()
  SwitchPreference(
    modifier = Modifier.settingsSearchTarget(R.string.pref_resume_indicator_overlay_title),
    value = showResumeIndicatorOverlay,
    onValueChange = preferences.showResumeIndicatorOverlay::set,
    title = { Text(stringResource(R.string.pref_resume_indicator_overlay_title)) },
    summary = {
      Text(
        stringResource(R.string.pref_resume_indicator_overlay_summary),
        color = MaterialTheme.colorScheme.outline,
      )
    },
  )

  PreferenceDivider()

  val showProviderStatusOverlay by preferences.showProviderStatusOverlay.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_provider_status_overlay_title),
                  value = showProviderStatusOverlay,
                  onValueChange = preferences.showProviderStatusOverlay::set,
                  title = { Text(stringResource(R.string.pref_provider_status_overlay_title)) },
                  summary = {
                    Text(
                      stringResource(R.string.pref_provider_status_overlay_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
              }

            }
          }
        }
      }
    }
  }
}
