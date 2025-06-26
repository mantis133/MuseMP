package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import org.mantis.muse.storage.entity.RecentlyPlayedEntity

@Dao
interface RecentlyPlayedDao {

    @Query("SELECT * FROM recently_played WHERE id = :id")
    suspend fun getId(id: Long): RecentlyPlayedEntity

    @Insert(onConflict = REPLACE)
    suspend fun insert(playRecord: RecentlyPlayedEntity): Long

    @Delete
    suspend fun delete(playRecord: RecentlyPlayedEntity)
}