/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.video.player.domain.anime4k

import android.content.Context
import java.io.File

/**
 * Anime4K Manager stub - Anime4K shaders removed to reduce app size.
 */
class Anime4KManager(
  private val context: Context,
) {
  companion object {
    val BUILT_IN_SHADER_FILES: Set<String> = emptySet()
    val DEFAULT_QUALITY = Quality.BALANCED
  }

  enum class Quality(
    val suffix: String,
    val titleRes: Int,
  ) {
    FAST("S", app.video.player.R.string.anime4k_quality_fast),
    BALANCED("M", app.video.player.R.string.anime4k_quality_balanced),
    HIGH("L", app.video.player.R.string.anime4k_quality_high),
  }

  enum class Mode(
    val titleRes: Int,
  ) {
    OFF(app.video.player.R.string.anime4k_mode_off),
    A(app.video.player.R.string.anime4k_mode_a),
    B(app.video.player.R.string.anime4k_mode_b),
    C(app.video.player.R.string.anime4k_mode_c),
    A_PLUS(app.video.player.R.string.anime4k_mode_a_plus),
    B_PLUS(app.video.player.R.string.anime4k_mode_b_plus),
    C_PLUS(app.video.player.R.string.anime4k_mode_c_plus),
    ARTCNN(app.video.player.R.string.anime4k_mode_artcnn);

    fun isEnabled(): Boolean = this != OFF
  }

  var isInitialized: Boolean = true
    private set

  fun setPostFilters(
    darken: Boolean = false,
    thin: Boolean = false,
    deblur: Boolean = false,
  ) {}

  fun initialize(): Boolean = false

  fun copyShadersFromAssets(): Boolean = true

  fun getShaderChain(
    mode: Mode,
    quality: Quality = DEFAULT_QUALITY,
  ): String = ""

  fun getShaderPaths(mode: Mode): List<String> = emptyList()

  fun getShaderPaths(mode: Mode, quality: Quality): List<String> = emptyList()

  fun getShaderFiles(mode: Mode, quality: Quality): List<File> = emptyList()

  fun getShadersForMode(
    mode: Mode,
    quality: Quality = Quality.BALANCED,
    darkenLines: Boolean = false,
    thinLines: Boolean = false,
    deblur: Boolean = false,
  ): List<String> = emptyList()

  fun isShaderSupported(): Boolean = false

  fun isAnime4KSupported(): Boolean = false

  fun hasInstalledShaders(): Boolean = false

  fun isHighResolutionVideo(width: Int, height: Int): Boolean = false

  fun getEffectiveQuality(width: Int, height: Int, selectedQuality: Quality): Quality = selectedQuality

  fun cleanupLegacyFiles() {}
}
