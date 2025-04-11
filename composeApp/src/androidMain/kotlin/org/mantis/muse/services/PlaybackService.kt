package org.mantis.muse.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.OptIn
import androidx.core.bundle.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future

class PlaybackService : MediaSessionService() {

    private var session : MediaLibrarySession? = null
    private var callbacks = object: MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            controller: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val mediaItem = MediaItem.Builder()
                    .setMediaId("root")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("My Music Library")
                            .setIsBrowsable(true)
                            .build()
                    )
                    .build()
            return Futures.immediateFuture(LibraryResult.ofItem(mediaItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            controller: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.immediateFuture(LibraryResult.ofItemList(when (parentId) {
                "root" -> listOf(
                    createFolderItem("playlists", "Playlists"),
                    createFolderItem("songs", "All Songs")
                )

                "songs" -> getAllSongs()
                "playlists" -> getAllPlaylists()
                else -> emptyList()
            },params))
        }

        @OptIn(UnstableApi::class)
        override fun onGetItem(
            session: MediaLibrarySession,
            controller: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val mediaItem = findMediaItemById(mediaId)
            if (mediaItem == null) {
                return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_UNKNOWN));
            }
            return Futures.immediateFuture(LibraryResult.ofItem(mediaItem, null))
        }

        // Helpers
        private fun createFolderItem(id: String, title: String): MediaItem {
            return MediaItem.Builder()
                .setMediaId(id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setIsBrowsable(true)
                        .build()
                )
                .build()
        }

        private fun getAllSongs(): List<MediaItem> {
            // Replace with real query to your media DB
            return listOf(
                MediaItem.Builder()
                    .setMediaId("song1")
                    .setUri("asset:///song1.mp3")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("Song 1")
                            .setArtist("Artist 1")
                            .build()
                    )
                    .build()
            )
        }

        private fun getAllPlaylists(): List<MediaItem> {
            return listOf(
                createFolderItem("playlist1", "Favorites")
            )
        }

        private fun findMediaItemById(id: String): MediaItem? {
            return (getAllSongs() + getAllPlaylists()).find { it.mediaId == id }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return session
    }

    @OptIn(UnstableApi::class)
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
        session = MediaLibrarySession.Builder(this, player, callbacks)
            .setId("MuseMP")
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