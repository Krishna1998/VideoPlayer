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
import app.video.player.domain.torrent.TorrentStreamingEngine
import app.video.player.network.AndroidCookieJar
import app.video.player.network.SharedHttpClient
import app.video.player.preferences.AiPreferences
import app.video.player.repository.IntroDbRepository
import app.video.player.repository.ai.AiClient
import app.video.player.repository.ai.AiService
import app.video.player.repository.ai.AnthropicClient
import app.video.player.repository.ai.GroqClient
import app.video.player.repository.ai.GroqSpeechClient
import app.video.player.repository.ai.OpenAiClient
import app.video.player.repository.ai.OpenCodeClient
import app.video.player.repository.ai.OpenRouterClient
import app.video.player.repository.ai.OpenRouterSpeechClient
import app.video.player.repository.ai.RealtimeSubtitleService
import app.video.player.repository.ai.SubtitleGenerationService
import app.video.player.repository.ai.TogetherClient
import app.video.player.repository.subtitle.OnlineSubtitleFileStore
import app.video.player.repository.subtitle.OnlineSubtitleOrchestrator
import app.video.player.repository.subtitlehub.MpvRxSubtitleHubRepository
import app.video.player.repository.wyzie.WyzieSearchRepository
import app.video.player.ui.player.MpvConfigCache
import app.video.player.ui.player.PlaybackSessionShaderRuntime
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
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
    single { MpvRxSubtitleHubRepository(get(), get(), get(), get()) }
    single { OnlineSubtitleOrchestrator(get<WyzieSearchRepository>(), get<MpvRxSubtitleHubRepository>()) }
    single { IntroDbRepository(get(), get(), androidContext()) }
    single { OpenCodeClient(get(), get()) }
    single { GroqClient(get(), get()) }
    single { OpenAiClient(get(), get()) }
    single { AnthropicClient(get(), get()) }
    single { OpenRouterClient(get(), get()) }
    single { TogetherClient(get(), get()) }
    single { GroqSpeechClient(get(), get()) }
    single { OpenRouterSpeechClient(get(), get()) }
    single<AiClient>(named("opencode")) { OpenCodeClient(get(), get()) }
    single<AiClient>(named("groq")) { GroqClient(get(), get()) }
    single<AiClient>(named("openai")) { OpenAiClient(get(), get()) }
    single<AiClient>(named("anthropic")) { AnthropicClient(get(), get()) }
    single<AiClient>(named("openrouter")) { OpenRouterClient(get(), get()) }
    single<AiClient>(named("together")) { TogetherClient(get(), get()) }
    single { SubtitleGenerationService(androidContext(), get(), get(), get(), get(), get()) }
    single { RealtimeSubtitleService(androidContext(), get(), get(), get(), get(), get(), get()) }
    single {
      AiService(
        androidContext(),
        get<AiPreferences>(),
        get<AiClient>(named("opencode")),
        get<AiClient>(named("groq")),
        get<AiClient>(named("openai")),
        get<AiClient>(named("anthropic")),
        get<AiClient>(named("openrouter")),
        get<AiClient>(named("together")),
        get<Json>(),
      )
    }
    single {
      app.video.player.domain.syncplay
        .SyncplayManager(androidContext())
    }
    single { app.video.player.data.lyrics.LrcLibApiService(get()) }
    single { app.video.player.data.artwork.AppleMusicArtworkRepository(get()) }
    single { app.video.player.data.lyrics.LyricsTranslationService(get()) }
    single { app.video.player.repository.lyrics.LyricsRepository(androidContext(), get(), get()) }
    single { TorrentStreamingEngine(androidContext()) }
    single { app.video.player.repository.SeerrRepository(get(), get(), get()) }
  }
