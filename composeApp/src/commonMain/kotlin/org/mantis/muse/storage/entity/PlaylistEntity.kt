package org.mantis.muse.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist",
    indices = [
        Index(value=["name"], unique = true),
        Index(value=["fileUri"], unique = true)
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val fileUri: String,
    val thumbnailUri: String?,
)