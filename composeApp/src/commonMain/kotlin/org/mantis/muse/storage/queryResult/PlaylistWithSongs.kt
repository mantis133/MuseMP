package org.mantis.muse.storage.queryResult

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity
import org.mantis.muse.storage.entity.SongEntity

//data class PlaylistWithSongs(
//    @Embedded val playlist: PlaylistEntity,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "songId",
//        associateBy = Junction(PlaylistSongEntryEntity::class)
//    )
//    val songs: List<SongEntity>
//)
