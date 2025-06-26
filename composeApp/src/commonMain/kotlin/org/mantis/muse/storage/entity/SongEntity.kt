package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.mantis.muse.util.Song

@Entity(
    tableName = "song",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val durationMs: Long,
    val fileName: String,
    val uri: String
)