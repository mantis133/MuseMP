package org.mantis.muse.repositories

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song

class SongRepository(
    private val songDao: SongDao
) {
//    fun getAllFromDatabase(): Flow<List<Song>> = songDao.getAll().map { songEntities ->
//        songEntities.map { songEntity ->
//            Song (
//                songEntity.name,
//                songEntity.artists,
//                ""
//            )
//        }
//    }

    fun getAllFromInternals(context: Context): Flow<List<Song>> {
        val filePaths = mutableListOf<String>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATA),
            "${MediaStore.Audio.Media.MIME_TYPE} = ?",
            arrayOf("audio/mpeg"),
            null
        )?.use {
            while (it.moveToNext()) {
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                filePaths.add(path)
            }
        }
        return flowOf()
    }
}