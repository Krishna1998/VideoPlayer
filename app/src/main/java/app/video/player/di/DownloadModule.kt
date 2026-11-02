/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.di

import app.video.player.domain.download.AppDownloadManager
import app.video.player.domain.download.DownloadLocations
import app.video.player.domain.download.DownloadOutputReservations
import app.video.player.domain.download.LinkDownloadCoordinator
import app.video.player.domain.download.YtdlpDownloadEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DownloadModule =
  module {
    single { DownloadLocations(androidContext(), get()) }
    single { DownloadOutputReservations(get<app.video.player.database.VideoPlayerDatabase>().downloadItemDao()) }
    single { AppDownloadManager(androidContext(), get<app.video.player.database.VideoPlayerDatabase>().downloadItemDao(), get(), get()) }
    single { YtdlpDownloadEngine(androidContext(), get(), get()) }
    single { LinkDownloadCoordinator(get(), get()) }
  }
