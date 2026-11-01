package app.video.player.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.video.player.database.entities.PlaybackBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackBookmarkDao {
  @Query("SELECT * FROM playback_bookmarks WHERE mediaId = :mediaId ORDER BY positionMs, createdAt")
  fun observe(mediaId: String): Flow<List<PlaybackBookmarkEntity>>

  @Insert
  suspend fun add(bookmark: PlaybackBookmarkEntity): Long

  @Query("UPDATE playback_bookmarks SET title = :title WHERE id = :id")
  suspend fun rename(id: Long, title: String)

  @Query("DELETE FROM playback_bookmarks WHERE id = :id")
  suspend fun delete(id: Long)
}