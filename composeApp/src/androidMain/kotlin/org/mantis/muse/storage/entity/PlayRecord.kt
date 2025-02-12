package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songRecords")
data class PlayRecord(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val songName: String,
    val songArtists: String,
    val playlistName: String?,
    val listenedDurationSecs: Int
)
