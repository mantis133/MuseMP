package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity

@Dao
interface PlaylistSongRelationshipDao {

    @Insert(onConflict = IGNORE)
    suspend fun insert(playlistSongRelationship: PlaylistSongEntryEntity): Long

    @Delete
    suspend fun delete(playlistSongRelationship: PlaylistSongEntryEntity)
}