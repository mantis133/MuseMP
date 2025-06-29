package org.mantis.muse.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "remote_connection"
)
class RemoteConnectionEntity (
    @PrimaryKey
    val id: Long,
    val displayName: String,
    val ip: String,
    val port: Short
)