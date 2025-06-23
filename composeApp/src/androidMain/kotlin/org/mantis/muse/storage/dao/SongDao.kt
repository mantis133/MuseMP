package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.SongEntity

@Dao
interface SongDao {
    @Query("SELECT * FROM song")
    fun getAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM song WHERE name = :songName")
    suspend fun getSongByName(songName: String): SongEntity?

    @Query("SELECT * FROM song WHERE fileName = :filename")
    suspend fun getSongByFilename(filename: String): SongEntity?

    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongEntity?

//    @Query("SELECT * FROM song S WHERE id IN (SELECT songId FROM playlist_song_entry where playlistId = :playlistId) ORDER BY (SELECT position FROM playlist_song_entry WHERE songId = S.id)")
    @Query("""
        SELECT S.* FROM song S
        INNER JOIN playlist_song_entry PSE ON S.id = PSE.songId
        WHERE PSE.playlistId = :playlistId
        ORDER BY PSE.position
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT * FROM song WHERE id in (SELECT songId FROM artist_song_record WHERE artistId = :artistId)")
    suspend fun getSongsFromArtist(artistId: Long): List<SongEntity>

    @Insert(onConflict = IGNORE)
    suspend fun insertSongs(song: SongEntity): Long

    @Delete
    suspend fun deleteSong(song: SongEntity)
}