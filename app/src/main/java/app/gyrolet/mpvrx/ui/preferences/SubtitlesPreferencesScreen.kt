/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.preferences

import android.net.Uri
import app.gyrolet.mpvrx.ui.components.AppToast as Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import app.gyrolet.mpvrx.presentation.components.AppTextField as TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState as collectFlowAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.domain.fonts.GoogleFontsRepository
import app.gyrolet.mpvrx.preferences.AppearancePreferences
import app.gyrolet.mpvrx.preferences.DEFAULT_SUBTITLE_FONT_FAMILY
import app.gyrolet.mpvrx.preferences.LEGACY_DEFAULT_SUBTITLE_FONT_FAMILY
import app.gyrolet.mpvrx.preferences.SubtitlesPreferences
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.presentation.Screen
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.preferences.components.SwitchPreference
import app.gyrolet.mpvrx.ui.player.defaultSubtitleFontDisplayName
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.ui.utils.currentMpvConfigOverrideOptions
import app.gyrolet.mpvrx.ui.utils.popSafely
import app.gyrolet.mpvrx.utils.media.CustomFontEntry
import app.gyrolet.mpvrx.utils.media.copyFontsFromDirectory
import app.gyrolet.mpvrx.utils.media.loadCustomFontEntries
import app.gyrolet.mpvrx.utils.media.subtitleFontChoices
import com.github.k1rakishou.fsaf.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import app.gyrolet.mpvrx.ui.preferences.components.AppListPreference as ListPreference
import app.gyrolet.mpvrx.ui.preferences.components.AppPreference as Preference
import app.gyrolet.mpvrx.ui.preferences.components.AppTextFieldPreference as TextFieldPreference
import org.koin.compose.koinInject

