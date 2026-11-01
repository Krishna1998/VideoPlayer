/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.video.player.database.converters.NetworkProtocolConverter
import app.video.player.database.converters.NetworkStreamEntryTypeConverter
import app.video.player.database.dao.AudiobookDao
import app.video.player.database.dao.AudiobookshelfServerDao
import app.video.player.database.dao.DirectoryScanDao
import app.video.player.database.dao.DownloadItemDao
import app.video.player.database.dao.FrameCaptureDao
import app.video.player.database.dao.NetworkConnectionDao
import app.video.player.database.dao.NetworkStreamEntryDao
import app.video.player.database.dao.PlaybackStateDao
import app.video.player.database.dao.PlaybackBookmarkDao
import app.video.player.database.dao.PlaylistDao
import app.video.player.database.dao.RecentlyPlayedDao
import app.video.player.database.dao.SecureMediaDao
import app.video.player.database.dao.SnapshotFolderDao
import app.video.player.database.dao.VideoMetadataDao
import app.video.player.database.dao.JellyfinServerDao
import app.video.player.database.dao.NavidromeServerDao
import app.video.player.database.entities.AudiobookshelfServerEntity
import app.video.player.database.entities.DirectoryScanEntity
import app.video.player.database.entities.AudiobookEntity
import app.video.player.database.entities.AudiobookTrackEntity
import app.video.player.database.entities.AudiobookChapterEntity
import app.video.player.database.entities.DownloadItemEntity
import app.video.player.database.entities.FrameCaptureEntity
import app.video.player.database.entities.JellyfinServerEntity
import app.video.player.database.entities.NavidromeServerEntity
import app.video.player.database.entities.NetworkStreamEntryEntity
import app.video.player.database.entities.PlaybackStateEntity
import app.video.player.database.entities.PlaybackBookmarkEntity
import app.video.player.database.entities.PlaylistEntity
import app.video.player.database.entities.PlaylistItemEntity
import app.video.player.database.entities.RecentlyPlayedEntity
import app.video.player.database.entities.SecureMediaEntity
import app.video.player.database.entities.SnapshotFolderEntity
import app.video.player.database.entities.VideoMetadataEntity
import app.video.player.domain.network.NetworkConnection

@Database(
  entities = [
    PlaybackStateEntity::class,
    RecentlyPlayedEntity::class,
    VideoMetadataEntity::class,
    NetworkConnection::class,
    PlaylistEntity::class,
    PlaylistItemEntity::class,
    DirectoryScanEntity::class,
    SecureMediaEntity::class,
    NetworkStreamEntryEntity::class,
    JellyfinServerEntity::class,
    DownloadItemEntity::class,
    NavidromeServerEntity::class,
    AudiobookEntity::class,
    AudiobookTrackEntity::class,
    AudiobookChapterEntity::class,
    PlaybackBookmarkEntity::class,
    AudiobookshelfServerEntity::class,
    FrameCaptureEntity::class,
    SnapshotFolderEntity::class,
  ],
  version = 31,
  exportSchema = true,
)
@TypeConverters(NetworkProtocolConverter::class, NetworkStreamEntryTypeConverter::class)
abstract class MpvRxDatabase : RoomDatabase() {
  abstract fun videoDataDao(): PlaybackStateDao

  abstract fun recentlyPlayedDao(): RecentlyPlayedDao

  abstract fun videoMetadataDao(): VideoMetadataDao

  abstract fun networkConnectionDao(): NetworkConnectionDao

  abstract fun networkStreamEntryDao(): NetworkStreamEntryDao

  abstract fun playlistDao(): PlaylistDao

  abstract fun directoryScanDao(): DirectoryScanDao

  abstract fun secureMediaDao(): SecureMediaDao

  abstract fun jellyfinServerDao(): JellyfinServerDao

  abstract fun downloadItemDao(): DownloadItemDao

  abstract fun navidromeServerDao(): NavidromeServerDao

  abstract fun audiobookDao(): AudiobookDao

  abstract fun playbackBookmarkDao(): PlaybackBookmarkDao

  abstract fun audiobookshelfServerDao(): AudiobookshelfServerDao

  abstract fun frameCaptureDao(): FrameCaptureDao

  abstract fun snapshotFolderDao(): SnapshotFolderDao
}
