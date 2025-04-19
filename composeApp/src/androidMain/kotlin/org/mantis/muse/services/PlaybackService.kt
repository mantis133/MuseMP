package org.mantis.muse.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Effect
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.PriorityTaskManager
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.analytics.AnalyticsCollector
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.TrackSelectionArray
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.media3.exoplayer.video.spherical.CameraMotionListener
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionError
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.services.MLCallbacks
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.storage.dao.ArtistDao
import org.mantis.muse.storage.dao.ArtistSongRelationshipDao
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.PlaylistSongRelationshipDao
import org.mantis.muse.storage.dao.RecentlyPlayedDao
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.util.MediaId.Root
import org.mantis.muse.util.toMuseMediaId
import java.lang.IllegalStateException

@UnstableApi
class PlaybackService : MediaLibraryService() {

    private var session : MediaLibrarySession? = null
    private lateinit var db: MusicCacheDB
    private lateinit var playlistDao: PlaylistDAO
    private lateinit var songDao: SongDao
    private lateinit var artistDao: ArtistDao
    private lateinit var artistSongDao: ArtistSongRelationshipDao
    private lateinit var playlistSongDao: PlaylistSongRelationshipDao
    private lateinit var recentDao: RecentlyPlayedDao
    private lateinit var mediaRepository: MediaRepository
    private lateinit var callbacks: MLCallbacks

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return session
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            context = this@PlaybackService,
            klass = MusicCacheDB::class.java,
            name = "MusicCache")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
        playlistDao = db.playlistDAO()
        songDao = db.songDAO()
        artistDao = db.artistDAO()
        artistSongDao = db.artistSongRelationDao()
        playlistSongDao = db.playlistSongRelationDao()
        recentDao = db.recentlyPlayedDao()
        mediaRepository = MediaRepository(playlistDao, songDao, artistDao, artistSongDao, playlistSongDao, recentDao)
        callbacks = MLCallbacks(mediaRepository)

        Log.d("LOOK AT ME", "LOOK AT ME")

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

        player.addListener(PCallbacks(mediaRepository))

        session = MediaLibrarySession.Builder(this, player, callbacks)
            .setId("MuseMP")
            .build()
        Log.d("LOOK AT ME", "LOOK AT ME 2")

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
        stopKoin()
        super.onDestroy()
    }
}


@UnstableApi
class MLCallbacks(val repo: MediaRepository): MediaLibrarySession.Callback {

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
        val contents:List<MediaItem> = id.getChildren(page, pageSize)
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
//        println("adding items")
        if (mediaItems.isEmpty()) return Futures.immediateFuture(listOf<MediaItem>())

        val fullMediaItems = when(mediaItems.first().mediaMetadata.isBrowsable) {
            true -> {
                runBlocking {
                    repo.setRecent(mediaItems.first().mediaId.toMuseMediaId(), 0)
                }
                onGetChildren(
                    mediaSession as MediaLibrarySession,
                    controller,
                    mediaItems.first().mediaId,
                    0, 1, null)
                    .get().value!!.map { it }
            }
            false -> {
                runBlocking {
                    repo.setRecent(mediaItems.first().mediaId.toMuseMediaId(), 0)
                }
                mediaItems.map { mediaItem ->
                    // Stupid dumb "security" conversion
                    // when media items come across the veil into this function they lose their Uri property
                    onGetItem(
                        mediaSession as MediaLibrarySession,
                        controller,
                        mediaItem.mediaId
                    ).get().value!!
                }
            }
            null -> TODO()
        }

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

        val (_, mid, pos) = runBlocking{ repo.getRecent() }
        val recentItem =
        this.onGetItem(
            mediaSession as MediaLibrarySession,
            controller,
            mid
        ).get().value



        return Futures.immediateFuture(
            when (recentItem!!.mediaMetadata.isBrowsable) {
                true -> MediaSession.MediaItemsWithStartPosition(
                    onGetChildren(mediaSession, controller, recentItem.mediaId, 0, 1, null).get().value as List<MediaItem>,
                    pos!!.toInt(), 0L
                )
                false -> MediaSession.MediaItemsWithStartPosition(
                    listOf(recentItem),
                    /* startIndex = */ 0,
                    /* startPositionMs = */ 0L
                )
                null -> TODO()
            }
        )
    }
}

class PCallbacks(private val repo: MediaRepository): Player.Listener {
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//        super.onMediaItemTransition(mediaItem, reason)

        when (reason) {
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                val (_, mid, pos) = runBlocking{ repo.getRecent() }
                runBlocking{ repo.setRecent(mid.toMuseMediaId(), pos?.plus(1)) }
            }

            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                // Don't care
            }

            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> {
                // Don't care
            }

            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                val (_, mid, pos) = runBlocking{ repo.getRecent() }
                runBlocking{ repo.setRecent(mid.toMuseMediaId(), pos?.plus(1)) }
            }
        }
    }

}