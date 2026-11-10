/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.data.network.client

import android.net.Uri
import app.video.player.domain.network.NetworkConnection
import app.video.player.domain.network.NetworkFile
import java.io.IOException
import java.io.InputStream

class WebDavClient(
  private val connection: NetworkConnection,
) : NetworkClient {
  override suspend fun connect(): Result<Unit> =
    Result.failure(IOException("WebDAV is not supported in this lightweight build"))

  override suspend fun disconnect() {}

  override fun isConnected(): Boolean = false

  override suspend fun listFiles(path: String): Result<List<NetworkFile>> =
    Result.failure(IOException("WebDAV is not supported in this lightweight build"))

  override suspend fun getFileStream(
    path: String,
    offset: Long,
  ): Result<InputStream> =
    Result.failure(IOException("WebDAV is not supported in this lightweight build"))

  override suspend fun getFileUri(path: String): Result<Uri> =
    Result.failure(IOException("WebDAV is not supported in this lightweight build"))
}
