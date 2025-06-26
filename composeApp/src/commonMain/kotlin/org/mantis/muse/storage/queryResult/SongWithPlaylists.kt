package org.mantis.muse.storage.queryResult

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity
import org.mantis.muse.storage.entity.SongEntity


data class SongWithPlaylists(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId",
        associateBy = Junction(PlaylistSongEntryEntity::class)
    )
    val playlists: List<PlaylistEntity>
)