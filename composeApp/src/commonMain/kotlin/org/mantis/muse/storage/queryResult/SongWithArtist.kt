package org.mantis.muse.storage.queryResult

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.mantis.muse.storage.entity.ArtistEntity
import org.mantis.muse.storage.entity.ArtistSongEntity
import org.mantis.muse.storage.entity.SongEntity


data class SongWithArtist(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "artistId",
        associateBy = Junction(ArtistSongEntity::class)
    )
    val artists: List<ArtistEntity>
)