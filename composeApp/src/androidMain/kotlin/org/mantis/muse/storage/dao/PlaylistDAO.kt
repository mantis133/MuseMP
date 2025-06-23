package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.util.Playlist

@Dao
interface PlaylistDAO {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

//    @Transaction
//    @Query("SELECT * FROM playlist where id = :playlistId")
//    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Query("SELECT * FROM playlist WHERE name = :playlistName")
    fun getPlaylistByName(playlistName: String): Flow<PlaylistEntity?>

    @Insert
    suspend fun insertPlaylists(vararg playlists: PlaylistEntity)

    @Update
    suspend fun updatePlaylists(vararg playlists: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
}