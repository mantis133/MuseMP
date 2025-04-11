package org.mantis.muse.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import java.io.File

class Song(val name: String, val artist: List<String>, val fileUri: Uri) {

    override fun toString(): String {
        return "$name, $artist, $fileUri"
    }

}

fun Song.toAlbumArt(): Bitmap? {
    val file = fileUri.toFile()
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(file.path)
    return if (mmr.embeddedPicture == null) null else BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.fromUri(fileUri)
}

fun fromFilePath(fileUri: Uri): Song{
    val file = fileUri.toFile()
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(file.path)
    val artists = when (val a = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)){
        null -> "Unknown"
        else -> a
    }
    return Song(file.nameWithoutExtension, listOf(artists), fileUri)
}

//fun getAlbumCover() : Bitmap?{
//    val file = File(this.path)
//    val mmr = MediaMetadataRetriever()
//    mmr.setDataSource(file.path)
//    return if (mmr.embeddedPicture != null) {
//        BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)} else {null}
//}