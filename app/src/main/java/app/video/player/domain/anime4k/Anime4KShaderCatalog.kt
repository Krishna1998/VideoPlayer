/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.video.player.domain.anime4k

/** Single source of truth for the Anime4K shaders bundled with the app. */
object Anime4KShaderCatalog {
  const val ASSET_DIRECTORY = "shaders/anime4k"
  const val INSTALL_DIRECTORY = "shaders/anime4k"

  val requiredFiles: List<String> = emptyList()
  val requiredFileSet: Set<String> = emptySet()
}
