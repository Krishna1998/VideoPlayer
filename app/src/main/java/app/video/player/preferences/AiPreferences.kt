/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.preferences

import app.video.player.BuildConfig
import app.video.player.preferences.preference.Preference
import app.video.player.preferences.preference.PreferenceStore
import app.video.player.preferences.preference.getEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

enum class AiProvider(
  val displayName: String,
) {
  GROQ("Groq"),
  GEMINI("Gemini"),
}

/** Always off and ignores restored values, so a distribution can omit a feature outright. */
private class DisabledPreference(
  private val key: String,
) : Preference<Boolean> {
  override fun key(): String = key

  override fun get(): Boolean = false

  override fun set(value: Boolean) = Unit

  override fun isSet(): Boolean = false

  override fun delete() = Unit

  override fun defaultValue(): Boolean = false

  override fun changes(): Flow<Boolean> = flowOf(false)

  override fun stateIn(scope: CoroutineScope): StateFlow<Boolean> = MutableStateFlow(false)
}

class AiPreferences(
  preferenceStore: PreferenceStore,
) {
  // Play requires in-app reporting of AI output, which mpvRx does not provide.
  val enabled: Preference<Boolean> =
    if (BuildConfig.IS_PLAY_STORE_BUILD) {
      DisabledPreference("ai_enabled")
    } else {
      preferenceStore.getBoolean("ai_enabled", false)
    }

  val provider = preferenceStore.getEnum("ai_provider", AiProvider.GROQ)

  val groqApiKey = preferenceStore.getString("ai_groq_api_key", "")
  val geminiApiKey = preferenceStore.getString("ai_gemini_api_key", "")

  val selectedModel = preferenceStore.getString("ai_selected_model", "")

  val availableModels = preferenceStore.getString("ai_available_models", "[]")

  private val groqSelectedModel = preferenceStore.getString("ai_selected_model_groq", "")
  private val geminiSelectedModel = preferenceStore.getString("ai_selected_model_gemini", "")

  private val groqAvailableModels = preferenceStore.getString("ai_available_models_groq", "[]")
  private val geminiAvailableModels = preferenceStore.getString("ai_available_models_gemini", "[]")

  // Auto-translate target languages (comma-separated codes: "en,hi")
  val autoTranslateLanguages = preferenceStore.getString("ai_auto_translate_languages", "en,hi")

  val customPromptEnabled = preferenceStore.getBoolean("ai_custom_prompt_enabled", false)
  val customPrompt = preferenceStore.getString("ai_custom_prompt", "")
  val customRenamePrompt = preferenceStore.getString("ai_custom_rename_prompt", "")
  val customSubtitleTranslationPrompt = preferenceStore.getString("ai_custom_subtitle_translation_prompt", "")
  val customSubtitleFormatPrompt = preferenceStore.getString("ai_custom_subtitle_format_prompt", "")

  val renameWithAi = preferenceStore.getBoolean("ai_rename_enabled", true)
  val subtitleFormatWithAi = preferenceStore.getBoolean("ai_subtitle_format_enabled", true)
  val subtitleTranslationEnabled = preferenceStore.getBoolean("ai_subtitle_translation_enabled", false)
  val subtitleTranslationFirstTime = preferenceStore.getBoolean("ai_subtitle_translation_first_time", true)

  val lastVerified = preferenceStore.getLong("ai_last_verified", 0L)

  init {
    val currentProvider = provider.get()
    val providerModel = selectedModelFor(currentProvider)
    if (providerModel.get().isBlank() && selectedModel.get().isNotBlank()) {
      providerModel.set(selectedModel.get())
    }
    val providerModels = availableModelsFor(currentProvider)
    if (providerModels.get() == "[]" && availableModels.get() != "[]") {
      providerModels.set(availableModels.get())
    }
  }

  fun selectedModelFor(provider: AiProvider): Preference<String> =
    when (provider) {
      AiProvider.GROQ -> groqSelectedModel
      AiProvider.GEMINI -> geminiSelectedModel
    }

  fun availableModelsFor(provider: AiProvider): Preference<String> =
    when (provider) {
      AiProvider.GROQ -> groqAvailableModels
      AiProvider.GEMINI -> geminiAvailableModels
    }
}
