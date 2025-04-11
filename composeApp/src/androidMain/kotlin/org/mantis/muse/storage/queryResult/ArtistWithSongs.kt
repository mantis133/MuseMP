package org.mantis.muse.storage.queryResult

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.mantis.muse.storage.entity.ArtistEntity
import org.mantis.muse.storage.entity.ArtistSongEntity
import org.mantis.muse.storage.entity.SongEntity

data class ArtistWithSongs(
    @Embedded val artist: ArtistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "songId",
        associateBy = Junction(ArtistSongEntity::class)
    )
    val songs: List<SongEntity>
)
