package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "artist_song_record",
    primaryKeys = ["artistId","songId"],
    foreignKeys = [
        ForeignKey(entity = ArtistEntity::class, parentColumns = ["id"], childColumns = ["artistId"], onDelete = CASCADE),
        ForeignKey(entity = SongEntity::class, parentColumns = ["id"], childColumns = ["songId"], onDelete = CASCADE),
    ],
    indices = [
        Index(value = ["songId"], unique = true)
    ]
)
data class ArtistSongEntity(
    val artistId: Long,
    val songId: Long,
)
