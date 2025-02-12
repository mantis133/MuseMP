package org.mantis.muse.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.storage.entity.SongEntity

@Database(
    entities = [
        PlaylistEntity::class,
        SongEntity::class
               ],
    exportSchema = false,
    version = 2
)
abstract class MusicCacheDB: RoomDatabase() {
    abstract fun playlistDAO(): PlaylistDAO
    abstract fun songDAO(): SongDao
}