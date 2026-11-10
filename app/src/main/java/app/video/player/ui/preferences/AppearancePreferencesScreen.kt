/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.preferences

import android.app.Activity
import app.video.player.ui.components.AppToast as Toast
import app.video.player.ui.components.AppToastType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.video.player.R
import app.video.player.domain.thumbnail.ThumbnailRepository
import app.video.player.preferences.AppearancePreferences
import app.video.player.preferences.BrowserPreferences
import app.video.player.preferences.GesturePreferences
import app.video.player.preferences.MultiChoiceSegmentedButton
import app.video.player.preferences.PlayerPreferences
import app.video.player.preferences.ThumbnailGenerator
import app.video.player.preferences.ThumbnailMode
import app.video.player.preferences.ThumbnailQuality
import app.video.player.preferences.TreeFlattenDepth
import app.video.player.preferences.preference.collectAsState
import app.video.player.presentation.Screen
import app.video.player.presentation.components.ConfirmDialog
import app.video.player.ui.components.AppSlider
import app.video.player.ui.icons.Icon
import app.video.player.ui.icons.Icons
import app.video.player.ui.preferences.components.SwitchPreference
import app.video.player.ui.preferences.components.RestartRequiredDialog
import app.video.player.ui.preferences.components.ThemePicker
import app.video.player.ui.preferences.components.ThumbnailParallelismDialog
import app.video.player.ui.preferences.components.thumbnailParallelismLabel
import app.video.player.domain.thumbnail.maxThumbnailParallelism
import app.video.player.ui.theme.DarkMode
import app.video.player.ui.theme.CustomThemeDefinition
import app.video.player.ui.theme.LocalThemeTransitionState
import app.video.player.ui.utils.LocalBackStack
import app.video.player.ui.utils.navigateTo
import app.video.player.ui.utils.popSafely
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import app.video.player.ui.preferences.components.AppListPreference as ListPreference
import app.video.player.ui.preferences.components.PreferenceAction
import app.video.player.ui.preferences.components.AppPreference as Preference
import app.video.player.ui.preferences.components.AppSliderPreference as SliderPreference
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Serializable
object AppearancePreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val preferences = koinInject<AppearancePreferences>()
    val browserPreferences = koinInject<BrowserPreferences>()
    val gesturePreferences = koinInject<GesturePreferences>()
    val playerPreferences = koinInject<PlayerPreferences>()
    val thumbnailRepository = koinInject<ThumbnailRepository>()
    val television = app.video.player.ui.tv.isTelevision()
    val backstack = LocalBackStack.current
    val scope = rememberCoroutineScope()
    val systemDarkTheme = isSystemInDarkTheme()
    val themeTransition = LocalThemeTransitionState.current

    val darkMode by preferences.darkMode.collectAsState()
    val appTheme by preferences.appTheme.collectAsState()
    val customTheme by preferences.customTheme.collectAsState()
    val selectedCustomThemeName by preferences.selectedCustomThemeName.collectAsState()
    var changingThumbnailGenerator by remember { mutableStateOf(false) }
    var pendingThumbnailMode by remember { mutableStateOf<ThumbnailMode?>(null) }
    var pendingAppUiScale by remember { mutableStateOf<Float?>(null) }
    var isThemeSectionExpanded by rememberSaveable { mutableStateOf(true) }
    val searchTarget by SettingsSearchNavigation.target.collectAsState()
    LaunchedEffect(searchTarget) {
      if (searchTarget?.screen == AppearancePreferencesScreen) isThemeSectionExpanded = true
    }
    var isAppFontSheetOpen by rememberSaveable { mutableStateOf(false) }
    val storedThumbnailMode by browserPreferences.thumbnailMode.collectAsState()
    val thumbnailGenerator by browserPreferences.thumbnailGenerator.collectAsState()
    val thumbnailQuality by browserPreferences.thumbnailQuality.collectAsState()
    val thumbnailFramePosition by browserPreferences.thumbnailFramePosition.collectAsState()
    val thumbnailParallelism by browserPreferences.thumbnailParallelism.collectAsState()
    val automaticThumbnailParallelism = remember { thumbnailRepository.automaticParallelism }
    val maximumThumbnailParallelism = remember { maxThumbnailParallelism() }
    var isThumbnailParallelismDialogOpen by rememberSaveable { mutableStateOf(false) }
    val dualPaneForTablet by browserPreferences.dualPaneForTablet.collectAsState()
    val treeFlattenDepth by browserPreferences.treeFlattenDepth.collectAsState()
    val thumbnailCacheClearedMessage = stringResource(R.string.pref_thumbnail_cache_cleared)

    val thumbnailMode = storedThumbnailMode
    val customThemes = remember(customTheme) { CustomThemeDefinition.parseCollection(customTheme) }
    val selectedCustomTheme = customThemes.firstOrNull { it.name == selectedCustomThemeName }
    val selectedThemeLabel = selectedCustomTheme?.name ?: stringResource(appTheme.titleRes)

    // Determine if we're in dark mode for theme preview
    val isDarkMode =
      when (darkMode) {
        DarkMode.Dark -> true
        DarkMode.Light -> false
        DarkMode.System -> systemDarkTheme
      }

    if (pendingThumbnailMode != null) {
      ConfirmDialog(
        title = stringResource(R.string.pref_appearance_thumbnail_generation_change_title),
        subtitle = stringResource(R.string.pref_appearance_thumbnail_generation_change_summary),
        onConfirm = {
          val selectedMode = pendingThumbnailMode
          pendingThumbnailMode = null
          if (selectedMode != null) {
            browserPreferences.thumbnailMode.set(selectedMode)
            scope.launch {
              val result =
                withContext(Dispatchers.IO) {
                  runCatching { thumbnailRepository.clearThumbnailCache() }
                }
              result
                .onSuccess {
                  Toast
                    .makeText(
                      context,
                      thumbnailCacheClearedMessage,
                      Toast.LENGTH_SHORT,
                    ).show()
                }.onFailure { error ->
                  Toast
                    .makeText(
                      context,
                      context.resources.getString(
                        R.string.pref_thumbnail_cache_clear_failed,
                        error.message ?: "Unknown error",
                      ),
                      Toast.LENGTH_LONG,
                    ).show()
                }
            }
          }
        },
        onCancel = { pendingThumbnailMode = null },
      )
    }

    pendingAppUiScale?.let { scale ->
      RestartRequiredDialog(
        title = stringResource(R.string.pref_appearance_ui_scale_restart_title),
        message =
          stringResource(
            R.string.pref_appearance_ui_scale_restart_message,
            (scale * 100).roundToInt(),
          ),
        confirmLabel = stringResource(R.string.pref_appearance_ui_scale_restart_now),
        onDismiss = { pendingAppUiScale = null },
        onConfirm = {
          pendingAppUiScale = null
          preferences.appUiScale.set(scale)
          (context as? Activity)?.recreate()
        },
      )
    }

    if (isAppFontSheetOpen) {
      GoogleFontsSheet(
        preferences = preferences,
        onDismiss = { isAppFontSheetOpen = false },
      )
    }

    if (isThumbnailParallelismDialogOpen) {
      ThumbnailParallelismDialog(
        initialValue = thumbnailParallelism,
        automatic = automaticThumbnailParallelism,
        maximum = maximumThumbnailParallelism,
        onConfirm = { selected ->
          browserPreferences.thumbnailParallelism.set(selected)
          isThumbnailParallelismDialogOpen = false
        },
        onDismiss = { isThumbnailParallelismDialogOpen = false },
      )
    }

    Scaffold(
      topBar = {
        SettingsTopAppBar(
          title = stringResource(R.string.pref_appearance_title),
          onBack = { backstack.popSafely() },
        )
      },
    ) { padding ->
      ProvideExpressivePreferenceLocals {
        val (settingsListState, settingsHighlight) =
          rememberSettingsSearchList(AppearancePreferencesScreen, MaterialTheme.colorScheme.primary)
        LazyColumn(
          state = settingsListState,
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding)
              .then(settingsHighlight),
        ) {
          item {
            PreferenceSectionHeader(position = PreferenceSectionPosition.First,
              title = stringResource(id = R.string.pref_appearance_category_theme),
              modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_title),
            )
          }

          item {
            // Leave previews off a painted panel; only the actual preference controls
            // belong to the per-item connected background group.
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .settingsSearchTarget(R.string.pref_appearance_category_theme)
                    .clickable { isThemeSectionExpanded = !isThemeSectionExpanded }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  Text(
                    text = stringResource(id = R.string.pref_appearance_category_theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text(
                    text = "${stringResource(darkMode.titleRes)} · $selectedThemeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                  )
                }
                Icon(
                  imageVector = if (isThemeSectionExpanded) Icons.RoundedFilled.ExpandLess else Icons.RoundedFilled.ExpandMore,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              AnimatedVisibility(visible = isThemeSectionExpanded) {
                Column {
                  PreferenceDivider()

                  Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    MultiChoiceSegmentedButton(
                      choices =
                        DarkMode.entries
                          .map {
                            stringResource(
                              it.titleRes,
                            )
                          }.toImmutableList(),
                      selectedIndices = persistentListOf(DarkMode.entries.indexOf(darkMode)),
                      onClick = { index, position ->
                        if (themeTransition?.isAnimating != true) {
                          themeTransition?.startTransition(position)
                          scope.launch {
                            delay(50)
                            preferences.darkMode.set(DarkMode.entries[index])
                          }
                        }
                      },
                    )
                  }

                  PreferenceDivider()

                  val amoledMode by preferences.amoledMode.collectAsState()
                  ThemePicker(
                    currentTheme = appTheme,
                    customThemes = customThemes,
                    selectedCustomThemeName = selectedCustomThemeName,
                    isDarkMode = isDarkMode,
                    onThemeSelected = { theme, position ->
                      if ((theme != appTheme || selectedCustomThemeName.isNotBlank()) && themeTransition?.isAnimating != true) {
                        themeTransition?.startTransition(position)
                        scope.launch {
                          delay(50)
                          preferences.appTheme.set(theme)
                          preferences.selectedCustomThemeName.set("")
                        }
                      }
                    },
                    onAddCustomTheme = { backstack.navigateTo(CustomThemeEditorScreen()) },
                    onCustomThemeSelected = { theme, position ->
                      if (theme.name != selectedCustomThemeName && themeTransition?.isAnimating != true) {
                        themeTransition?.startTransition(position)
                        scope.launch {
                          delay(50)
                          preferences.selectedCustomThemeName.set(theme.name)
                        }
                      }
                    },
                    onEditCustomTheme = { name -> backstack.navigateTo(CustomThemeEditorScreen(name)) },
                    onDeleteCustomTheme = { name ->
                      val remainingThemes = customThemes.filterNot { it.name == name }
                      preferences.customTheme.set(CustomThemeDefinition.serializeCollection(remainingThemes))
                      if (selectedCustomThemeName == name) preferences.selectedCustomThemeName.set("")
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                  )

                  PreferenceCard {
                   DevicePreference(R.string.pref_app_icon_title) {
                     ExpressivePreferenceItem {
                     Preference(
                       modifier = Modifier.settingsSearchTarget(R.string.pref_app_icon_title),
                       title = { Text(stringResource(R.string.pref_app_icon_title)) },
                       summary = {
                         Text(
                           stringResource(R.string.pref_app_icon_summary),
                           color = MaterialTheme.colorScheme.outline,
                         )
                       },
                       onClick = { backstack.navigateTo(AppIconPreferencesScreen) },
                     )
                     }

                    PreferenceDivider()
                   }

                  SwitchPreference(
                    modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_amoled_mode_title),
                    value = amoledMode,
                    onValueChange = { newValue ->
                      if (themeTransition?.isAnimating != true) {
                        themeTransition?.startTransition(
                          androidx.compose.ui.geometry.Offset.Zero,
                        )
                        scope.launch {
                          delay(50)
                          preferences.amoledMode.set(newValue)
                        }
                      }
                    },
                    title = {
                      Text(
                        text = stringResource(id = R.string.pref_appearance_amoled_mode_title),
                      )
                    },
                    summary = {
                      Text(
                        text =
                          stringResource(
                            id = R.string.pref_appearance_amoled_mode_summary,
                          ),
                        color = MaterialTheme.colorScheme.outline,
                      )
                    },
                    enabled = darkMode != DarkMode.Light,
                  )

                  PreferenceDivider()

                  val useSystemFont by preferences.useSystemFont.collectAsState()
                  val googleFontFamily by preferences.googleFontFamily.collectAsState()
                  val activeFontName =
                    when {
                      useSystemFont -> stringResource(R.string.pref_appearance_system_font_title)
                      googleFontFamily.isNotBlank() -> googleFontFamily
                      else -> stringResource(R.string.app_font_google_sans_flex)
                    }
                  ExpressivePreferenceItem {
                  Preference(
                    modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_app_font_title),
                    title = { Text(stringResource(R.string.pref_appearance_app_font_title)) },
                    summary = {
                      Text(
                        text = stringResource(R.string.pref_appearance_app_font_summary, activeFontName),
                        color = MaterialTheme.colorScheme.outline,
                      )
                    },
                    onClick = { isAppFontSheetOpen = true },
                    action = PreferenceAction.Dialog,
                  )
                  }

                  PreferenceDivider()

                  val appFontScale by preferences.appFontScale.collectAsState()
                  ExpressivePreferenceItem {
                    Preference(
                      modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_font_size_title),
                      title = { Text(stringResource(R.string.pref_appearance_font_size_title)) },
                      summary = {
                        Column {
                          Text(
                            text =
                              if (appFontScale == 1.0f) {
                                stringResource(
                                  R.string.pref_appearance_font_size_default,
                                  (appFontScale * 100).roundToInt(),
                                )
                              } else {
                                stringResource(
                                  R.string.pref_appearance_font_size_summary,
                                  (appFontScale * 100).roundToInt(),
                                ),
                              },
                            color = MaterialTheme.colorScheme.outline,
                          )
                          Row(
                            modifier =
                              Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                          ) {
                            IconButton(
                              onClick = {
                                val newScale = ((appFontScale - 0.05f) * 100).roundToInt() / 100f
                                preferences.appFontScale.set(newScale.coerceIn(0.75f, 1.40f))
                              },
                              enabled = appFontScale > 0.75f,
                            ) {
                              Icon(
                                Icons.RoundedFilled.Remove,
                                contentDescription = stringResource(R.string.generic_decrease),
                                tint =
                                  if (appFontScale > 0.75f) {
                                    MaterialTheme.colorScheme.primary
                                  } else {
                                    MaterialTheme.colorScheme.outline
                                  },
                              )
                            }
                            AppSlider(
                              value = appFontScale,
                              onValueChange = { newScale ->
                                val stepped = ((newScale * 20).roundToInt() / 20f).coerceIn(0.75f, 1.40f)
                                preferences.appFontScale.set(stepped)
                              },
                              modifier = Modifier.weight(1f),
                              valueRange = 0.75f..1.40f,
                              steps = 12,
                            )
                            IconButton(
                              onClick = {
                                val newScale = ((appFontScale + 0.05f) * 100).roundToInt() / 100f
                                preferences.appFontScale.set(newScale.coerceIn(0.75f, 1.40f))
                              },
                              enabled = appFontScale < 1.40f,
                            ) {
                              Icon(
                                Icons.RoundedFilled.Add,
                                contentDescription = stringResource(R.string.generic_increase),
                                tint =
                                  if (appFontScale < 1.40f) {
                                    MaterialTheme.colorScheme.primary
                                  } else {
                                    MaterialTheme.colorScheme.outline
                                  },
                              )
                            }
                          }
                        }
                      },
                    )
                  }

                  PreferenceDivider()

                  val appUiScale by preferences.appUiScale.collectAsState()
                  ExpressivePreferenceItem {
                  SliderPreference(
                    modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_ui_scale_title),
                    value = pendingAppUiScale ?: appUiScale,
                    onValueChange = { pendingAppUiScale = it },
                    title = {
                      Text(stringResource(R.string.pref_appearance_ui_scale_title))
                    },
                    valueRange = 0.5f..1.5f,
                    valueSteps = 19,
                    summary = {
                      Text(
                        stringResource(
                          R.string.pref_appearance_ui_scale_summary,
                          ((pendingAppUiScale ?: appUiScale) * 100).roundToInt(),
                        ),
                        color = MaterialTheme.colorScheme.outline,
                      )
                    },
                    onSliderValueChange = { pendingAppUiScale = it },
                    sliderValue = pendingAppUiScale ?: appUiScale,
                  )
                  }

                  }
                }
              }
            }
          }

          item {
            PreferenceSectionHeader(position = PreferenceSectionPosition.Middle,
              title = stringResource(id = R.string.pref_appearance_category_file_browser),
            )
          }

          item {
            ExpressivePreferenceGroup {
              val itemCount = 7
              val unlimitedNameLines by preferences.unlimitedNameLines.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_unlimited_name_lines_title),
                value = unlimitedNameLines,
                onValueChange = { preferences.unlimitedNameLines.set(it) },
                title = {
                  Text(
                    text = stringResource(id = R.string.pref_appearance_unlimited_name_lines_title),
                  )
                },
                summary = {
                  Text(
                    text =
                      stringResource(
                        id = R.string.pref_appearance_unlimited_name_lines_summary,
                      ),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                groupIndex = 0,
                groupSize = itemCount,
              )

              val showUnplayedOldVideoLabel by preferences.showUnplayedOldVideoLabel.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_show_unplayed_old_video_label_title),
                value = showUnplayedOldVideoLabel,
                onValueChange = { preferences.showUnplayedOldVideoLabel.set(it) },
                title = {
                  Text(
                    text =
                      stringResource(
                        id = R.string.pref_appearance_show_unplayed_old_video_label_title,
                      ),
                  )
                },
                summary = {
                  Text(
                    text =
                      stringResource(
                        id = R.string.pref_appearance_show_unplayed_old_video_label_summary,
                      ),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                groupIndex = 1,
                groupSize = itemCount,
              )

              val unplayedOldVideoDays by preferences.unplayedOldVideoDays.collectAsState()
              ExpressivePreferenceTile(index = 2, itemCount = itemCount) {
                SliderPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_unplayed_old_video_days_title),
                  value = unplayedOldVideoDays.toFloat(),
                  onValueChange = { preferences.unplayedOldVideoDays.set(it.roundToInt()) },
                  title = {
                    Text(
                      text = stringResource(id = R.string.pref_appearance_unplayed_old_video_days_title),
                    )
                  },
                  valueRange = 0f..30f,
                  valueSteps = 29,
                  summary = {
                    Text(
                      text =
                        if (unplayedOldVideoDays == 0) {
                          "Unlimited — NEW stays until the watched threshold is reached"
                        } else {
                          stringResource(
                            id = R.string.pref_appearance_unplayed_old_video_days_summary,
                            unplayedOldVideoDays,
                          )
                        },
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  onSliderValueChange = { preferences.unplayedOldVideoDays.set(it.roundToInt()) },
                  sliderValue = unplayedOldVideoDays.toFloat(),
                  enabled = showUnplayedOldVideoLabel,
                )
              }

              val autoScrollToLastPlayed by browserPreferences.autoScrollToLastPlayed.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_auto_scroll_title),
                value = autoScrollToLastPlayed,
                onValueChange = { browserPreferences.autoScrollToLastPlayed.set(it) },
                title = {
                  Text(text = stringResource(R.string.pref_appearance_auto_scroll_title))
                },
                summary = {
                  Text(
                    text = stringResource(R.string.pref_appearance_auto_scroll_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                groupIndex = 3,
                groupSize = itemCount,
              )

              ExpressivePreferenceTile(index = 4, itemCount = itemCount) {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_tree_flatten_depth_title),
                  value = treeFlattenDepth,
                  onValueChange = browserPreferences.treeFlattenDepth::set,
                  values = TreeFlattenDepth.entries,
                  valueToText = { AnnotatedString(it.displayName) },
                  title = { Text(stringResource(R.string.pref_tree_flatten_depth_title)) },
                  summary = {
                    Text(
                      stringResource(
                        R.string.pref_tree_flatten_depth_summary,
                        treeFlattenDepth.displayName,
                      ),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
              }

              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.ui_dual_pane_view),
                value = dualPaneForTablet,
                onValueChange = { browserPreferences.dualPaneForTablet.set(it) },
                title = {
                  Text(
                    text =
                      androidx.compose.ui.res.stringResource(
                        app.video.player.R.string.ui_dual_pane_view,
                      ),
                  )
                },
                summary = {
                  Text(
                    text =
                      androidx.compose.ui.res.stringResource(
                        app.video.player.R.string.ui_enable_dual_pane_layout_on_tablets_in_folder_view,
                      ),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                groupIndex = 5,
                groupSize = itemCount,
              )

              val deleteFolderAllContents by browserPreferences.deleteFolderAllContents.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.ui_delete_folder_all_contents),
                value = deleteFolderAllContents,
                onValueChange = { browserPreferences.deleteFolderAllContents.set(it) },
                title = {
                  Text(
                    androidx.compose.ui.res.stringResource(
                      app.video.player.R.string.ui_delete_folder_all_contents,
                    ),
                  )
                },
                summary = {
                  Text(
                    text =
                      if (deleteFolderAllContents) {
                        "Deletes entire folder (all files)"
                      } else {
                        "Only media files (audio/video) are deleted"
                      },
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                groupIndex = 6,
                groupSize = itemCount,
              )
            }
          }

          item {
            PreferenceSectionHeader(position = PreferenceSectionPosition.Middle,
              title = stringResource(id = R.string.pref_appearance_category_thumbnails),
            )
          }

          item {
            ExpressivePreferenceGroup {
              val showFramePosition = thumbnailGenerator != ThumbnailGenerator.MediaStore && thumbnailMode == ThumbnailMode.FrameAtPosition
              val itemCount = if (showFramePosition) 7 else 6
              val showVideoThumbnails by browserPreferences.showVideoThumbnails.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_show_video_thumbnails_title),
                value = showVideoThumbnails,
                onValueChange = { browserPreferences.showVideoThumbnails.set(it) },
                title = {
                  Text(
                    text = stringResource(id = R.string.pref_appearance_show_video_thumbnails_title),
                  )
                },
                summary = {
                  Text(
                    text =
                      stringResource(
                        id = R.string.pref_appearance_show_video_thumbnails_summary,
                      ),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                groupIndex = 0,
                groupSize = itemCount,
              )

              ExpressivePreferenceTile(index = 1, itemCount = itemCount) {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_thumbnail_generator_title),
                  value = thumbnailGenerator,
                  onValueChange = { selected ->
                    if (!changingThumbnailGenerator && selected != thumbnailGenerator) {
                      changingThumbnailGenerator = true
                      val progressToast = Toast.loading(context, context.getString(R.string.toast_updating_thumbnail_generator))
                      scope.launch {
                        try {
                          thumbnailRepository.setThumbnailGenerator(selected)
                          app.video.player.ui.player.MediaPlayerWidget.clearArtworkCache()
                          app.video.player.ui.player.MediaPlayerWidget.requestUpdate(context)
                          progressToast.update(thumbnailCacheClearedMessage, AppToastType.Success)
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                          progressToast.cancel()
                          throw cancelled
                        } catch (error: Exception) {
                          progressToast.update(context.getString(R.string.pref_thumbnail_cache_clear_failed, error.message ?: "Unknown error"), AppToastType.Error)
                        } finally {
                          changingThumbnailGenerator = false
                        }
                      }
                    }
                  },
                  values = ThumbnailGenerator.entries,
                  valueToText = {
                    AnnotatedString(
                      context.getString(
                        when (it) {
                          ThumbnailGenerator.Auto -> R.string.pref_thumbnail_generator_auto
                          ThumbnailGenerator.Native -> R.string.pref_thumbnail_generator_native
                          ThumbnailGenerator.MediaStore -> R.string.pref_thumbnail_generator_mediastore
                        },
                      ),
                    )
                  },
                  title = { Text(stringResource(R.string.pref_appearance_thumbnail_generator_title)) },
                  summary = {
                    Text(
                      text = stringResource(
                        when (thumbnailGenerator) {
                          ThumbnailGenerator.Auto -> R.string.pref_thumbnail_generator_auto_summary
                          ThumbnailGenerator.Native -> R.string.pref_thumbnail_generator_native_summary
                          ThumbnailGenerator.MediaStore -> R.string.pref_thumbnail_generator_mediastore_summary
                        },
                      ),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  enabled = !changingThumbnailGenerator,
                )
              }

              ExpressivePreferenceTile(index = 2, itemCount = itemCount) {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_thumbnail_generation_title),
                  value = thumbnailMode,
                  onValueChange = { newMode ->
                    if (newMode != thumbnailMode) {
                      pendingThumbnailMode = newMode
                    }
                  },
                  values = ThumbnailMode.entries,
                  valueToText = { AnnotatedString(it.displayName) },
                  title = {
                    Text(
                      text = stringResource(id = R.string.pref_appearance_thumbnail_generation_title),
                    )
                  },
                  summary = {
                    Text(
                      text =
                        when (thumbnailMode) {
                          ThumbnailMode.FrameAtPosition ->
                            "${thumbnailMode.displayName} (${thumbnailFramePosition.roundToInt()}%)"
                          else -> thumbnailMode.displayName
                        },
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  enabled = showVideoThumbnails && thumbnailGenerator != ThumbnailGenerator.MediaStore,
                )
              }

              ExpressivePreferenceTile(index = 3, itemCount = itemCount) {
                ListPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_thumbnail_quality_title),
                  value = thumbnailQuality,
                  onValueChange = { newQuality ->
                    if (newQuality != thumbnailQuality) {
                      scope.launch {
                        withContext(Dispatchers.IO) {
                          thumbnailRepository.clearThumbnailCache()
                        }
                        browserPreferences.thumbnailQuality.set(newQuality)
                      }
                    }
                  },
                  values = ThumbnailQuality.entries,
                  valueToText = { AnnotatedString(it.displayName) },
                  title = {
                    Text(
                      text = stringResource(id = R.string.pref_appearance_thumbnail_quality_title),
                    )
                  },
                  summary = {
                    Text(
                      text =
                        stringResource(
                          id = R.string.pref_appearance_thumbnail_quality_summary,
                          thumbnailQuality.maxSizePx,
                        ),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  enabled = showVideoThumbnails,
                )
              }

              ExpressivePreferenceTile(index = 4, itemCount = itemCount) {
                Preference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_appearance_thumbnail_parallelism_title),
                  title = {
                    Text(text = stringResource(id = R.string.pref_appearance_thumbnail_parallelism_title))
                  },
                  summary = {
                    Text(
                      text =
                        thumbnailParallelismLabel(
                          thumbnailParallelism,
                          automaticThumbnailParallelism,
                          maximumThumbnailParallelism,
                        ),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  onClick = { isThumbnailParallelismDialogOpen = true },
                  action = PreferenceAction.Dialog,
                  enabled = showVideoThumbnails,
                )
              }

              if (showFramePosition) {
                ExpressivePreferenceTile(index = 5, itemCount = itemCount) {
                  SliderPreference(
                    value = thumbnailFramePosition,
                    onValueChange = { browserPreferences.thumbnailFramePosition.set(it) },
                    sliderValue = thumbnailFramePosition,
                    onSliderValueChange = { browserPreferences.thumbnailFramePosition.set(it) },
                    title = {
                      Text(
                        text = stringResource(id = R.string.pref_appearance_thumbnail_position_title),
                      )
                    },
                    valueRange = 0f..100f,
                    valueSteps = 99,
                    summary = {
                      Text(
                        text =
                          stringResource(
                            id = R.string.pref_appearance_thumbnail_position_summary,
                            thumbnailFramePosition.roundToInt(),
                          ),
                        color = MaterialTheme.colorScheme.outline,
                      )
                    },
                    enabled = showVideoThumbnails,
                  )
                }
              }

              val tapThumbnailToSelect by gesturePreferences.tapThumbnailToSelect.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_gesture_tap_thumbnail_to_select_title),
                value = tapThumbnailToSelect,
                onValueChange = { gesturePreferences.tapThumbnailToSelect.set(it) },
                title = {
                  Text(
                    text = stringResource(id = R.string.pref_gesture_tap_thumbnail_to_select_title),
                  )
                },
                summary = {
                  Text(
                    text =
                      stringResource(
                        id = R.string.pref_gesture_tap_thumbnail_to_select_summary,
                      ),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                enabled = showVideoThumbnails,
                groupIndex = itemCount - 1,
                groupSize = itemCount,
              )
            }
          }

          item {
            PreferenceSectionHeader(
              position = if (television) PreferenceSectionPosition.Last else PreferenceSectionPosition.Middle,
              title = stringResource(R.string.pref_appearance_category_navigation),
            )
          }

          item {
            val showProfileTab by preferences.showProfileTab.collectAsState()
            val savedOrder by preferences.bottomNavigationOrder.collectAsState()
            val names = listOf("HOME", "PROFILE")
            val ordered =
              listOf("HOME") +
                (savedOrder.filter { it in names } + names)
                  .distinct()
                  .filterNot { it == "HOME" }
            val tabItems = ordered.map { id ->
              when (id) {
                "HOME" -> ReorderableTabItem(id, stringResource(R.string.pref_nav_home_title), stringResource(R.string.pref_nav_home_summary), true, required = true)
                else -> ReorderableTabItem(id, stringResource(R.string.pref_nav_profile_title), stringResource(R.string.pref_nav_profile_summary), showProfileTab, titleRes = R.string.pref_nav_profile_title)
              }
            }
            ExpressivePreferenceGroup {
              ReorderableTabPreferences(
                tabs = tabItems,
                onEnabledChange = { id, enabled ->
                  when (id) {
                    "PROFILE" -> preferences.showProfileTab.set(enabled)
                  }
                },
                onMove = { id, destination ->
                  if (id != "HOME") {
                    val current = ordered.toMutableList()
                    current.remove(id)
                    current.add(destination.coerceIn(1, current.size), id)
                    preferences.bottomNavigationOrder.set(current)
                  }
                },
              )
            }
          }

          if (!television) {
            item {
              PreferenceSectionHeader(
                position = PreferenceSectionPosition.Last,
                title = stringResource(R.string.pref_nav_actions_section),
              )
            }

            item {
              ExpressivePreferenceGroup {
                val showQuickPlayFab by preferences.showQuickPlayFab.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_quick_play_fab_title),
                  value = showQuickPlayFab,
                  onValueChange = preferences.showQuickPlayFab::set,
                  title = { Text(text = stringResource(R.string.pref_quick_play_fab_title)) },
                  summary = {
                    Text(
                      text = stringResource(R.string.pref_quick_play_fab_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  groupIndex = 0,
                  groupSize = 5,
                )
                val quickPlayFabDirect by preferences.quickPlayFabDirect.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.pref_quick_play_fab_direct_title),
                  value = quickPlayFabDirect,
                  onValueChange = preferences.quickPlayFabDirect::set,
                  enabled = showQuickPlayFab,
                  title = { Text(text = stringResource(R.string.pref_quick_play_fab_direct_title)) },
                  summary = {
                    Text(
                      text = stringResource(R.string.pref_quick_play_fab_direct_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  groupIndex = 1,
                  groupSize = 5,
                )
                val navigationBarGlow by preferences.navigationBarGlow.collectAsState()
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.navbar_glow_title),
                  value = navigationBarGlow,
                  onValueChange = preferences.navigationBarGlow::set,
                  title = { Text(stringResource(R.string.navbar_glow_title)) },
                  summary = { Text(stringResource(R.string.navbar_glow_summary), color = MaterialTheme.colorScheme.outline) },
                  groupIndex = 2,
                  groupSize = 5,
                )
                val legacyNavigationBar by preferences.legacyNavigationBar.collectAsState()
                val progressiveNavigationBlur by preferences.progressiveNavigationBlur.collectAsState()
                LaunchedEffect(legacyNavigationBar, progressiveNavigationBlur) {
                  if (legacyNavigationBar && progressiveNavigationBlur) {
                    preferences.progressiveNavigationBlur.set(false)
                  }
                }
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.legacy_navigation_bar_title),
                  value = legacyNavigationBar,
                  onValueChange = { enabled ->
                    preferences.legacyNavigationBar.set(enabled)
                    if (enabled) preferences.progressiveNavigationBlur.set(false)
                  },
                  title = { Text(stringResource(R.string.legacy_navigation_bar_title)) },
                  summary = { Text(stringResource(R.string.legacy_navigation_bar_summary), color = MaterialTheme.colorScheme.outline) },
                  groupIndex = 3,
                  groupSize = 5,
                )
                SwitchPreference(
                  modifier = Modifier.settingsSearchTarget(R.string.progressive_navigation_blur_title),
                  value = progressiveNavigationBlur && !legacyNavigationBar,
                  onValueChange = preferences.progressiveNavigationBlur::set,
                  enabled = !legacyNavigationBar,
                  title = { Text(stringResource(R.string.progressive_navigation_blur_title)) },
                  summary = { Text(stringResource(R.string.progressive_navigation_blur_summary), color = MaterialTheme.colorScheme.outline) },
                  groupIndex = 4,
                  groupSize = 5,
                )
              }
            }
          }
        }
      }
    }
  }
}
