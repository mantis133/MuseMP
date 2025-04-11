package org.mantis.muse.storage

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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

    fun getSongsFromShared(): Flow<Uri> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.ARTIST,
        )

        val songs = mutableListOf<Uri>()

        this.context.contentResolver.query(
            this.queryUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.ARTIST)

            while(cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val artists = cursor.getString(artistColumn)

                val contentUri = ContentUris.withAppendedId(queryUri, id)

                songs.add(contentUri)
            }
        }

        return songs.asFlow()
    }

//    fun getAllSharedFiles() -> Flow<File>{
//
//        val projection = arrayOf(
//            MediaStore.Files.FileColumns._ID,
//            MediaStore.Files.FileColumns.DISPLAY_NAME,
//            MediaStore.Files.FileColumns.MIME_TYPE,
//
//            )
//
//        this.context.contentResolver.query(
//            queryUri,
//            projection,
//            null,
//            null,
//            null
//        )?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
//            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
//            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
//            println(cursor.count)
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val name = cursor.getString(nameColumn)
//                val mimeType = cursor.getString(mimeTypeColumn)
//
//                val contentUri = ContentUris.withAppendedId(queryUri, id)
//
//                println("File: $name, of mimeType: $mimeType, at location: $contentUri")
//            }
//        }
//    }
//
//    return

}