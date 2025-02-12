package org.mantis.muse.util

import android.graphics.Bitmap
import java.io.File
import java.net.URI

class Playlist(
    val filePath: String,
    val name: String,
    val songList: List<Song>,
    val fileURI: URI
) {
    companion object


    val size: Int
        get() {
            return songList.size
        }
}

val Playlist.coverArt: Bitmap? // PREVENTS COMMON MAIN
    get() {
        return null
    }

fun Playlist.Companion.fromURI(fileURI: URI): Playlist{
    val playlistFile: File = File(fileURI)
    val songs = mutableListOf<Song>()

    playlistFile.readLines().forEach { line ->
        when {
            line.startsWith("#") -> {}
            else -> {songs.add(fromFilePath(playlistFile.parent?.plus("/$line")?:line))}
        }
    }

    return Playlist(
        filePath = playlistFile.absolutePath,
        name = playlistFile.nameWithoutExtension,
        songList = songs,
        fileURI = fileURI
    )
}

fun Playlist.Companion.cheapFromURI(fileURI: URI): Playlist {
    val playlistFile: File = File(fileURI)

    return Playlist(
        filePath = playlistFile.absolutePath,
        name = playlistFile.nameWithoutExtension,
        songList = listOf(),
        fileURI = fileURI
    )
}