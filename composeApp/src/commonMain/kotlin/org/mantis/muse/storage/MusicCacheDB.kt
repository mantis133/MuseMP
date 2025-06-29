package org.mantis.muse.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.mantis.muse.storage.dao.ArtistDao
import org.mantis.muse.storage.dao.ArtistSongRelationshipDao
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.PlaylistSongRelationshipDao
import org.mantis.muse.storage.dao.RecentlyPlayedDao
import org.mantis.muse.storage.dao.RemoteConnectionDao
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.storage.entity.ArtistEntity
import org.mantis.muse.storage.entity.ArtistSongEntity
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity
import org.mantis.muse.storage.entity.RecentlyPlayedEntity
import org.mantis.muse.storage.entity.RemoteConnectionEntity
import org.mantis.muse.storage.entity.SongEntity

@Database(
    entities = [
        PlaylistEntity::class,
        SongEntity::class,
        ArtistEntity::class,
        PlaylistSongEntryEntity::class,
        ArtistSongEntity::class,
        RecentlyPlayedEntity::class,
        RemoteConnectionEntity::class,
    ],
    exportSchema = false,
    version = 7
)
//@TypeConverters(Converters::class)
abstract class MusicCacheDB: RoomDatabase() {
    abstract fun playlistDAO(): PlaylistDAO
    abstract fun songDAO(): SongDao
    abstract fun artistDAO(): ArtistDao
    abstract fun artistSongRelationDao(): ArtistSongRelationshipDao
    abstract fun playlistSongRelationDao(): PlaylistSongRelationshipDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun remoteConnectionDao(): RemoteConnectionDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<MusicCacheDB> {
    override fun initialize(): MusicCacheDB
}