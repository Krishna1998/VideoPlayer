/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.preferences

import androidx.annotation.StringRes
import app.video.player.R
import app.video.player.presentation.Screen
import app.video.player.ui.background.BackgroundType
import app.video.player.ui.background.RandomInterval
import app.video.player.ui.securefolder.SecureFolderSettingsScreen
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.min

/**
 * Represents a searchable preference item.
 * Used to index all preferences for the settings search feature.
 */
data class SearchablePreference(
  @StringRes val titleRes: Int? = null,
  val title: String? = null,
  @StringRes val summaryRes: Int? = null,
  val summary: String? = null,
  val keywords: List<String> = emptyList(),
  val category: String,
  val screen: Screen,
  @StringRes val targetRes: Int? = null,
  val anchorItemIndex: Int? = null,
)

data class SettingsSearchResult(
  val preference: SearchablePreference,
  val titleMatchIndices: Set<Int>,
  val score: Int,
)

private data class SearchEntrySpec(
  @StringRes val titleRes: Int,
  val keywords: List<String>,
  @StringRes val targetRes: Int? = null,
)

private fun MutableList<SearchablePreference>.addSearchEntries(
  category: String,
  screen: Screen,
  anchorItemIndex: Int,
  vararg entries: SearchEntrySpec,
) {
  entries.forEach { entry ->
    add(
      SearchablePreference(
        titleRes = entry.titleRes,
        keywords = entry.keywords,
        category = category,
        screen = screen,
        targetRes = entry.targetRes,
        anchorItemIndex = anchorItemIndex,
      ),
    )
  }
}

/**
 * All searchable preferences indexed for settings search.
 */
