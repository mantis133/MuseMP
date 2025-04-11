package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.ArtistEntity

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artist WHERE name = :artistName")
    suspend fun getArtistByName(artistName: String): ArtistEntity

    @Query("SELECT * FROM artist WHERE id IN (SELECT artistId FROM artist_song_record WHERE songId = :songId)")
    suspend fun getArtistsBySong(songId: Long): List<ArtistEntity>

    @Insert(onConflict = IGNORE)
    suspend fun insertArtists(artists: ArtistEntity): Long

    @Update
    suspend fun updateArtists(vararg playlists: ArtistEntity)

    @Delete
    suspend fun deleteArtist(playlist: ArtistEntity)
}