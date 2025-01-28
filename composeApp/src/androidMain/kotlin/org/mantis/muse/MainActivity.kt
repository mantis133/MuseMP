package org.mantis.muse

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.room.Room
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mantis.muse.layouts.PlayerChipStateful
import org.mantis.muse.layouts.PlaylistSelectionScreenState
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.viewmodels.MediaPlayerViewModel
import org.mantis.muse.viewmodels.PlaylistPickerViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    var conn: MediaController? = null
    val player = AndroidMediaPlayer(conn)

    var songPosition = 7;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val callbacks = MediaPLayerCallbacks(
//            {playing -> playPause(playing, conn!!);},
//            {skipNext(conn!!)},
//            {skipLast(conn!!)},
//            {loop(conn!!)},
//            {shuffle(conn!!)}
//        )
//        window.statusBarColor = ContextCompat.getColor(this, androidx.media3.session.R.color.primary_text_default_material_dark)

        val db = Room.databaseBuilder(
            this,
            MusicCacheDB::class.java, "MusicCache"
        ).build()

        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
        val searcher = LocalFileSource(externalStorageVolumes.filterNotNull().toTypedArray())

        val playlistsDao = db.playlistDAO()
        val playlistRepo = PlaylistRepository(playlistsDao)

        var mi: MediaItem? = null

        runBlocking{
            searcher.localMp3Files.collect {
                println(it.name + " " + it.absolutePath)
                mi = MediaItem.fromUri(it.absolutePath)
            }
        }

        val viewModel by mutableStateOf(PlaylistPickerViewModel(searcher, playlistRepo, player))
        val mediaViewModel by mutableStateOf(MediaPlayerViewModel(player))
//        println("music directory")
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).walkTopDown()
//            .forEach { file ->
//                println(file.name) // seems to work on android 9 api 28
//            }
        val sdCardStorage = externalStorageVolumes[1] // should be sd card. causes a error when no sd card in present (java.lang.ArrayIndexOutOfBoundsException)


//        val filePaths = mutableListOf<String>()
//
//        this.contentResolver.query(
//            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//            arrayOf(MediaStore.Audio.Media.DATA),
//            "${MediaStore.Audio.Media.MIME_TYPE} = ?",
//            arrayOf("audio/mpeg"),
//            null
//        )?.use {
//            while (it.moveToNext()) {
//                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
//                filePaths.add(path)
//            }
//        }
//        println("playlists: $filePaths")

        CoroutineScope(Dispatchers.IO).launch {
//            viewModel.newPlaylist(Playlist("fake","super cool and fake playlist",listOf()))
            playlistsDao.getAllPlaylists().collect{value -> println(value)}
        }

        setContent {
            PlaylistSelectionScreenState(viewModel)
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()){
                PlayerChipStateful(viewModel = mediaViewModel, modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp))
            }
//            NavHostContainer(this, callbacks) {playlist:Playlist -> this.loadedPlaylist = playlist}
//            Box(Modifier.padding(10.dp)){
//                MediaPlayerChip(BitmapFactory.decodeResource(this@MainActivity.resources, R.drawable.home_icon),"cool son gggg ggg ggggggggggg", "cool Artist", Modifier.height(100.dp))
//            }
        }

    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                conn = controllerFuture.get();
//                conn.value.addListener(object: Player.Listener{
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        super.onPlaybackStateChanged(playbackState)
//                        if(playbackState == Player.STATE_READY) {
//                            songPosition = conn!!.currentPosition.toInt()
//                            Log.d("time", conn!!.currentPosition.toString())
//                        }
//                    }
//                })
                player.mediaConn = conn
                println("Media Controller is loaded")
            },
            MoreExecutors.directExecutor()
        )
    }
}

