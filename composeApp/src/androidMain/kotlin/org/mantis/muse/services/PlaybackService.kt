package org.mantis.muse.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var session : MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return session
    }

    override fun onCreate() {
        super.onCreate()

        val intentReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action){
                    Intent.ACTION_HEADSET_PLUG -> {
//                        when(intent.getIntExtra("state",-1)){
//                            0 -> {/*headphones unplugged*/
//                                session?.player?.pause()
//                            }
//                            1 -> {/*headphones plugged in*/
//
//                            }
//                            else ->{/*who the hell knows*/
////                                Log.d(TAG, "change?")
//                            }
//                        }
                        session?.player?.pause()
                        println("HEADPHONE ACTION")
                    }
                }
            }
        }

//        val likeButton = CommandButton.Builder()
//            .setDisplayName("Like")
//            .setIconResId(R.drawable.like_icon)
//            .setSessionCommand(SessionCommand(SessionCommand.COMMAND_CODE_SESSION_SET_RATING))
//            .build()
//        val favoriteButton = CommandButton.Builder()
//            .setDisplayName("Save to favorites")
//            .setIconResId(R.drawable.favorite_icon)
//            .setSessionCommand(SessionCommand(SAVE_TO_FAVORITES, Bundle()))
//            .build()
        

        val player : ExoPlayer = ExoPlayer.Builder(this)

            .build()
        session = MediaSession.Builder(this, player)

            .build()

        registerReceiver(
            intentReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_HEADSET_PLUG)
            }
        )
    }

    override fun startForegroundService(service: Intent?): ComponentName? {
        // implementing this function solves an error when trying to resume playback with the app closed?

        return super.startForegroundService(service)
    }

    override fun onTaskRemoved(rootIntent: Intent?) { // this function is causing the big errors
        val player = session?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }


    override fun onDestroy() {
        session?.run {
            player.release()
            release()
            session = null
        }
        super.onDestroy()
    }
}