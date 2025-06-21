package org.mantis.muse

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext
import org.mantis.muse.MainActivity
import org.mantis.muse.layouts.MediaPlayerUI
import org.mantis.muse.layouts.theme.MuseTheme
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.storage.dao.ArtistDao
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.storage.entity.SongEntity
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.fromFilePath
import org.mantis.muse.util.fromURI
import java.io.File
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.notificationBar)

        checkPermissions()

//        runBlocking {  }

        GlobalScope.launch(Dispatchers.IO){
//            cleanDB(get(), get(), get())
//            reloadDB(get(), get(), get(), this@MainActivity)

        }

        setContent {
            MuseTheme{
                KoinContext{
                    NavHostContainer(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                    MediaPlayerUI()
                }
            }
        }

    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
//        var browser: MediaBrowser? = null
//        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))

//        val browserFuture = MediaBrowser.Builder(this, sessionToken).buildAsync()
//        browserFuture.addListener({
//                browser = browserFuture.get()
//                val mi: MediaItem = browser?.getItem("SONGSummer")?.get()?.value?:throw IllegalArgumentException("harahar")
//                browser.addMediaItem(mi)
//                browser.prepare()
//                browser.play()
//            }, MoreExecutors.directExecutor())
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= 33)
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(
            this,
            permissions,
            0
        )
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            println("yippee")
        } else {
            println("ARGG!!")
        }
    }
}

suspend fun cleanDB(playlistDAO: PlaylistDAO, songDao: SongDao, artistDao: ArtistDao) {
    playlistDAO.getAllPlaylists().collect { it.onEach { playlistDAO.deletePlaylist(it) } }
    songDao.getAll().collect { it.onEach { songDao.deleteSong(it) } }
    artistDao.getAllArtists().collect { it.onEach { artistDao.deleteArtist(it) } }
}

suspend fun reloadDB(localFiles: LocalFileSource, songDao: SongDao, mediaRepository: MediaRepository, context: Context){

    // There is a crash when searching for a non existent file when adding songs from playlist

    val mmr = MediaMetadataRetriever()
    localFiles.localMp3Files.collect { files ->
        val songs = files
            .map { fromFilePath(mmr, it.toUri()) }
            .onEach { song -> mediaRepository.insertSong(song) }
    }
//    localFiles.sharedSongs.collect { uris ->
//        val songs = uris
//            .onEach { println("SHARED SONG: $it") }
//            .onEach { song -> mediaRepository.insertSong(Song(song.name, if(song.artists!=null){listOf(song.artists)}else{listOf()}, song.fileName, song.uri)) }
//            println(songs.size)
//    }
    localFiles.localPlaylistFiles.collect { files ->
        val playlists = files
            .map { Playlist.Companion.fromURI(it.toUri()) }
            .onEach { playlist -> mediaRepository.insertPlaylist(playlist) }
    }
//    localFiles.sharedPlaylistFiles.collect { playlists ->
//        playlists
//            .map { playlistRes -> Playlist(playlistRes.name.toString(), listOf(), playlistRes.uri, null) }
//            .forEach { playlist -> mediaRepository.insertPlaylist(playlist) }
//    }
    mediaRepository.playlistsStream.collect { playlists ->
        playlists.onEach { playlist ->
            val completePlaylist = Playlist.Companion.fromURI(playlist.fileURI)
            completePlaylist.songList.forEachIndexed { idx, song ->
                mediaRepository.addSongToPlaylist(playlist, song, idx.toLong())
            }
        }
    }
//            val uri = playlist.fileURI
//            val songs = mutableListOf<Song>()
//            context
//                .contentResolver
//                .openInputStream(uri)
//                ?.bufferedReader()
//                .use { fileContent ->
//                    fileContent?.readLines()?.forEachIndexed { idx, fileLine ->
//                        when {
//                            fileLine.startsWith("#EXTIMG") -> {}
//                            fileLine.startsWith("#") -> {}
//                            else -> {
////                                        val l = playlistFile.parent?.plus("/$fileLine")?:fileLine
////                                        songs.add(fromFilePath(mmr, File(l).toUri()))
//                                val songName = fileLine.substringAfterLast('/')
//                                println(songName)
//                                val found = mediaRepository.getSongByFilename(songName)
//                                println(found != null)
//                                found?.let { mediaRepository.addSongToPlaylist(playlist, found, idx.toLong()) }
//                            }
//                        }
//                    }
//                }

    mmr.release()
}

//data class M3UParserResult(
//    val songUris: List<String>
//)
//
//fun m3uParser(fileLines: List<String>): M3UParserResult {
//
//}