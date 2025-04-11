package org.mantis.muse.util

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import java.io.File

class Playlist(
    val name: String,
    val songList: List<Song>,
    val fileURI: Uri
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

val Playlist.coverArt: Bitmap? // PREVENTS COMMON MAIN
    get() {
        return null
    }

fun Playlist.Companion.fromURI(fileURI: Uri): Playlist{
    val playlistFile: File = fileURI.toFile()
    val songs = mutableListOf<Song>()

    playlistFile.readLines().forEach { line ->
        when {
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
        fileURI = fileURI
    )
}

fun Playlist.Companion.cheapFromURI(fileURI: Uri): Playlist {
    val playlistFile: File = fileURI.toFile()

    return Playlist(
        name = playlistFile.nameWithoutExtension,
        songList = listOf(),
        fileURI = fileURI
    )
}