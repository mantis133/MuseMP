package com.example.muse.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.media3.common.MediaItem
import java.io.File
import java.util.Vector

data class Playlist(
    val path : String,
    val title: String,
    val cover: Bitmap?,
    val songs: List<Song>,
    val size: Int
)

data class Song(
    val path : String,
    val name: String,
    val artists: String,
//    val albumCover: Bitmap?,
)
{
    fun intoMediaItem() : MediaItem {
        return MediaItem.fromUri(path)
    }
    fun getAlbumCover() : Bitmap?{
        val file = File(this.path)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(file.path)
        return if (mmr.embeddedPicture != null) {
            BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)} else {null}
    }
}