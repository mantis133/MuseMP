package org.mantis.muse.util

import org.mantis.muse.storage.entity.SongEntity

expect class Song

expect fun Song.toSongEntity(): SongEntity
expect fun Song.toSongEntity(id: Long): SongEntity