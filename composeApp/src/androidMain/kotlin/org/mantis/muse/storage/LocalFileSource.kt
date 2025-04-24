package org.mantis.muse.storage

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.mantis.muse.util.Song
import java.io.File

class LocalFileSource(
    val context: Context
) {
    private val dataSources: Array<File> = ContextCompat.getExternalFilesDirs(context, null)

    private val queryUri = if(Build.VERSION.SDK_INT >= 29){
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Files.getContentUri("external")
    }

    val localMp3Files: Flow<List<File>>
        get() = dataSources.map { dir -> dir.walkTopDown().filter{ it.extension == "mp3" }.toList() }.asFlow()

    val localPlaylistFiles: Flow<List<File>>
        get() = dataSources.map { dir -> dir.walkTopDown().filter { it.extension == "m3u" }.toList() }.asFlow()

    val sharedSongs: Flow<List<LocalSongResult>>
        get() = flow {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE
            )

            val songs = mutableListOf<LocalSongResult>()

            val selection = "${MediaStore.Audio.Media.MIME_TYPE} LIKE ?"
            val selectionArgs = arrayOf("audio/%")

            val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val fileNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)


                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val fileName = cursor.getString(fileNameColumn)
                    val artist = cursor.getString(artistColumn)
                    val contentUri = ContentUris.withAppendedId(uri, id)
                    songs.add(LocalSongResult(title, fileName, artist, contentUri))
                }
            }

            emit(songs)
        }

    val sharedPlaylistFiles: Flow<List<LocalPlaylistResult>>
        get() = flow {
            val files = mutableListOf<LocalPlaylistResult>()

            val queryUri = MediaStore.Files.getContentUri("external")

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE,
            )

            val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%.m3u")

            context.contentResolver.query(
                queryUri,
                projection,
                selection, selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val fileName = cursor.getString(displayNameColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val contentUri = ContentUris.withAppendedId(queryUri, id)
//                    println("$id, $title, $fileName, $mimeType, $contentUri")
                    files.add(LocalPlaylistResult(title, fileName, contentUri))
                }
            }

            emit(files)
        }
}

data class LocalSongResult(
    val name: String,
    val fileName: String,
    val artists: String?,
    val uri: Uri
)

data class LocalPlaylistResult (
    val name: String?,
    val fileName: String,
    val uri: Uri
)