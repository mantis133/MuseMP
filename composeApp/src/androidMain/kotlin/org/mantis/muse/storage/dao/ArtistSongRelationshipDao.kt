package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.mantis.muse.storage.entity.ArtistSongEntity

@Dao
interface ArtistSongRelationshipDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg relation: ArtistSongEntity)

    @Delete
    suspend fun delete(relation: ArtistSongEntity)
}