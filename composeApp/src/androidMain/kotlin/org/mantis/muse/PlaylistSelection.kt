package org.mantis.muse

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.session.legacy.MediaMetadataCompat
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import java.io.File

@Throws(FileSystemException::class)
fun fileCrawler(catchers: Map<String, MutableList<File>>, startFolder: File){
    if (!startFolder.isDirectory){
        throw FileSystemException(startFolder, reason = "provided start folder is not a directory")
    }
    startFolder.listFiles()?.forEach { file ->
        when {
            file.isDirectory -> {
                fileCrawler(catchers, file)
            }
            file.isFile -> {
                catchers.keys.forEach { extension ->
                    if (file.endsWith(extension)) {
                        catchers[extension]!!.add(file)
                    }
                }
            }
        }
    }
}

//fun loadPlaylistScreen(context: Context): List<Playlist> {
//    val storageDirectories:Array<File?> = ContextCompat.getExternalFilesDirs(context, null)
//    val playlistDir = File(storageDirectories[1], "Playlists")
//
//    val playlists = mutableListOf<Playlist>()
//
//    playlistDir.listFiles()?.forEach { file ->
//        if (file.name.endsWith(".m3u")) {
//            file.readLines().forEach { line ->
//                Log.d("FILELINE",line)
//            }
//            val playlist = buildPlaylist(file, playlistDir)
//            playlists.add(playlist)
//        }
//    }
//    return playlists
//}
//
//fun buildPlaylist(file: File, rootfolder: File) : Playlist {
//    val songs: MutableList<Song> = mutableListOf()
//
//    file.readLines().forEach { line ->
//        when {
//            File(rootfolder, line).isFile -> {songs.add(buildSong(File(rootfolder, line)))}
//            line.startsWith("#EXTIMG")  -> {} // playlist cover art
//            line.startsWith("#PLAYLIST") -> {} // playlist title
//        }
//    }
//    return Playlist(file.absolutePath, file.name, songs, )
//}
//
//fun buildSong(file: File) : Song{
//        val mmr = MediaMetadataRetriever()
//        mmr.setDataSource(file.path)
//        val path = file.path
//        val name = file.nameWithoutExtension
//        val artists = when (val a = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)){
//            null -> "Unknown"
//            else -> a
//        }
////        val albumCover = if (mmr.embeddedPicture != null) {BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)} else {null}
//
//        return Song(name, artists, 0f, path)
//    }