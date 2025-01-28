package org.mantis.muse.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import org.mantis.muse.R

class Playlist(val filePath: String, val name: String, val songList: List<Song>) {
    val coverArt: Bitmap? // PREVENTS COMMON MAIN
        get() {
            return null
        }
    val size: Int
        get() {
            return songList.size
        }

}