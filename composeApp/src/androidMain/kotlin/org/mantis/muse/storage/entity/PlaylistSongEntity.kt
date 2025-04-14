package org.mantis.muse.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "playlist_song_entry",
    primaryKeys = ["playlistId","songId"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = CASCADE),
        ForeignKey(entity = SongEntity::class, parentColumns = ["id"], childColumns = ["songId"], onDelete = CASCADE),
    ]
)
data class PlaylistSongEntryEntity(
    @ColumnInfo(index = true)
    val songId: Long,
    val playlistId: Long,
    val position: Long,
)