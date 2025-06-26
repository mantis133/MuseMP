package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Update
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.PlaylistEntity

@Dao
interface PlaylistSongRelationshipDao {

    @get:Query("SELECT * FROM playlist_song_entry")
    val playlistEntries: Flow<List<PlaylistSongEntryEntity>>

    @Insert(onConflict = IGNORE)
    suspend fun insert(playlistSongRelationship: PlaylistSongEntryEntity): Long

    @Delete
    suspend fun delete(playlistSongRelationship: PlaylistSongEntryEntity)
}