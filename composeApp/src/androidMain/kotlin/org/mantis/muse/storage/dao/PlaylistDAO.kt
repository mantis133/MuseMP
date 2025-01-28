package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.util.Playlist

@Dao
interface PlaylistDAO {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert
    suspend fun insertPlaylists(vararg playlists: PlaylistEntity)

    @Update
    suspend fun updatePlaylists(vararg playlists: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
}