package org.mantis.muse.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.media3.common.MediaItem
import java.io.File

//class Song(val name: String, val artist: String, val lengthMs: Float, val filePath: String) {
//    constructor(filePath: String): this(
//        name = "",
//        artist = "",
//        0f,
//        filePath = filePath,
//    )
//
//    override fun toString(): String {
//        return "$name, $artist, $filePath"
//    }
//
//}

//expect fun Song.toAlbumArt(): Bitmap?


fun Song.toAlbumArt(): Bitmap? {
    val file = File(this.filePath)
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(file.path)
    return if (mmr.embeddedPicture == null) null else BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.fromUri(this.filePath)
}


//fun getAlbumCover() : Bitmap?{
//    val file = File(this.path)
//    val mmr = MediaMetadataRetriever()
//    mmr.setDataSource(file.path)
//    return if (mmr.embeddedPicture != null) {
//        BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)} else {null}
//}