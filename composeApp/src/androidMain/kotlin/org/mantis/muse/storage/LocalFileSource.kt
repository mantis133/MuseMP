package org.mantis.muse.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.io.File

class LocalFileSource(
    private val dataSources: Array<File>
) {
    val localMp3Files: Flow<File>
        get() = dataSources.flatMap { dir -> dir.walkTopDown().filter{ it.extension == "mp3" } }.asFlow()

    val localPlaylistFiles: Flow<File>
        get() = dataSources.flatMap { dir -> dir.walkTopDown().filter { it.extension == "m3u" } }.asFlow()



}