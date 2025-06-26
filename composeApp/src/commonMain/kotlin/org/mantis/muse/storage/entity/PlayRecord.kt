package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

enum class RecordableActions {
    Play,
    Pause,
    SkipNext,
    SkipLast,
    Started,
    Ended,
    ChangedLoopState,
    ChangedShuffleState,
    SongLoaded,
}

@Entity(tableName = "songRecords")
data class PlayRecord(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val songName: String,
    val songArtists: String,
    val playlistName: String?,
    val songDurationPositionMS: Int,
    val dateTime: Date,
    val action: RecordableActions
)
