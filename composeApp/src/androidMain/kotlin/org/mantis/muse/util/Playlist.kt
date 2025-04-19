package org.mantis.muse.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import org.koin.java.KoinJavaComponent.inject
import org.mantis.muse.R
import java.io.File

class Playlist(
    val name: String,
    val songList: List<Song>,
    val fileURI: Uri,
    val thumbnailUri: Uri?,
) {
    companion object

    val size: Int
        get() {
            return songList.size
        }

    override fun toString(): String{
        return "$name, songs: ${songList.size}, $fileURI"
    }
}

fun uriToImageBitmap(context: Context, uri: Uri): ImageBitmap {
//    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//        val source = ImageDecoder.createSource(context.contentResolver, uri)
//        ImageDecoder.decodeBitmap(source)
//    } else {
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri).asImageBitmap()
//    }
//
//    return bitmap.asImageBitmap()
}

val Playlist.coverArt: Bitmap? // PREVENTS COMMON MAIN
    get() {
        val context by inject<Context>(Context::class.java)
        return if (thumbnailUri == null) {
            BitmapFactory.decodeResource(context.resources, R.drawable.home_icon)
        } else {
            BitmapFactory.decodeFile(this.thumbnailUri.toFile().toString())
        }
    }

fun Playlist.Companion.fromURI(fileURI: Uri): Playlist{
    val playlistFile: File = fileURI.toFile()
    val songs = mutableListOf<Song>()
    var thumbnailUri: Uri? = null
    val context by inject<Context>(Context::class.java)
//    context.filesDir
    ContextCompat.getExternalFilesDirs(context, null)[1]
    playlistFile.readLines().forEach { line ->
        when {
            line.startsWith("#EXTIMG") -> {thumbnailUri = File(ContextCompat.getExternalFilesDirs(context, null)[1],line.removePrefix("#EXTIMG:").trim()).toUri()}
            line.startsWith("#") -> {}
            else -> {
                val l = playlistFile.parent?.plus("/$line")?:line
                songs.add(fromFilePath(File(l).toUri()))
            }
//            else -> {songs.add(fromFilePath(playlistFile.parent?.plus("/$line")?:line))}
        }
    }

    return Playlist(
        name = playlistFile.nameWithoutExtension,
        songList = songs,
        fileURI = fileURI,
        thumbnailUri = thumbnailUri
    )
}

fun Playlist.Companion.cheapFromURI(fileURI: Uri): Playlist {
    val playlistFile: File = fileURI.toFile()

    return Playlist(
        name = playlistFile.nameWithoutExtension,
        songList = listOf(),
        fileURI = fileURI,
        thumbnailUri = null
    )
}