/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.di

import app.video.player.domain.anime4k.Anime4KManager
import app.video.player.domain.fonts.GoogleFontsRepository
import app.video.player.domain.hdr.HdrToysManager
import app.video.player.domain.hdr.MpvShaderRuntime
import app.video.player.network.AndroidCookieJar
import app.video.player.network.SharedHttpClient
import app.video.player.repository.IntroDbRepository
import app.video.player.repository.subtitle.OnlineSubtitleFileStore
import app.video.player.repository.subtitle.OnlineSubtitleOrchestrator
import app.video.player.repository.subtitlehub.VideoPlayerSubtitleHubRepository
import app.video.player.repository.wyzie.WyzieSearchRepository
import app.video.player.ui.player.MpvConfigCache
import app.video.player.ui.player.PlaybackSessionShaderRuntime
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val domainModule =
  module {
    single { AndroidCookieJar() }
    single<OkHttpClient> {
      SharedHttpClient.derive {
        connectTimeout(30, TimeUnit.SECONDS)
        cookieJar(get<AndroidCookieJar>())
      }
    }
    single { Anime4KManager(androidContext()) }
    single<MpvShaderRuntime> { PlaybackSessionShaderRuntime }
    single { MpvConfigCache(androidContext(), get()) }
    single { HdrToysManager(androidContext(), get()) }
    single { GoogleFontsRepository(androidContext(), get()) }
    single { OnlineSubtitleFileStore(androidContext(), get()) }
    single { WyzieSearchRepository(androidContext(), get(), get(), get(), get()) }
    single { VideoPlayerSubtitleHubRepository(get(), get(), get(), get()) }
    single { OnlineSubtitleOrchestrator(get<WyzieSearchRepository>(), get<VideoPlayerSubtitleHubRepository>()) }
    single { IntroDbRepository(get(), get(), androidContext()) }
    single {
      app.video.player.domain.syncplay
        .SyncplayManager(androidContext())
    }
    single { app.video.player.repository.SeerrRepository(get(), get(), get()) }
  }
