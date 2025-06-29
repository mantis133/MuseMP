package org.mantis.muse.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.mantis.muse.storage.entity.RemoteConnectionEntity

@Dao
interface RemoteConnectionDao {

    @get:Query("SELECT * FROM remote_connection")
    val connections: Flow<List<RemoteConnectionEntity>>

    @Insert
    suspend fun insert(connection: RemoteConnectionEntity)

    @Delete
    suspend fun delete(connection: RemoteConnectionEntity)
}