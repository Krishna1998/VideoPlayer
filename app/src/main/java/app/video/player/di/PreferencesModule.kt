/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.di

import app.video.player.preferences.AdvancedPreferences
import app.video.player.preferences.AppearancePreferences
import app.video.player.preferences.AudioPreferences
import app.video.player.preferences.BrowserPreferences
import app.video.player.preferences.DecoderPreferences
import app.video.player.preferences.DownloadPreferences
import app.video.player.preferences.FoldersPreferences
import app.video.player.preferences.GesturePreferences
import app.video.player.preferences.MediaServerPreferences
import app.video.player.preferences.NetworkBookmarkPreferences
import app.video.player.preferences.PlayerPreferences
import app.video.player.preferences.SecureFolderPreferences
import app.video.player.preferences.SeerrPreferences
import app.video.player.preferences.SettingsManager
import app.video.player.preferences.SubtitlesPreferences
import app.video.player.preferences.YtdlPreferences
import app.video.player.preferences.preference.AndroidPreferenceStore
import app.video.player.preferences.preference.PreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val PreferencesModule =
  module {
    single { AndroidPreferenceStore(androidContext()) }.bind(PreferenceStore::class)

    single { AppearancePreferences(get(), androidContext()) }
    singleOf(::PlayerPreferences)
    singleOf(::GesturePreferences)
    singleOf(::DecoderPreferences)
    singleOf(::SubtitlesPreferences)
    singleOf(::AudioPreferences)
    singleOf(::AdvancedPreferences)
    single { BrowserPreferences(get(), androidContext()) }
    singleOf(::FoldersPreferences)
    singleOf(::NetworkBookmarkPreferences)
    singleOf(::YtdlPreferences)
    singleOf(::SettingsManager)
    singleOf(::SecureFolderPreferences)
    singleOf(::SeerrPreferences)
    singleOf(::MediaServerPreferences)
    singleOf(::DownloadPreferences)
  }
