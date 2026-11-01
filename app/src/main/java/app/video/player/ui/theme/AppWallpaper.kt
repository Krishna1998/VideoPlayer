/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.video.player.preferences.AppearancePreferences
import app.video.player.preferences.preference.collectAsState
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.koin.compose.koinInject

val LocalAppWallpaperActive = compositionLocalOf { false }
internal val LocalAppBackgroundBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalLightTextOnWallpaper = compositionLocalOf { false }

@Composable
@androidx.compose.runtime.ReadOnlyComposable
fun Color.onWallpaper(): Color = this

enum class WallpaperScaleMode {
  Fit,
  Fill,
}

const val WallpaperDerivedThemeName = "Wallpaper"

@Composable
fun AppWallpaperHost(content: @Composable () -> Unit) {
  val preferences = koinInject<AppearancePreferences>()
  val liquidGlassEnabled by preferences.liquidGlassEnabled.collectAsState()
  val backgroundBackdrop = if (liquidGlassEnabled) rememberLayerBackdrop() else null

  Box(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier =
        Modifier
          .matchParentSize()
          .then(if (backgroundBackdrop != null) Modifier.layerBackdrop(backgroundBackdrop) else Modifier)
          .background(MaterialTheme.colorScheme.background),
    )
    CompositionLocalProvider(
      LocalAppWallpaperActive provides false,
      LocalAppBackgroundBackdrop provides backgroundBackdrop,
      LocalLightTextOnWallpaper provides false,
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        content()
      }
    }
  }
}

@Composable
fun wallpaperAwareBackgroundColor(): Color = MaterialTheme.colorScheme.background

suspend fun saveWallpaperCopy(
  context: android.content.Context,
  sourceUri: String,
): String = ""
