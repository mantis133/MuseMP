package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.SongEntity

@Dao
interface SongDao {
    @Query("SELECT * FROM song")
    fun getAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM song WHERE name = :songName")
    suspend fun getSongByName(songName: String): SongEntity?

    @Query("SELECT * FROM song WHERE id IN (SELECT songId FROM playlist_song_entry where playlistId = :playlistId)")
    suspend fun getSongsInPlaylist(playlistId: Long): List<SongEntity>

    @Insert(onConflict = IGNORE)
    suspend fun insertSongs(song: SongEntity): Long

    @Delete
    suspend fun deleteSong(song: SongEntity)
}