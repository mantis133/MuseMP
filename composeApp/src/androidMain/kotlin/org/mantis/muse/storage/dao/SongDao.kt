package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.SongEntity

@Dao
interface SongDao {
//    @Query("SELECT * FROM songs")
//    fun getAll(): Flow<List<SongEntity>>
//
//    @Insert
//    suspend fun insertSongs(vararg song: SongEntity)
//
//    @Delete
//    suspend fun deleteSong(song: SongEntity)
}