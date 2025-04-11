package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artist",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val youtube: String?,
    val spotify: String?,
)