object SearchablePreferences {
  private val staticPreferences: List<SearchablePreference> =
    buildList {
      // Appearance preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_title,
          summaryRes = R.string.pref_appearance_summary,
          keywords = listOf("theme", "dark", "light", "amoled", "material you", "color", "appearance"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_amoled_mode_title,
          summaryRes = R.string.pref_appearance_amoled_mode_summary,
          keywords = listOf("amoled", "black", "dark", "oled", "pure black"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_liquid_glass_title,
          summaryRes = R.string.pref_appearance_liquid_glass_summary,
          keywords = listOf("liquid glass", "glass", "blur", "translucent", "navigation", "seekbar", "switch"),
          category = "Appearance",
          screen = LiquidGlassPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_liquid_glass_in_hdr_title,
          summaryRes = R.string.pref_liquid_glass_in_hdr_summary,
          keywords = listOf("hdr", "liquid glass", "textureview", "surfaceview", "player controls", "sdr"),
          category = "Appearance",
          screen = LiquidGlassPreferencesScreen,
        ),
      )
      listOf(
        R.string.pref_liquid_glass_opacity to listOf("opacity", "transparency", "backdrop"),
        R.string.pref_liquid_glass_frost to listOf("frost", "blur", "frosted"),
        R.string.pref_liquid_glass_refraction_height to listOf("refraction", "lens", "edge", "width"),
        R.string.pref_liquid_glass_refraction_amount to listOf("refraction", "lens", "strength"),
        R.string.pref_liquid_glass_depth to listOf("depth", "lens"),
        R.string.pref_liquid_glass_chromatic to listOf("chromatic", "aberration", "dispersion", "rainbow"),
        R.string.pref_liquid_glass_vibrancy to listOf("vibrancy", "color"),
        R.string.player_sheets_filters_Saturation to listOf("saturation", "color"),
        R.string.player_sheets_filters_brightness to listOf("brightness", "color"),
        R.string.player_sheets_filters_contrast to listOf("contrast", "color"),
        R.string.pref_liquid_glass_highlight_style to listOf("highlight", "style", "ambient", "plain"),
        R.string.pref_liquid_glass_highlight_strength to listOf("highlight", "strength"),
        R.string.pref_liquid_glass_highlight_width to listOf("highlight", "width", "rim"),
        R.string.pref_liquid_glass_highlight_blur to listOf("highlight", "blur", "softness"),
      ).forEach { (titleRes, keywords) ->
        add(
          SearchablePreference(
            titleRes = titleRes,
            keywords = listOf("liquid glass", "kyant") + keywords,
            category = "Appearance",
            screen = LiquidGlassPreferencesScreen,
            targetRes = titleRes,
          ),
        )
      }
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_app_font_title,
          summaryRes = R.string.pref_appearance_app_font_summary_search,
          keywords = listOf("font", "system", "typeface", "google sans", "google fonts", "download", "ui", "appearance"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_ui_scale_title,
          summaryRes = R.string.pref_appearance_ui_scale_summary,
          keywords = listOf("scale", "size", "zoom", "ui", "display", "appearance"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_unlimited_name_lines_title,
          summaryRes = R.string.pref_appearance_unlimited_name_lines_summary,
          keywords = listOf("name", "full", "truncate", "lines", "display"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_show_unplayed_old_video_label_title,
          summaryRes = R.string.pref_appearance_show_unplayed_old_video_label_summary,
          keywords = listOf("unplayed", "old", "label", "video", "new", "indicator"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_unplayed_old_video_days_title,
          keywords = listOf("days", "old", "video", "threshold", "time"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_auto_scroll_title,
          summaryRes = R.string.pref_appearance_auto_scroll_summary,
          keywords = listOf("scroll", "auto", "last played", "resume", "position"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_show_video_thumbnails_title,
          summaryRes = R.string.pref_appearance_show_video_thumbnails_summary,
          keywords = listOf("thumbnail", "thumbnails", "preview", "poster", "video"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_thumbnail_generator_title,
          summaryRes = R.string.pref_appearance_thumbnail_generator_summary,
          keywords = listOf("thumbnail", "generator", "auto", "default", "fallback", "native", "mediastore", "android", "system", "preview", "hidden"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_thumbnail_generation_title,
          summaryRes = R.string.pref_appearance_thumbnail_generation_summary,
          keywords =
            listOf(
              "thumbnail",
              "generation",
              "frame",
              "hybrid",
              "first frame",
              "embedded",
              "slider",
              "percentage",
              "preview",
            ),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_thumbnail_quality_title,
          summaryRes = R.string.pref_appearance_thumbnail_quality_summary,
          keywords = listOf("thumbnail", "quality", "resolution", "720p", "1080p", "1440p", "storage"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_tap_thumbnail_to_select_title,
          summaryRes = R.string.pref_gesture_tap_thumbnail_to_select_summary,
          keywords = listOf("thumbnail", "selection", "select", "tap", "gesture"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_show_network_thumbnails_title,
          summaryRes = R.string.pref_appearance_show_network_thumbnails_summary,
          keywords = listOf("network", "thumbnail", "stream", "preview", "images"),
          category = "Appearance",
          screen = AppearancePreferencesScreen,
        ),
      )
      // Layout preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_layout_title,
          summaryRes = R.string.pref_layout_summary,
          keywords = listOf("layout", "controls", "buttons", "player", "customize", "arrange"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_layout_top_right_controls,
          keywords = listOf("controls", "top", "right", "landscape", "buttons"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_layout_bottom_right_controls,
          keywords = listOf("controls", "bottom", "right", "landscape", "buttons"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_layout_bottom_left_controls,
          keywords = listOf("controls", "bottom", "left", "landscape", "buttons"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_layout_portrait_bottom_controls,
          keywords = listOf("controls", "portrait", "bottom", "buttons"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_appearance_force_dark_player_buttons_background_title,
          summaryRes = R.string.pref_appearance_force_dark_player_buttons_background_summary,
          keywords = listOf("dark", "black", "background", "buttons", "player", "light theme"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_display_hide_player_control_time,
          keywords = listOf("time", "hide", "controls", "disappear", "timeout", "ms"),
          category = "Appearance",
          screen = PlayerControlsPreferencesScreen,
        ),
      )

      // Player preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_player,
          summaryRes = R.string.pref_player_summary,
          keywords = listOf("player", "orientation", "gestures", "controls", "playback"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_orientation,
          keywords = listOf("orientation", "landscape", "portrait", "rotate", "screen"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_save_position_on_quit,
          keywords = listOf("save", "position", "resume", "remember", "progress"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_close_after_eof,
          keywords = listOf("close", "end", "playback", "quit", "finish"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_remember_brightness,
          keywords = listOf("brightness", "remember", "display", "screen"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_autoplay_next_video_title,
          summaryRes = R.string.pref_autoplay_next_video_summary,
          keywords = listOf("autoplay", "next", "video", "auto", "advance", "continuous"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_auto_pip_title,
          summaryRes = R.string.pref_auto_pip_summary,
          keywords = listOf("pip", "picture", "auto", "navigation", "home", "back"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_auto_pip_home_only_title,
          summaryRes = R.string.pref_auto_pip_home_only_summary,
          keywords = listOf("pip", "picture", "home", "gesture", "swipe", "back", "close"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_external_display_projection_title,
          summaryRes = R.string.pref_player_external_display_projection_summary,
          keywords = listOf("external display", "hdmi", "usb-c", "wireless display", "projection", "screen"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_keep_screen_on_when_paused_title,
          summaryRes = R.string.pref_player_keep_screen_on_when_paused_summary,
          keywords = listOf("keep screen on", "screen", "awake", "paused", "pause", "display", "sleep"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_autoplay_after_screen_unlock_title,
          summaryRes = R.string.pref_player_autoplay_after_screen_unlock_summary,
          keywords = listOf("autoplay", "screen unlock", "unlock", "resume", "lock screen", "continue playback"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.show_splash_ovals_on_double_tap_to_seek,
          keywords = listOf("oval", "circle", "double tap", "seek", "visual", "feedback"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.show_time_on_double_tap_to_seek,
          keywords = listOf("time", "double tap", "seek", "overlay", "timestamp"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_use_precise_seeking,
          keywords = listOf("precise", "seek", "keyframes", "accurate", "navigation"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_custom_skip_duration_title,
          summaryRes = R.string.pref_player_custom_skip_duration_summary,
          keywords = listOf("custom skip", "skip duration", "forward", "seek", "seconds", "jump"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_online_skip_markers_title,
          summary = "Fetch intro, recap, outro, credits, and preview markers from an online provider.",
          keywords =
            listOf(
              "online",
              "skip markers",
              "intro",
              "outro",
              "credits",
              "preview",
              "recap",
              "opening",
              "ending",
            ),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_marker_provider_title,
          targetRes = R.string.pref_online_skip_markers_title,
          summary = "Choose IntroDB, TIDB, AniSkip, or Anime Skip for online intro/outro markers.",
          keywords =
            listOf(
              "provider",
              "source",
              "introdb",
              "tidb",
              "theintrodb",
              "aniskip",
              "anime",
              "animeskip",
              "online markers",
              "skip provider",
            ),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_chapter_detect_title,
          summary = "Create skip markers from chapter names such as opening, ending, credits, or preview.",
          keywords =
            listOf(
              "chapter titles",
              "chapters",
              "intro",
              "outro",
              "opening",
              "ending",
              "credits",
              "preview",
              "markers",
            ),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_auto_skip_intro_title,
          summary = "Skip opening markers automatically during playback.",
          keywords = listOf("auto skip", "intro", "opening", "automatic", "skip op"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_auto_skip_outro_title,
          summary = "Skip ending markers automatically during playback.",
          keywords = listOf("auto skip", "outro", "ending", "credits", "automatic", "skip ed"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_haptic_feedback_title,
          keywords = listOf("haptic", "vibration", "feedback", "selection", "drag"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_confirm_back_to_exit_title,
          summaryRes = R.string.pref_confirm_back_to_exit_summary,
          keywords = listOf("back", "exit", "confirm", "double", "leave", "playback"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_resume_playback_title,
          keywords = listOf("resume", "position", "always", "ask", "never", "minimum", "duration"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_resume_min_duration,
          summaryRes = R.string.pref_player_resume_mode_min_duration_summary,
          keywords = listOf("resume", "minimum", "duration", "length", "short", "seconds"),
          category = "Player",
          screen = PlayerPreferencesScreen,
          targetRes = R.string.pref_player_resume_playback_title,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_video_swipe_title,
          keywords = listOf("swipe", "left", "right", "video", "folder", "watched", "queue", "delete"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_nested_tab_swipes_title,
          keywords = listOf("swipe", "nested", "inner", "tabs", "navigation", "music", "network", "recents"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_video_swipe_right,
          keywords = listOf("swipe", "video", "folder", "action", "zone", "width", "edge", "percentage"),
          category = "Gestures",
          screen = VideoSwipePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_video_swipe_left,
          keywords = listOf("swipe", "video", "folder", "action", "zone", "width", "edge", "percentage"),
          category = "Gestures",
          screen = VideoSwipePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_gestures_brightness,
          keywords = listOf("brightness", "gesture", "swipe", "display", "control"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_gestures_volume,
          keywords = listOf("volume", "gesture", "swipe", "audio", "sound"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_gestures_pinch_to_zoom,
          keywords = listOf("zoom", "pinch", "gesture", "scale", "crop", "video"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_gestures_horizontal_swipe_to_seek,
          keywords = listOf("horizontal", "swipe", "seek", "gesture", "left", "right"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_gestures_horizontal_swipe_sensitivity,
          summaryRes = R.string.pref_player_gestures_horizontal_swipe_sensitivity_summary,
          keywords = listOf("horizontal", "swipe", "sensitivity", "seek", "distance", "speed"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_gestures_hold_for_multiple_speed,
          keywords = listOf("hold", "speed", "multiple", "playback", "tempo", "rate"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_controls_allow_gestures_in_panels,
          keywords = listOf("gestures", "panels", "controls", "overlay", "enable"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.swap_the_volume_and_brightness_slider,
          keywords = listOf("swap", "volume", "brightness", "slider", "left", "right"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_controls_show_loading_circle,
          keywords = listOf("loading", "circle", "indicator", "buffer", "progress"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_controls_drawer_title,
          summaryRes = R.string.pref_player_controls_drawer_summary,
          keywords = listOf("controls", "drawer", "chevron", "panel", "side", "drag", "more controls"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_display_show_status_bar,
          keywords = listOf("status bar", "navigation", "system", "show", "hide", "immersive"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_nav_bar_title,
          summaryRes = R.string.pref_nav_bar_summary,
          keywords = listOf("navigation bar", "controls", "system", "show", "hide"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_display_reduce_player_animation,
          keywords = listOf("reduce", "animation", "motion", "performance", "smooth"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )

      // Gesture preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture,
          summaryRes = R.string.pref_gesture_summary,
          keywords = listOf("gesture", "single tap", "double tap", "swipe", "media controls", "touch"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_player_double_tap_seek_duration,
          keywords = listOf("seek", "duration", "single tap", "double tap", "time", "seconds"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_double_tap_seek_area_width_title,
          summaryRes = R.string.pref_double_tap_seek_area_width_summary,
          keywords = listOf("area", "width", "single tap", "double tap", "seek", "region", "percent"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_double_tap_left_title,
          keywords = listOf("single tap", "double tap", "left", "seek", "backward", "rewind"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_double_tap_center_title,
          keywords = listOf("single tap", "double tap", "center", "play", "pause", "action"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_double_tap_right_title,
          keywords = listOf("single tap", "double tap", "right", "seek", "forward", "advance"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_use_single_tap_for_left_title,
          summaryRes = R.string.pref_gesture_use_single_tap_for_left_summary,
          keywords = listOf("single", "tap", "left", "seek", "rewind", "controls", "zone"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_use_single_tap_for_center_title,
          summaryRes = R.string.pref_gesture_use_single_tap_for_center_summary,
          keywords = listOf("single", "tap", "center", "play", "pause", "controls", "zone"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_use_single_tap_for_right_title,
          summaryRes = R.string.pref_gesture_use_single_tap_for_right_summary,
          keywords = listOf("single", "tap", "right", "seek", "forward", "controls", "zone"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_media_previous,
          keywords = listOf("media", "previous", "gesture", "control", "backward"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_media_play,
          keywords = listOf("media", "play", "pause", "gesture", "control"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_gesture_media_next,
          keywords = listOf("media", "next", "gesture", "control", "forward"),
          category = "Gestures",
          screen = GesturePreferencesScreen,
        ),
      )
      // Storage / Folder preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_folders_title,
          summaryRes = R.string.pref_folders_summary,
          keywords = listOf("folders", "blacklist", "hide", "exclude", "manage"),
          category = "Folders",
          screen = FoldersPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_folders_include_hidden_title,
          targetRes = R.string.pref_hidden_scan_per_mode_title,
          summaryRes = R.string.pref_folders_include_hidden_summary,
          keywords =
            listOf("hidden", "dot folder", "dot file", "marker", "custom", "no media", "nomedia", "include", "scan"),
          category = "Folders",
          screen = FoldersPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_folders_add_folder,
          keywords = listOf("add", "folder", "exclude", "blacklist"),
          category = "Folders",
          screen = FolderBlacklistScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_folders_clear_all,
          keywords = listOf("clear", "all", "folders", "blacklist", "reset"),
          category = "Folders",
          screen = FolderBlacklistScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_blacklist_title,
          summaryRes = R.string.pref_blacklist_summary,
          keywords = listOf("blacklist", "exclude", "excluded", "hide", "folder", "audio", "video"),
          category = "Folders",
          screen = FoldersPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_filter_short_videos,
          keywords = listOf("minimum duration", "short videos", "filter", "scan"),
          category = "Folders",
          screen = FoldersPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_filter_small_files,
          keywords = listOf("small files", "minimum size", "filter", "scan"),
          category = "Folders",
          screen = FoldersPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_whitelist_title,
          summaryRes = R.string.pref_whitelist_summary,
          keywords = listOf("include", "allow", "folder", "whitelist", "only"),
          category = "Folders",
          screen = FoldersPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_whitelist_enable,
          keywords = listOf("whitelist", "enable", "only", "scan", "restrict"),
          category = "Folders",
          screen = FolderWhitelistScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_whitelist_add,
          keywords = listOf("whitelist", "add", "folder", "include", "allow"),
          category = "Folders",
          screen = FolderWhitelistScreen,
        ),
      )
      // Decoder preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_decoder,
          summaryRes = R.string.pref_decoder_summary,
          keywords = listOf("decoder", "hardware", "gpu", "debanding", "video"),
          category = "Decoder",
          screen = DecoderPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_decoder_try_hw_dec_title,
          keywords = listOf("hardware", "decoding", "hw", "acceleration", "gpu"),
          category = "Decoder",
          screen = DecoderPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_decoder_gpu_next_title,
          summaryRes = R.string.pref_decoder_gpu_next_summary,
          keywords = listOf("gpu", "next", "rendering", "backend", "vulkan", "opengl"),
          category = "Decoder",
          screen = DecoderPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_decoder_vulkan_experimental_title,
          summaryRes = R.string.pref_decoder_vulkan_summary,
          keywords = listOf("vulkan", "gpu", "rendering", "graphics", "api", "performance"),
          category = "Decoder",
          screen = DecoderPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_decoder_debanding_title,
          keywords = listOf("deband", "banding", "gradient", "visual", "quality"),
          category = "Decoder",
          screen = DecoderPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_decoder_yuv420p_title,
          summaryRes = R.string.pref_decoder_yuv420p_summary,
          keywords = listOf("yuv420p", "chroma", "subsampling", "format", "compatibility"),
          category = "Decoder",
          screen = DecoderPreferencesScreen,
        ),
      )

      // Subtitle preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_subtitles,
          summaryRes = R.string.pref_subtitles_summary,
          keywords = listOf("subtitles", "subs", "language", "fonts", "text"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_preferred_languages,
          keywords = listOf("language", "preferred", "subtitle", "audio", "locale", "code"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_subtitles_autoload_title,
          summaryRes = R.string.pref_subtitles_autoload_summary,
          keywords = listOf("autoload", "automatic", "subtitles", "external", "load"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_subtitles_disable_by_default_title,
          summaryRes = R.string.pref_subtitles_disable_by_default_summary,
          keywords = listOf("disable", "default", "subtitles", "off", "auto", "hide"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.player_sheets_sub_override_ass,
          summaryRes = R.string.player_sheets_sub_override_ass_subtitle,
          keywords = listOf("ass", "override", "subtitle", "ssa", "format", "style"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.player_sheets_sub_scale_by_window,
          summaryRes = R.string.player_sheets_sub_scale_by_window_summary,
          keywords = listOf("scale", "window", "subtitle", "size", "resize", "fit"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_subtitles_secondary_pinch_zoom,
          summaryRes = R.string.pref_subtitles_secondary_pinch_zoom_summary,
          keywords = listOf("pinch", "zoom", "secondary", "subtitle", "scale", "dual"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_subtitles_fonts_dir,
          summaryRes = R.string.pref_subtitles_font_directory_summary,
          keywords = listOf("fonts", "directory", "subtitle", "custom", "folder"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_subtitles_font_title,
          summaryRes = R.string.pref_subtitles_font_no_custom,
          keywords = listOf("font", "fonts", "family", "subtitle", "typography", "custom"),
          category = "Subtitles",
          screen = SubtitlesPreferencesScreen,
        ),
      )

      // Audio preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio,
          summaryRes = R.string.pref_audio_summary,
          keywords = listOf("audio", "language", "channels", "pitch", "sound"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_orientation_title,
          keywords = listOf("audio", "music", "orientation", "portrait", "landscape", "auto", "rotate"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_preferred_languages,
          keywords = listOf("language", "preferred", "subtitle", "audio", "locale", "code"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_pitch_correction_title,
          summaryRes = R.string.pref_audio_pitch_correction_summary,
          keywords = listOf("pitch", "correction", "speed", "audio", "sound"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_spatial_title,
          summaryRes = R.string.pref_audio_spatial_summary,
          keywords = listOf("spatial", "stereo", "wide", "immersive", "music", "audio"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_volume_normalization_title,
          summaryRes = R.string.pref_audio_volume_normalization_summary,
          keywords = listOf("volume", "normalization", "loudness", "audio", "sound"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_ignore_audio_focus_title,
          summaryRes = R.string.pref_ignore_audio_focus_summary,
          keywords = listOf("audio", "focus", "ignore", "mix", "simultaneous", "音频焦点", "同时播放"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_background_playback_title,
          summaryRes = R.string.pref_audio_background_playback_summary,
          keywords = listOf("background", "playback", "audio", "service", "music"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_mini_player_track_switching_title,
          summaryRes = R.string.pref_audio_mini_player_track_switching_summary,
          keywords = listOf("mini player", "background", "switch", "song", "audio", "music", "change"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_autoplay_next_audio_title,
          summaryRes = R.string.pref_autoplay_next_audio_summary,
          keywords = listOf("autoplay", "next", "audio", "music", "advance", "continuous"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_video_background_playback_title,
          summaryRes = R.string.pref_video_background_playback_summary,
          keywords = listOf("background", "playback", "video", "service", "media"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_background_playback_behavior,
          summaryRes = R.string.pref_background_playback_per_video_summary,
          keywords = listOf("background", "remember", "per video", "individual", "save", "video"),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_channels,
          keywords = listOf("channels", "audio", "stereo", "surround", "output", "sound"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_audio_volume_boost_cap,
          keywords = listOf("volume", "boost", "cap", "maximum", "amplify"),
          category = "Audio",
          screen = AudioPreferencesScreen,
        ),
      )

      // Advanced preferences
      add(
        SearchablePreference(
          titleRes = R.string.pref_custom_lua_title,
          summaryRes = R.string.pref_custom_lua_summary,
          keywords = listOf("lua", "js", "javascript", "custom", "button", "code", "player", "overlay", "script"),
          category = "Player",
          screen = CustomButtonScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced,
          summaryRes = R.string.pref_advanced_summary,
          keywords = listOf("advanced", "mpv", "config", "logs", "debug"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_export_settings_title,
          summaryRes = R.string.pref_export_settings_summary,
          keywords = listOf("export", "backup", "settings", "xml", "save"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_import_settings_title,
          summaryRes = R.string.pref_import_settings_summary,
          keywords = listOf("import", "restore", "settings", "xml", "load"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_mpv_conf_storage_location,
          keywords = listOf("storage", "location", "directory", "folder", "config"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_mpv_conf,
          keywords = listOf("mpv", "conf", "config", "configuration", "settings"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_input_conf,
          keywords = listOf("input", "conf", "keybindings", "shortcuts", "keys", "controls"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )

      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_enable_recently_played_title,
          summaryRes = R.string.pref_advanced_enable_recently_played_summary,
          keywords = listOf("recently", "played", "history", "enable", "track"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_clear_playback_history,
          keywords = listOf("clear", "history", "playback", "reset", "delete"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_clear_config_cache_title,
          summaryRes = R.string.pref_clear_config_cache_summary,
          keywords = listOf("clear", "config", "cache", "mpv", "settings"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_clear_thumbnail_cache_title,
          summaryRes = R.string.pref_clear_thumbnail_cache_summary,
          keywords = listOf("clear", "thumbnail", "cache", "preview", "images"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_clear_fonts_cache,
          keywords = listOf("clear", "fonts", "cache", "reset"),
          category = "Advanced",
          screen = AdvancedPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_advanced_notification_style,
          summary = "Choose media controls, progress with chapters, or no playback notification.",
          keywords =
            listOf(
              "notification",
              "media controls",
              "progress",
              "chapters",
              "no notification",
              "hide notification",
              "background playback",
            ),
          category = "Player",
          screen = PlayerPreferencesScreen,
        ),
      )
      add(
        SearchablePreference(
          titleRes = R.string.pref_codecs_title,
          summaryRes = R.string.pref_codecs_summary,
          keywords = listOf("codec", "codecs", "hardware", "software", "decoder", "decoding", "av1", "hevc", "h264", "vp9", "hardware acceleration", "gpu", "cpu", "battery", "heating", "media", "mime"),
          category = "Advanced",
          screen = CodecCapabilitiesScreen,
        ),
      )




      // Settings added after the original catalog. Keep route metadata beside each entry so
      // search coverage and exact lazy-list navigation cannot drift independently.
      addSearchEntries(
        category = "Appearance",
        screen = AppearancePreferencesScreen,
        anchorItemIndex = 3,
        SearchEntrySpec(R.string.pref_tree_flatten_depth_title, listOf("tree", "folder", "path", "flatten", "compression")),
        SearchEntrySpec(R.string.ui_dual_pane_view, listOf("tablet", "two pane", "split", "folder")),
        SearchEntrySpec(R.string.ui_delete_folder_all_contents, listOf("delete", "folder", "all files", "media only")),
      )
      addSearchEntries(
        category = "Appearance",
        screen = AppearancePreferencesScreen,
        anchorItemIndex = 5,
        SearchEntrySpec(
          R.string.pref_appearance_thumbnail_position_title,
          listOf("thumbnail", "frame", "position", "percent", "preview"),
          R.string.pref_appearance_thumbnail_generation_title,
        ),
        SearchEntrySpec(
          R.string.pref_appearance_thumbnail_parallelism_title,
          listOf("thumbnail", "cores", "threads", "parallel", "cpu", "speed", "performance"),
        ),
      )
      addSearchEntries(
        category = "Appearance",
        screen = AppearancePreferencesScreen,
        anchorItemIndex = 7,
        SearchEntrySpec(R.string.pref_nav_profile_title, listOf("profile", "you", "library", "tab", "navigation")),
        SearchEntrySpec(R.string.navbar_glow_title, listOf("glass", "glow", "navbar", "rim", "highlight", "liquid glass")),
        SearchEntrySpec(
          R.string.legacy_navigation_bar_title,
          listOf("legacy", "classic", "bottom", "navigation", "mpvex"),
        ),
        SearchEntrySpec(
          R.string.progressive_navigation_blur_title,
          listOf("haze", "blur", "progressive", "navigation", "navbar"),
        ),
        SearchEntrySpec(R.string.pref_nav_network_title, listOf("network", "stream", "tab", "navigation")),
        SearchEntrySpec(R.string.pref_quick_play_fab_title, listOf("quick play", "fab", "floating button", "random")),
        SearchEntrySpec(R.string.pref_quick_play_fab_direct_title, listOf("quick play", "direct", "random", "chooser")),
      )
      addSearchEntries(
        category = "Appearance",
        screen = AppearancePreferencesScreen,
        anchorItemIndex = 9,
        SearchEntrySpec(
          R.string.connected_mini_player_navigation_title,
          listOf("mini player", "navigation", "connected", "dock", "separate", "classic"),
        ),
      )

      addSearchEntries(
        category = "Appearance",
        screen = PlayerControlsPreferencesScreen,
        anchorItemIndex = 5,
        SearchEntrySpec(R.string.pref_section_seekbar_style, listOf("seekbar", "style", "thick", "thin", "squiggly", "wavy")),
      )
      addSearchEntries(
        category = "Appearance",
        screen = ControlLayoutEditorScreen(ControlRegion.BOTTOM_RIGHT),
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.ui_landscape_seekbar_position, listOf("landscape", "seekbar", "position", "above", "below", "buttons")),
        SearchEntrySpec(
          R.string.pref_layout_landscape_playback_position,
          listOf("landscape", "playback", "play", "pause", "previous", "next", "seekbar", "above", "below", "center", "position"),
        ),
      )
      addSearchEntries(
        category = "Appearance",
        screen = ControlLayoutEditorScreen(ControlRegion.PORTRAIT_BOTTOM),
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.ui_portrait_seekbar_position, listOf("portrait", "seekbar", "position", "above", "below", "buttons")),
        SearchEntrySpec(
          R.string.pref_layout_portrait_playback_position,
          listOf("portrait", "playback", "play", "pause", "previous", "next", "seekbar", "above", "below", "center", "position"),
        ),
      )
      addSearchEntries(
        category = "Appearance",
        screen = PlayerControlsPreferencesScreen,
        anchorItemIndex = 7,
        SearchEntrySpec(R.string.pref_player_buttons_opacity, listOf("opacity", "transparency", "transparent", "alpha", "background", "buttons", "player")),
        SearchEntrySpec(R.string.ui_time_network_clock, listOf("clock", "time", "network", "format", "12 hour", "24 hour")),
      )

      addSearchEntries(
        category = "Player",
        screen = PlayerPreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_playlist_mode_title, listOf("playlist", "next", "previous", "navigation", "queue")),
        SearchEntrySpec(R.string.pref_enable_video_mini_player_title, listOf("mini player", "video", "background", "continue")),
        SearchEntrySpec(R.string.ui_show_media_info_in_chooser, listOf("media info", "chooser", "open with", "system")),
      )
      addSearchEntries(
        category = "Player",
        screen = PlayerPreferencesScreen,
        anchorItemIndex = 3,
        SearchEntrySpec(R.string.pref_player_show_buffered_range_title, listOf("buffer", "seekbar", "cache", "range")),
        SearchEntrySpec(R.string.pref_player_show_chapter_indicators_title, listOf("chapter", "seekbar", "markers", "indicator")),
        SearchEntrySpec(
          R.string.pref_custom_intro_keywords_enabled,
          listOf("custom", "intro", "opening", "keywords", "chapters"),
          R.string.pref_chapter_detect_title,
        ),
        SearchEntrySpec(
          R.string.pref_custom_intro_keywords_title,
          listOf("custom", "intro", "opening", "keywords", "chapters"),
          R.string.pref_chapter_detect_title,
        ),
        SearchEntrySpec(
          R.string.pref_custom_outro_keywords_enabled,
          listOf("custom", "outro", "ending", "keywords", "chapters"),
          R.string.pref_chapter_detect_title,
        ),
        SearchEntrySpec(
          R.string.pref_custom_outro_keywords_title,
          listOf("custom", "outro", "ending", "keywords", "chapters"),
          R.string.pref_chapter_detect_title,
        ),
      )
      addSearchEntries(
        category = "Player",
        screen = PlayerPreferencesScreen,
        anchorItemIndex = 5,
        SearchEntrySpec(R.string.pref_player_safe_area_window_title, listOf("safe area", "cutout", "notch", "insets", "window")),
      )
      addSearchEntries(
        category = "Player",
        screen = PlayerPreferencesScreen,
        anchorItemIndex = 9,
        SearchEntrySpec(R.string.pref_volume_overlay_title, listOf("overlay", "volume", "gesture", "slider")),
        SearchEntrySpec(R.string.pref_brightness_overlay_title, listOf("overlay", "brightness", "gesture", "slider")),
        SearchEntrySpec(R.string.pref_hold_speed_overlay_pref_title, listOf("overlay", "hold", "speed", "feedback")),
        SearchEntrySpec(R.string.pref_aspect_ratio_overlay_title, listOf("overlay", "aspect ratio", "feedback")),
        SearchEntrySpec(R.string.pref_zoom_overlay_title, listOf("overlay", "zoom", "level", "feedback")),
        SearchEntrySpec(R.string.pref_repeat_shuffle_overlay_title, listOf("overlay", "repeat", "shuffle", "feedback")),
        SearchEntrySpec(R.string.pref_action_feedback_overlay_title, listOf("overlay", "action", "feedback", "pills")),
        SearchEntrySpec(R.string.pref_provider_status_overlay_title, listOf("overlay", "provider", "status", "network")),
      )

      addSearchEntries(
        category = "Gestures",
        screen = GesturePreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_player_gestures_pinch_to_zoom_subtitles, listOf("pinch", "zoom", "subtitle", "gesture")),
        SearchEntrySpec(R.string.pref_player_gestures_swipe_subtitles_to_seek_dialog, listOf("swipe", "subtitle", "seek", "dialog")),
        SearchEntrySpec(R.string.pref_player_gestures_swipe_subtitles_invert_direction, listOf("swipe", "subtitle", "invert", "direction")),
        SearchEntrySpec(R.string.pref_player_gestures_enable_center_swipe_up_gesture, listOf("swipe up", "center", "playlist", "gesture")),
      )
      addSearchEntries(
        category = "Gestures",
        screen = GesturePreferencesScreen,
        anchorItemIndex = 3,
        SearchEntrySpec(R.string.pref_gesture_center_vertical_subtitle_position_title, listOf("hold", "drag", "subtitle", "position", "center")),
      )

      addSearchEntries(
        category = "Decoder",
        screen = DecoderPreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_decoder_profile_title, listOf("mpv", "profile", "fast", "quality", "decoder")),
        SearchEntrySpec(R.string.pref_decoder_gpu_next_enable_title, listOf("gpu-next", "vulkan", "decoder", "enable"), R.string.pref_decoder_gpu_next_title),
      )

      addSearchEntries(
        category = "Audio",
        screen = AudioPreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.ui_include_audio_files, listOf("audio", "files", "browser", "folders", "library")),
        SearchEntrySpec(R.string.ui_minimum_audio_duration, listOf("audio", "minimum", "duration", "short", "filter")),
      )
      addSearchEntries(
        category = "Audio",
        screen = AudioPreferencesScreen,
        anchorItemIndex = 3,
        SearchEntrySpec(R.string.pref_audio_ambient_mode_title, listOf("audio", "ambient", "background", "visualizer")),
        SearchEntrySpec(R.string.pref_audio_wavy_seekbar_title, listOf("audio", "wavy", "seekbar", "visualizer", "animation", "wave")),
        SearchEntrySpec(R.string.pref_audio_animated_artwork_title, listOf("audio", "music", "animated", "artwork", "album", "cover", "apple")),
      )
      addSearchEntries(
        category = "Audio",
        screen = AudioPreferencesScreen,
        anchorItemIndex = 5,
        SearchEntrySpec(R.string.pref_autoplay_next_audio_title, listOf("autoplay", "next", "audio", "music")),
        SearchEntrySpec(R.string.pref_audio_drc_title, listOf("audio", "dynamic range", "compression", "drc", "loudness")),
        SearchEntrySpec(R.string.pref_lyrics_auto_translate, listOf("lyrics", "translation", "automatic", "language")),
        SearchEntrySpec(R.string.pref_lyrics_target_language, listOf("lyrics", "translation", "target", "language")),
        SearchEntrySpec(R.string.pref_lyrics_display_mode, listOf("lyrics", "translation", "display", "style")),
      )

      addSearchEntries(
        category = "Subtitles",
        screen = SubtitlesPreferencesScreen,
        anchorItemIndex = 3,
        SearchEntrySpec(R.string.reload_fonts, listOf("subtitle", "font", "reload", "rescan")),
        SearchEntrySpec(R.string.clear_font_directory, listOf("subtitle", "font", "clear", "folder"), R.string.pref_subtitles_fonts_dir),
      )


      addSearchEntries(
        category = "Advanced",
        screen = AdvancedPreferencesScreen,
        anchorItemIndex = 9,
        SearchEntrySpec(R.string.pref_app_language_title, listOf("app", "language", "locale", "translation")),
      )
      addSearchEntries(
        category = "Advanced",
        screen = AdvancedPreferencesScreen,
        anchorItemIndex = 3,
        SearchEntrySpec(R.string.pref_clear_storage_root_title, listOf("storage", "root", "folder", "clear"), R.string.pref_advanced_mpv_conf_storage_location),
      )
      addSearchEntries(
        category = "Advanced",
        screen = AdvancedPreferencesScreen,
        anchorItemIndex = 5,
        SearchEntrySpec(R.string.pref_mpv_conf_overrides_title, listOf("mpv", "config", "override", "ownership")),
      )

      addSearchEntries(
        category = "Advanced",
        screen = CustomButtonScreen,
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.pref_custom_buttons_title, listOf("custom", "buttons", "lua", "player", "script"), R.string.pref_custom_lua_title),
      )



      addSearchEntries(
        category = "Appearance",
        screen = AppearancePreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_appearance_category_theme, listOf("theme", "dark", "light", "system", "dynamic")),
        SearchEntrySpec(R.string.pref_appearance_system_font_title, listOf("font", "system", "default"), R.string.pref_appearance_app_font_title),
        SearchEntrySpec(R.string.app_font_google_fonts_title, listOf("google", "font", "download", "typeface"), R.string.pref_appearance_app_font_title),
        SearchEntrySpec(R.string.app_font_google_sans_flex, listOf("google sans", "font", "built in"), R.string.pref_appearance_app_font_title),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_title, listOf("custom", "theme", "colors", "palette"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_name, listOf("custom", "theme", "name"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_light_primary, listOf("custom", "theme", "light", "primary"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_light_secondary, listOf("custom", "theme", "light", "secondary"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_light_tertiary, listOf("custom", "theme", "light", "tertiary"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_light_background, listOf("custom", "theme", "light", "background"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_dark_primary, listOf("custom", "theme", "dark", "primary"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_dark_secondary, listOf("custom", "theme", "dark", "secondary"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_dark_tertiary, listOf("custom", "theme", "dark", "tertiary"), R.string.pref_appearance_category_theme),
        SearchEntrySpec(R.string.pref_appearance_custom_theme_dark_background, listOf("custom", "theme", "dark", "background"), R.string.pref_appearance_category_theme),
      )
      addSearchEntries(
        category = "Appearance",
        screen = AppIconPreferencesScreen,
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.pref_app_icon_title, listOf("app", "launcher", "icon", "logo", "purple")),
        SearchEntrySpec(R.string.app_icon_section_logo, listOf("app", "icon", "logo", "mark"), R.string.pref_app_icon_title),
        SearchEntrySpec(R.string.app_icon_custom_background, listOf("app", "icon", "custom", "color", "background"), R.string.pref_app_icon_title),
        SearchEntrySpec(R.string.app_icon_use_wallpaper_colors, listOf("app", "icon", "wallpaper", "colors"), R.string.pref_app_icon_title),
        SearchEntrySpec(R.string.app_icon_create_custom_shortcut, listOf("app", "icon", "custom", "home", "shortcut"), R.string.pref_app_icon_title),
      )

      addSearchEntries(
        category = "Appearance",
        screen = PlayerControlsPreferencesScreen,
        anchorItemIndex = 7,
        SearchEntrySpec(R.string.pref_custom_time_label, listOf("custom", "time", "controls", "timeout"), R.string.pref_player_display_hide_player_control_time),
      )
      addSearchEntries(
        category = "Advanced",
        screen = CustomButtonScreen,
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.ui_button_title, listOf("custom", "button", "title", "name"), R.string.pref_custom_lua_title),
      )
      addSearchEntries(
        category = "Player",
        screen = PlayerPreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_player_web_stream_links_title, listOf("web", "stream", "links", "external", "browser")),
        SearchEntrySpec(R.string.pref_resume_indicator_overlay_title, listOf("resume", "indicator", "overlay", "position")),
      )
      addSearchEntries(
        category = "Gestures",
        screen = GesturePreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_gesture_show_seekbar_horizontal_title, listOf("seekbar", "horizontal", "swipe", "seek")),
        SearchEntrySpec(R.string.pref_gesture_single_tap_left_title, listOf("single", "tap", "left", "action"), R.string.pref_gesture_double_tap_left_title),
        SearchEntrySpec(R.string.pref_gesture_single_tap_center_title, listOf("single", "tap", "center", "action"), R.string.pref_gesture_double_tap_center_title),
        SearchEntrySpec(R.string.pref_gesture_single_tap_right_title, listOf("single", "tap", "right", "action"), R.string.pref_gesture_double_tap_right_title),
        SearchEntrySpec(R.string.pref_custom_seek_label, listOf("custom", "seek", "seconds", "double tap"), R.string.pref_player_double_tap_seek_duration),
      )
      addSearchEntries(
        category = "Advanced",
        screen = AdvancedPreferencesScreen,
        anchorItemIndex = 1,
        SearchEntrySpec(R.string.pref_advanced_notification_style, listOf("notification", "style", "media", "compact")),
        SearchEntrySpec(R.string.pref_auto_backup_title, listOf("automatic", "backup", "settings", "restore", "folder")),
      )
      addSearchEntries(
        category = "Folders",
        screen = FoldersPreferencesScreen,
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.pref_hidden_scan_per_mode_title, listOf("hidden", "folders", "scan", "nomedia")),
        SearchEntrySpec(R.string.pref_hidden_scan_library, listOf("hidden", "scan", "library"), R.string.pref_hidden_scan_per_mode_title),
        SearchEntrySpec(R.string.pref_hidden_scan_tree, listOf("hidden", "scan", "tree"), R.string.pref_hidden_scan_per_mode_title),
        SearchEntrySpec(R.string.pref_hidden_scan_folders, listOf("hidden", "scan", "folders"), R.string.pref_hidden_scan_per_mode_title),
        SearchEntrySpec(R.string.pref_folders_hidden_markers_title, listOf("hidden", "markers", "nomedia", "scan"), R.string.pref_hidden_scan_per_mode_title),
        SearchEntrySpec(R.string.pref_hidden_scan_dot, listOf("hidden", "dot", "folders", "scan"), R.string.pref_hidden_scan_per_mode_title),
        SearchEntrySpec(R.string.pref_folders_add_marker_title, listOf("hidden", "marker", "add", "nomedia"), R.string.pref_hidden_scan_per_mode_title),
        SearchEntrySpec(R.string.pref_folders_marker_name, listOf("hidden", "marker", "name", "nomedia"), R.string.pref_hidden_scan_per_mode_title),
      )

      addSearchEntries(
        category = "Folders",
        screen = SecureFolderSettingsScreen,
        anchorItemIndex = 0,
        SearchEntrySpec(R.string.secure_folder_settings_title, listOf("secure", "folder", "private", "vault")),
        SearchEntrySpec(R.string.secure_folder_biometric_title, listOf("secure", "folder", "biometric", "fingerprint")),
        SearchEntrySpec(R.string.secure_folder_reset_pin, listOf("secure", "folder", "pin", "password", "change")),
        SearchEntrySpec(R.string.secure_folder_reset_security_question, listOf("secure", "folder", "security", "question")),
        SearchEntrySpec(R.string.secure_folder_forgot_pin_title, listOf("secure", "folder", "forgot", "pin", "recover")),
        SearchEntrySpec(R.string.secure_folder_hide_settings, listOf("secure", "folder", "hide", "entry")),
        SearchEntrySpec(R.string.secure_folder_destroy_title, listOf("secure", "folder", "destroy", "delete", "reset")),
      )

      // About
      add(
        SearchablePreference(
          titleRes = R.string.pref_about_title,
          summaryRes = R.string.pref_about_summary,
          keywords = listOf("about", "version", "licenses", "acknowledgments", "info", "app"),
          category = "About",
          screen = AboutScreen,
        ),
      )
    }.distinctBy { Triple(it.screen, it.titleRes, it.title) }

  /**
   * Screen-level fallback terms for controls that do not need a dedicated result card. This keeps
   * every settings area discoverable without duplicating each Compose control in two registries.
   * Dedicated entries above still win whenever they match.
   */
  private val screenFallbackPreferences: List<SearchablePreference> =
    listOf(
      SearchablePreference(
        titleRes = R.string.pref_appearance_title,
        summaryRes = R.string.pref_appearance_summary,
        keywords =
          listOf(
            "theme dark light system dynamic amoled font names labels thumbnails frame quality position",
            "navigation home profile network quick play fab auto scroll",
            "grid columns list folder cards video cards chips path extension duration resolution framerate subtitle",
          ),
        category = "Appearance",
        screen = AppearancePreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_player,
        keywords =
          listOf(
            "orientation speed background playback mini player close end eof notification media info pip screen unlock",
            "seeking precise buffered chapters brightness volume zoom pan system bars safe area controls timeout clock",
            "playlist repeat shuffle intro outro skip overlays animations",
          ),
        category = "Player",
        screen = PlayerPreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_decoder,
        summaryRes = R.string.pref_decoder_summary,
        keywords =
          listOf(
            "profile hardware software decoding mediacodec gpu next vulkan hdr sdr yuv420p",
            "deband iterations threshold range grain brightness saturation gamma contrast hue sharpness",
          ),
        category = "Decoder",
        screen = DecoderPreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_audio,
        summaryRes = R.string.pref_audio_summary,
        keywords =
          listOf(
            "preferred language delay pitch correction channels volume boost normalization drc",
            "audio player background playback orientation ambient wavy seekbar minimum duration",
          ),
        category = "Audio",
        screen = AudioPreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_gesture,
        summaryRes = R.string.pref_gesture_summary,
        keywords =
          listOf(
            "double tap seek area left center right single tap media previous play pause next custom key",
            "subtitle pinch zoom swipe invert direction playlist center swipe thumbnail select",
          ),
        category = "Gestures",
        screen = GesturePreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_layout_title,
        summaryRes = R.string.pref_layout_summary,
        keywords =
          listOf(
            "control layout editor buttons top left top right bottom left bottom right portrait landscape",
            "seekbar wavy custom button script icon action long press position reset",
          ),
        category = "Appearance",
        screen = PlayerControlsPreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_subtitles,
        summaryRes = R.string.pref_subtitles_summary,
        keywords =
          listOf(
            "preferred language autoload auto enable font folder size scale border bold italic colors shadow background justification position",
            "ass override window blend delay speed save folder download online search subtitle hub wyzie sources formats encodings hearing impaired api key",
          ),
        category = "Subtitles",
        screen = SubtitlesPreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_folders_title,
        summaryRes = R.string.pref_folders_summary,
        keywords =
          listOf(
            "folder blacklist exclude hidden audio video nomedia scan add remove clear storage pinned",
          ),
        category = "Folders",
        screen = FoldersPreferencesScreen,
      ),

      SearchablePreference(
        titleRes = R.string.pref_advanced,
        keywords =
          listOf(
            "language backup restore storage folder mpv config input conf lua scripts logs verbose history cache fonts",
            "network streaming cookies proxy user agent configuration editor",
          ),
        category = "Advanced",
        screen = AdvancedPreferencesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_codecs_title,
        summaryRes = R.string.pref_codecs_summary,
        keywords = listOf("codec capability hardware software av1 hevc h264 vp9 audio video mime device decoder report"),
        category = "Codecs",
        screen = CodecCapabilitiesScreen,
      ),
      SearchablePreference(
        titleRes = R.string.pref_about_title,
        summaryRes = R.string.pref_about_summary,
        keywords = listOf("version update auto update changelog release license open source libraries github privacy about"),
        category = "About",
        screen = AboutScreen,
      ),
    )

  private data class SearchField(
    val value: String,
    val weight: Int,
  )

  private val playStoreHiddenTitles =
    setOf(
      R.string.pref_player_web_stream_links_title,
      R.string.ui_yt_dlp_manager,
      R.string.ytdlp_subtitles_language,
      R.string.ytdlp_playlist_behavior,
      R.string.pref_lyrics_auto_translate,
      R.string.pref_lyrics_target_language,
      R.string.pref_lyrics_display_mode,
      R.string.pref_paxsenix_api_key,
    )

  private val excludedSearchScreens: Set<Screen> = emptySet()

  // Play builds omit AI, P2P and lyrics translation, so their settings must not surface.
  // Network, Media Servers, and AI Integration sections are removed from settings.
  private fun SearchablePreference.isAvailable(): Boolean =
    screen !in excludedSearchScreens &&
      (!app.video.player.BuildConfig.IS_PLAY_STORE_BUILD ||
        titleRes?.let(playStoreHiddenTitles::contains) != true)

  internal fun forScreens(screens: Set<Screen>): List<SearchablePreference> =
    (staticPreferences + screenFallbackPreferences)
      .filter { it.screen in screens && it.isAvailable() }
      .distinctBy { it.screen to it.searchTargetKey }

  fun search(
    query: String,
    includeSecureFolder: Boolean = true,
    television: Boolean = false,
    screens: Set<Screen>? = null,
    getStringRes: (Int) -> String,
  ): List<SettingsSearchResult> {
    if (query.isBlank()) return emptyList()

    val normalizedQuery = normalizeSearchText(query)
    if (normalizedQuery.isBlank()) return emptyList()

    fun available(preference: SearchablePreference): Boolean =
      (screens == null || preference.screen in screens) &&
        preference.isAvailable() && (includeSecureFolder || preference.screen != SecureFolderSettingsScreen) &&
        (!television || TvSettingsPolicy.supportsSearch(preference))

    val directResults =
      staticPreferences
        .mapIndexedNotNull { index, preference ->
          val entry = if (television) TvSettingsPolicy.searchEntry(preference) else preference
          entry.takeIf(::available)?.match(normalizedQuery, getStringRes, index)
        }.sortedWith(
          compareByDescending<SettingsSearchResult> { it.score }
            .thenBy { it.preference.category }
            .thenBy { result ->
              result.preference.titleRes?.let(getStringRes) ?: result.preference.title.orEmpty()
            },
        )
    if (directResults.isNotEmpty()) return directResults

    return screenFallbackPreferences
      .mapIndexedNotNull { index, preference ->
        val entry = if (television) TvSettingsPolicy.searchEntry(preference) else preference
        entry.takeIf(::available)?.match(normalizedQuery, getStringRes, staticPreferences.size + index)
      }.sortedByDescending(SettingsSearchResult::score)
  }

  private fun SearchablePreference.match(
    normalizedQuery: String,
    getStringRes: (Int) -> String,
    index: Int,
  ): SettingsSearchResult? {
    val resolvedTitle = titleRes?.let(getStringRes) ?: title.orEmpty()
    val fields =
      listOf(
        SearchField(normalizeSearchText(resolvedTitle), 8),
        SearchField(normalizeSearchText(keywords.joinToString(" ")), 4),
        SearchField(normalizeSearchText(summaryRes?.let(getStringRes) ?: summary.orEmpty()), 2),
        SearchField(normalizeSearchText(category), 1),
        SearchField(normalizeSearchText(getStringRes(searchCategoryTitleRes(category))), 1),
      )
    val queryTokens = normalizedQuery.split(' ').filter(String::isNotBlank)
    val tokenScores =
      queryTokens.map { token ->
        fields.maxOfOrNull { field -> fieldMatchScore(token, field.value) * field.weight } ?: 0
      }
    if (tokenScores.any { it <= 0 }) return null

    val phraseScore = fields.maxOfOrNull { field -> fieldMatchScore(normalizedQuery, field.value) * field.weight } ?: 0
    val score = phraseScore * 10 + tokenScores.sum() * 2 - index.coerceAtMost(500)
    return SettingsSearchResult(
      preference = this,
      titleMatchIndices = titleMatchIndices(normalizedQuery, resolvedTitle),
      score = score,
    )
  }

  private fun fieldMatchScore(
    query: String,
    candidate: String,
  ): Int {
    if (query.isBlank() || candidate.isBlank()) return 0
    if (candidate == query) return 1_000
    if (candidate.startsWith(query)) return 900 - (candidate.length - query.length).coerceAtMost(100)

    val words = candidate.split(' ').filter(String::isNotBlank)
    if (query in words) return 850
    words.indexOfFirst { it.startsWith(query) }.takeIf { it >= 0 }?.let { wordIndex ->
      return 800 - wordIndex.coerceAtMost(50)
    }

    val substringIndex = candidate.indexOf(query)
    if (substringIndex >= 0) return 700 - substringIndex.coerceAtMost(100)

    val compactQuery = query.replace(" ", "")
    val compactCandidate = candidate.replace(" ", "")
    fuzzySubsequenceScore(compactQuery, compactCandidate)?.let { return 400 + it }

    if (query.length >= 4) {
      val closestDistance = words.minOfOrNull { word -> editDistance(query, word, 2) } ?: Int.MAX_VALUE
      if (closestDistance <= 2) return 300 - closestDistance * 50
    }
    return 0
  }

  private fun fuzzySubsequenceScore(
    query: String,
    candidate: String,
  ): Int? {
    if (query.isEmpty() || candidate.isEmpty() || query.length > candidate.length) return null
    var candidateIndex = 0
    var previousMatch = -1
    var score = 0
    for (queryCharacter in query) {
      val matchIndex = candidate.indexOf(queryCharacter, candidateIndex)
      if (matchIndex < 0) return null
      score += if (previousMatch >= 0 && matchIndex == previousMatch + 1) 12 else 5
      score -= (matchIndex - candidateIndex).coerceAtMost(8)
      previousMatch = matchIndex
      candidateIndex = matchIndex + 1
    }
    return score - (candidate.length - query.length).coerceAtMost(80)
  }

  private fun editDistance(
    first: String,
    second: String,
    limit: Int,
  ): Int {
    if (abs(first.length - second.length) > limit) return limit + 1
    var previous = IntArray(second.length + 1) { it }
    for (firstIndex in first.indices) {
      val current = IntArray(second.length + 1)
      current[0] = firstIndex + 1
      var rowMinimum = current[0]
      for (secondIndex in second.indices) {
        current[secondIndex + 1] =
          min(
            min(current[secondIndex] + 1, previous[secondIndex + 1] + 1),
            previous[secondIndex] + if (first[firstIndex] == second[secondIndex]) 0 else 1,
          )
        rowMinimum = min(rowMinimum, current[secondIndex + 1])
      }
      if (rowMinimum > limit) return limit + 1
      previous = current
    }
    return previous.last()
  }

  private fun titleMatchIndices(
    normalizedQuery: String,
    title: String,
  ): Set<Int> {
    val (normalizedTitle, originalIndices) = normalizeSearchTextWithIndices(title)
    if (normalizedTitle.isBlank()) return emptySet()

    val matched = linkedSetOf<Int>()
    normalizedQuery.split(' ').filter(String::isNotBlank).forEach { token ->
      val substringIndex = normalizedTitle.indexOf(token)
      if (substringIndex >= 0) {
        repeat(token.length) { offset -> originalIndices.getOrNull(substringIndex + offset)?.let(matched::add) }
      } else {
        fuzzySubsequenceIndices(token, normalizedTitle)
          ?.mapNotNull(originalIndices::getOrNull)
          ?.let(matched::addAll)
      }
    }
    return matched
  }

  private fun fuzzySubsequenceIndices(
    query: String,
    candidate: String,
  ): List<Int>? {
    var searchFrom = 0
    return buildList {
      query.forEach { character ->
        val matchIndex = candidate.indexOf(character, searchFrom)
        if (matchIndex < 0) return null
        add(matchIndex)
        searchFrom = matchIndex + 1
      }
    }
  }

  private fun normalizeSearchText(value: String): String = normalizeSearchTextWithIndices(value).first

  private fun normalizeSearchTextWithIndices(value: String): Pair<String, List<Int>> {
    val output = StringBuilder(value.length)
    val originalIndices = mutableListOf<Int>()
    value.forEachIndexed { index, character ->
      val normalized =
        Normalizer
          .normalize(character.toString(), Normalizer.Form.NFD)
          .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
          .lowercase()
      normalized.forEach { normalizedCharacter ->
        val searchableCharacter = if (normalizedCharacter.isLetterOrDigit()) normalizedCharacter else ' '
        if (searchableCharacter == ' ' && output.lastOrNull() == ' ') return@forEach
        output.append(searchableCharacter)
        originalIndices += index
      }
    }
    val rawOutput = output.toString()
    val firstContentIndex = rawOutput.indexOfFirst { it != ' ' }
    if (firstContentIndex < 0) return "" to emptyList()
    val lastContentIndex = rawOutput.indexOfLast { it != ' ' }
    return rawOutput.substring(firstContentIndex, lastContentIndex + 1) to
      originalIndices.subList(firstContentIndex, lastContentIndex + 1)
  }
}
