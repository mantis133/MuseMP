package org.mantis.muse.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.get
import org.mantis.muse.storage.entity.SongEntity
import java.io.File

data class Song(
    val name: String,
    val artist: List<String>,
    val durationMs: Long,
    val fileName: String,
    val fileUri: Uri
) {
    constructor(name: String, artist: List<String>, fileUri: Uri): this(name, artist, 0L, "", fileUri)
    constructor(songEntity: SongEntity, artists: List<String>): this(songEntity.name, artists, songEntity.durationMs, songEntity.fileName, songEntity.uri)

    override fun toString(): String {
        return "$name, $artist, $fileUri"
    }

}

fun Song.toAlbumArt(): Bitmap? {
    val context: Context = get().get<Context>()
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(context, fileUri)
    return if (mmr.embeddedPicture == null) null else BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.fromUri(fileUri)
}

fun fromFilePath(mmr: MediaMetadataRetriever, fileUri: Uri, context: Context = get().get<Context>()): Song{
    mmr.setDataSource(context, fileUri)
    val title: String = when (val t = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)){
        null -> try{ fileUri.toFile().name } catch (_: IllegalArgumentException) { null.toString() }
        else -> t
    }
    val artists = when (val a = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)){
        null -> "Unknown"
        else -> a
    }
    val durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
    return Song(title, listOf(artists), durationMs!!, fileUri.toFile().name, fileUri)
}
