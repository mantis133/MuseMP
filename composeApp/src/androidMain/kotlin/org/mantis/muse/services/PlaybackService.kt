package org.mantis.muse.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.koin.android.ext.android.inject
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.util.MediaId.Root
import org.mantis.muse.util.toMuseMediaId
import java.lang.IllegalStateException

@UnstableApi
class PlaybackService : MediaSessionService() {

    private var session : MediaLibrarySession? = null
    val mr by inject<MediaRepository>()
    private var callbacks = MLCallbacks(mr)

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


@UnstableApi
class MLCallbacks(val repo: MediaRepository): MediaLibrarySession.Callback {
    var songs = mutableListOf<MediaItem>()
    var plPos = 0

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(
            LibraryResult.ofItem(Root.getInstance(), params)
        )
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val id = parentId.toMuseMediaId()
        val contents:List<MediaItem> = id.getChildren()
        return Futures.immediateFuture(
                LibraryResult.ofItemList(contents, params)
        )
    }

    @OptIn(UnstableApi::class)
    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val id = mediaId.toMuseMediaId()
        return try { Futures.immediateFuture(LibraryResult.ofItem(id.getInstance(),null)) }
        catch (_: IllegalStateException) { Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_INVALID_STATE)) }
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        println("adding items")
        val fullMediaItems = mediaItems.map { mediaItem ->
            // Stupid dumb "security" conversion
            // when media items come across the veil into this function they lose their Uri property
            onGetItem(
                mediaSession as MediaLibrarySession,
                controller,
                mediaItem.mediaId
            ).get().value!!
        }
        songs.addAll(fullMediaItems)
        return Futures.immediateFuture(fullMediaItems)
    }

//    override fun onSetMediaItems(
//        mediaSession: MediaSession,
//        controller: MediaSession.ControllerInfo,
//        mediaItems: List<MediaItem>,
//        startIndex: Int,
//        startPositionMs: Long
//    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
////        songs = mediaItems as MutableList<MediaItem>
//        println("setting items")
////        plPos = startIndex
//        return Futures.immediateFuture(
//            MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
//        )
//    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        // Resume playback of a recent media item, e.g., from your cache or DB
        println("ADDEDD"+ mediaSession.player.currentMediaItem)
        println("ADDEDD"+ mediaSession.player.mediaItemCount)
        println("PLAYBACK" + songs.map{it.mediaId}.toString())
//        println("ADDEDD"+ mediaSession.player.currentMediaItem)

        val recentItem =
        this.onGetItem(
            mediaSession as MediaLibrarySession,
            controller,
            songs[plPos].mediaId
        ).get().value



        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(
                listOf(recentItem!!),
                /* startIndex = */ 0,
                /* startPositionMs = */ 0L
            )
        )
    }
}

