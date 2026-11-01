/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.data.network.client

import app.video.player.domain.network.NetworkConnection
import app.video.player.domain.network.NetworkProtocol
import app.video.player.domain.network.normalizedAddress

object NetworkClientFactory {
  fun createClient(rawConnection: NetworkConnection): NetworkClient {
    val connection = rawConnection.normalizedAddress()
    return when (connection.protocol) {
      NetworkProtocol.SMB -> SmbClient(connection)
      NetworkProtocol.FTP -> FtpClient(connection)
      NetworkProtocol.WEBDAV -> WebDavClient(connection)
      NetworkProtocol.SFTP -> SftpClient(connection)
    }
  }
}
