package org.mantis.muse

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext
import org.mantis.muse.layouts.MediaPlayerUI
import org.mantis.muse.layouts.theme.MuseTheme
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.fromFilePath
import org.mantis.muse.util.fromURI
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity() {
    private var conn: MediaController? = null
    private val player by inject<AndroidMediaPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.notificationBar)

        checkPermissions()

        val localFiles: LocalFileSource = get()
        val mediaRepository: MediaRepository = get()

        GlobalScope.launch(Dispatchers.IO){
            localFiles.localPlaylistFiles.collect { files ->
                val playlists = files.map {
                    Playlist(
                        it.nameWithoutExtension,
                        listOf(),
                        it.toUri()
                    )
                }.onEach { playlist -> mediaRepository.insertPlaylist(playlist) }
            }
            localFiles.localMp3Files.collect { files ->
                val songs = files
                    .map { fromFilePath(it.toUri()) }
                    .onEach { song -> mediaRepository.insertSong(song) }
            }
            mediaRepository.playlistsStream.collect { playlists ->
                playlists.onEach { playlist ->
                    val completePlaylist = Playlist.Companion.fromURI(playlist.fileURI)
                    completePlaylist.songList.forEach { song ->
                        mediaRepository.addSongToPlaylist(playlist, song)
                    }
                }
            }
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

    override fun onStart() {
        super.onStart()
//        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
//        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//        controllerFuture.addListener(
//            {
//                conn = controllerFuture.get()
//                conn.value.addListener(object: Player.Listener{
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        super.onPlaybackStateChanged(playbackState)
//                        if(playbackState == Player.STATE_READY) {
////                            songPosition = conn!!.currentPosition.toInt()
//                            Log.d("time", conn!!.currentPosition.toString())
//                        }
//                    }
//
//                })
//                player.mediaConn = conn
//                println("Media Controller is loaded")
//            },
//            MoreExecutors.directExecutor()
//        )
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