@Serializable
object SubtitlesPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val backstack = LocalBackStack.current
    val playerPanel = LocalPlayerSettingsPanel.current
    val preferences = koinInject<SubtitlesPreferences>()
    val appearancePreferences = koinInject<AppearancePreferences>()
    val googleFontsRepository = koinInject<GoogleFontsRepository>()
    val configOwnedOptions = currentMpvConfigOverrideOptions()
    val fileManager = koinInject<FileManager>()
    val scope = rememberCoroutineScope()
    var fontRefreshKey by remember { mutableStateOf(0) }

    fun reloadFontsFrom(uriString: String) {
      if (uriString.isBlank()) return
      scope.launch(Dispatchers.IO) {
        val copiedFonts = copyFontsFromDirectory(context, fileManager, uriString)
        withContext(Dispatchers.Main) {
          fontRefreshKey++
          Toast
            .makeText(
              context,
              if (copiedFonts > 0) {
                context.getString(R.string.fonts_loaded, copiedFonts)
              } else {
                context.getString(R.string.pref_subtitles_font_no_custom)
              },
              Toast.LENGTH_SHORT,
            ).show()
        }
      }
    }

    val fontFolderPicker =
      rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
      ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
          context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
          )
        }.onFailure { error ->
          Toast
            .makeText(
              context,
              context.getString(R.string.pref_subtitles_font_copy_failed, error.message ?: "Unknown error"),
              Toast.LENGTH_SHORT,
            ).show()
        }

        val uriString = uri.toString()
        preferences.fontsFolder.set(uriString)
        reloadFontsFrom(uriString)
      }

    Scaffold(
      contentWindowInsets = settingsContentWindowInsets(),
      topBar = {
        SettingsTopAppBar(
          title = stringResource(R.string.pref_subtitles),
          onBack = { backstack.popSafely() },
        )
      },
    ) { padding ->
      ProvideExpressivePreferenceLocals {
        val fontsFolder by preferences.fontsFolder.collectAsState()
        val selectedFont by preferences.font.collectAsState()
        val appFontFamily by appearancePreferences.googleFontFamily.collectAsState()
        val requestedSearchTarget by SettingsSearchNavigation.target.collectFlowAsState()
        var customFonts by remember { mutableStateOf<List<CustomFontEntry>>(emptyList()) }
        var downloadedGoogleFonts by remember { mutableStateOf<List<String>>(emptyList()) }

        LaunchedEffect(fontsFolder, fontRefreshKey, appFontFamily) {
          downloadedGoogleFonts =
            withContext(Dispatchers.IO) {
              googleFontsRepository.installedFonts(appFontFamily).map { it.family }
            }
          customFonts =
            loadCustomFontEntries(context)
              .filterNot { googleFontsRepository.isManagedMpvFont(it.file) }
              .filter { it.familyName != DEFAULT_SUBTITLE_FONT_FAMILY && it.familyName != LEGACY_DEFAULT_SUBTITLE_FONT_FAMILY }
        }

        val fontChoices =
          remember(downloadedGoogleFonts, customFonts) {
            subtitleFontChoices(downloadedGoogleFonts, customFonts)
              .filter { it.familyName != DEFAULT_SUBTITLE_FONT_FAMILY && it.familyName != LEGACY_DEFAULT_SUBTITLE_FONT_FAMILY }
          }
        val availableFonts = fontChoices.map { it.label }
        val effectiveDefaultFont = DEFAULT_SUBTITLE_FONT_FAMILY

        val (settingsListState, settingsHighlight) =
          rememberSettingsSearchList(SubtitlesPreferencesScreen, MaterialTheme.colorScheme.primary)
        LazyColumn(
          state = settingsListState,
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding)
              .then(settingsHighlight),
        ) {
          // === GENERAL SECTION ===
          item {
            PreferenceSectionHeader(
              position = if (playerPanel) PreferenceSectionPosition.Single else PreferenceSectionPosition.First,
              title = stringResource(R.string.general),
              modifier = Modifier.settingsSearchTarget(R.string.pref_subtitles),
            )
          }

          item {
            PreferenceCard {
              val preferredLanguages by preferences.preferredLanguages.collectAsState()
              ExpressivePreferenceItem {
              TextFieldPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_preferred_languages),
                value = preferredLanguages,
                enabled = "slang" !in configOwnedOptions,
                onValueChange = preferences.preferredLanguages::set,
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
                    Text(stringResource(R.string.enter_subtitle_title_preferences))
                    TextField(
                      value,
                      onValueChange,
                      modifier = Modifier.fillMaxWidth(),
                      placeholder = { Text(stringResource(R.string.subtitle_title_preferences_placeholder)) },
                    )
                  }
                },
              )
              }

              PreferenceDivider()

              val autoload by preferences.autoloadMatchingSubtitles.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_subtitles_autoload_title),
                value = autoload,
                enabled =
                  setOf("sub-auto", "sub-file-paths", "subs-fallback").none(configOwnedOptions::contains),
                onValueChange = { preferences.autoloadMatchingSubtitles.set(it) },
                title = { Text(stringResource(R.string.pref_subtitles_autoload_title)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_subtitles_autoload_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val overrideAss by preferences.overrideAssSubs.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.player_sheets_sub_override_ass),
                value = overrideAss,
                enabled =
                  setOf("sub-ass-override", "secondary-sub-ass-override", "sub-pos", "secondary-sub-pos")
                    .none(configOwnedOptions::contains),
                onValueChange = { preferences.overrideAssSubs.set(it) },
                title = { Text(stringResource(R.string.player_sheets_sub_override_ass)) },
                summary = {
                  Text(
                    stringResource(R.string.player_sheets_sub_override_ass_subtitle),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val scaleByWindow by preferences.scaleByWindow.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.player_sheets_sub_scale_by_window),
                value = scaleByWindow,
                enabled =
                  setOf(
                    "sub-scale-by-window",
                    "sub-use-margins",
                  ).none(configOwnedOptions::contains),
                onValueChange = { preferences.scaleByWindow.set(it) },
                title = { Text(stringResource(R.string.player_sheets_sub_scale_by_window)) },
                summary = {
                  Text(
                    stringResource(R.string.player_sheets_sub_scale_by_window_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val secondaryPinchZoom by preferences.secondarySubPinchZoom.collectAsState()
              SwitchPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_subtitles_secondary_pinch_zoom),
                value = secondaryPinchZoom,
                enabled = setOf("sub-scale", "secondary-sub-scale").none(configOwnedOptions::contains),
                onValueChange = { preferences.secondarySubPinchZoom.set(it) },
                title = { Text(stringResource(R.string.pref_subtitles_secondary_pinch_zoom)) },
                summary = {
                  Text(
                    stringResource(R.string.pref_subtitles_secondary_pinch_zoom_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )
            }
          }

          // === FONT SECTION ===
          if (!playerPanel) {
          item {
            PreferenceSectionHeader(position = PreferenceSectionPosition.Last, title = stringResource(R.string.pref_section_subtitle_fonts))
          }

          item {
            PreferenceCard {
              ExpressivePreferenceItem {
              Preference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_subtitles_fonts_dir),
                enabled = "sub-fonts-dir" !in configOwnedOptions,
                title = { Text(stringResource(R.string.pref_subtitles_fonts_dir)) },
                summary = {
                  val folderSummary =
                    if (fontsFolder.isBlank()) {
                      stringResource(R.string.pref_subtitles_font_directory_summary)
                    } else {
                      val displayPath =
                        runCatching { getSimplifiedPathFromUri(fontsFolder) }
                          .getOrElse { Uri.decode(fontsFolder) ?: fontsFolder }
                      stringResource(R.string.pref_subtitles_font_directory_selected, displayPath)
                    }
                  Text(
                    text = folderSummary,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                icon = {
                  Icon(
                    Icons.RoundedFilled.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                },
                onClick = { fontFolderPicker.launch(null) },
              )
              }

              PreferenceDivider()

              val effectiveSelected =
                selectedFont.takeUnless {
                  it.isBlank() ||
                    it == DEFAULT_SUBTITLE_FONT_FAMILY ||
                    it == LEGACY_DEFAULT_SUBTITLE_FONT_FAMILY
                }.orEmpty()
              val fontValues = (listOf("") + availableFonts + listOf(effectiveSelected).filter(String::isNotBlank)).distinct()
              // Keep the clicked filename highlighted when multiple files advertise the
              // same family. mpv itself continues to receive the real family name.
              var preferredFontLabel by rememberSaveable { mutableStateOf("") }
              fun familyForLabel(label: String): String =
                fontChoices.firstOrNull { it.label == label }?.familyName ?: label
              val fontValue =
                preferredFontLabel.takeIf { it in fontValues && familyForLabel(it) == effectiveSelected }
                  ?: fontValues.firstOrNull { familyForLabel(it) == effectiveSelected }.orEmpty()
              val fontLabel = fontValue.ifBlank { defaultSubtitleFontDisplayName(effectiveDefaultFont) }

              ExpressivePreferenceItem {
              ListPreference(
                modifier = Modifier.settingsSearchTarget(R.string.pref_subtitles_font_title),
                value = fontValue,
                enabled = "sub-font" !in configOwnedOptions,
                onValueChange = { label ->
                  preferredFontLabel = label
                  preferences.font.set(familyForLabel(label))
                },
                values = fontValues,
                valueToText = {
                  AnnotatedString(it.ifBlank { context.getString(R.string.pref_subtitles_font_default) })
                },
                title = { Text(stringResource(R.string.pref_subtitles_font_title)) },
                summary = {
                  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                      stringResource(R.string.pref_subtitles_font_summary, fontLabel),
                      color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                      if (availableFonts.isEmpty()) {
                        stringResource(R.string.pref_subtitles_font_no_custom)
                      } else {
                        stringResource(R.string.fonts_loaded, fontChoices.size)
                      },
                      color = MaterialTheme.colorScheme.outline,
                      style = MaterialTheme.typography.bodySmall,
                    )
                  }
                },
              )
              }

              PreferenceDivider()

              ExpressivePreferenceItem {
              Preference(
                modifier = Modifier.settingsSearchTarget(R.string.reload_fonts),
                title = { Text(stringResource(R.string.reload_fonts)) },
                summary = {
                  Text(
                    stringResource(R.string.reload_fonts_summary),
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                icon = {
                  Icon(
                    Icons.RoundedFilled.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                },
                enabled = fontsFolder.isNotBlank(),
                onClick = { reloadFontsFrom(fontsFolder) },
              )
              }

              if (fontsFolder.isNotBlank() || customFonts.isNotEmpty()) {
                PreferenceDivider()

                ExpressivePreferenceItem {
                Preference(
                  title = {
                    Text(
                      stringResource(R.string.clear_font_directory),
                      color = MaterialTheme.colorScheme.error,
                    )
                  },
                  icon = {
                    Icon(
                      Icons.RoundedFilled.Clear,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                    )
                  },
                  onClick = {
                    preferences.fontsFolder.set("")
                    preferences.font.set("")
                    scope.launch(Dispatchers.IO) {
                      googleFontsRepository.clearImportedMpvFonts()
                      googleFontsRepository.syncMpvFonts(appFontFamily)
                      withContext(Dispatchers.Main) { fontRefreshKey++ }
                    }
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
  }
}
