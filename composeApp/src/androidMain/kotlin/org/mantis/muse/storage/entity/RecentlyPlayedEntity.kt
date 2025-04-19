package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity (
    @PrimaryKey val id: Long,
    val mediaId: String,
    val position: Long?,
)