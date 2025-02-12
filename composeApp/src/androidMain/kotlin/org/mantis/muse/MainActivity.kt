package org.mantis.muse

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext
import org.mantis.muse.layouts.MediaPlayerUI
import org.mantis.muse.layouts.theme.MuseTheme
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.util.AndroidMediaPlayer

class MainActivity : ComponentActivity() {
    private var conn: MediaController? = null
    private val player by inject<AndroidMediaPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.notificationBar)

        checkPermissions()

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
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                conn = controllerFuture.get()
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
                player.mediaConn = conn
                println("Media Controller is loaded")
            },
            MoreExecutors.directExecutor()
        )
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

