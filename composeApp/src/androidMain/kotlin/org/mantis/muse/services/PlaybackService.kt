package org.mantis.muse.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.room.Room
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.server.websocket.WebSockets
import io.ktor.server.application.*
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mantis.muse.network.PlayerCommand
import org.mantis.muse.network.RemotePlayer
import org.mantis.muse.network.json
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.storage.dao.ArtistDao
import org.mantis.muse.storage.dao.ArtistSongRelationshipDao
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.PlaylistSongRelationshipDao
import org.mantis.muse.storage.dao.RecentlyPlayedDao
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.util.LoopState
import org.mantis.muse.util.MediaId.Root
import org.mantis.muse.util.PlayerState
import org.mantis.muse.util.toMuseMediaId
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

@UnstableApi
class PlaybackService : MediaLibraryService() {

    private var session : MediaLibrarySession? = null
    private lateinit var player : ExoPlayer
    private lateinit var db: MusicCacheDB
    private lateinit var playlistDao: PlaylistDAO
    private lateinit var songDao: SongDao
    private lateinit var artistDao: ArtistDao
    private lateinit var artistSongDao: ArtistSongRelationshipDao
    private lateinit var playlistSongDao: PlaylistSongRelationshipDao
    private lateinit var recentDao: RecentlyPlayedDao
    private lateinit var mediaRepository: MediaRepository
    private lateinit var callbacks: MLCallbacks

    private lateinit var server: Job

    val intentReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action){
                Intent.ACTION_HEADSET_PLUG -> {
                    when(intent.getIntExtra("state",-1)){
                        0 -> {/*headphones unplugged*/
                            session?.player?.pause()
                        }
                        1 -> {/*headphones plugged in*/

                        }
                    }
//                        session?.player?.pause()
                    println("HEADPHONE ACTION")
                }
                
            }
        }
    }

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
            .fallbackToDestructiveMigration(true)
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
        

        player = ExoPlayer.Builder(this)

            .build()

        val commandChannel = Channel<PlayerCommand>(UNLIMITED)

        player.addListener(PCallbacks(mediaRepository, player, commandChannel))

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

        server = CoroutineScope(Dispatchers.IO).launch {
            val port = 19742
            embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
            println("EXITING SERVER")
        }
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
        unregisterReceiver(intentReceiver)
        super.onDestroy()
//        remotePlayer.close()
        server.cancel()
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

class PCallbacks(private val repo: MediaRepository, private val player: Player, private val commandChannel: Channel<PlayerCommand>): Player.Listener {
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
                val direction = pos?.minus(player.currentMediaItemIndex)?.times(-1)
                runBlocking{
                    repo.setRecent(mid.toMuseMediaId(), pos?.plus(direction?:0))
                }
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
        val newState = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> LoopState.None
            Player.REPEAT_MODE_ONE -> LoopState.Single
            Player.REPEAT_MODE_ALL -> LoopState.Full
            else -> LoopState.None
        }
        commandChannel.trySend(PlayerCommand.UpdateState(PlayerState(loopState = newState)))
    }
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val connections = ConcurrentHashMap.newKeySet<DefaultWebSocketServerSession>()
        webSocket("/") {
            connections += this
            for (frame in incoming) {
                println(frame)
                if (frame is Frame.Text) {
                    val command = json.decodeFromString<PlayerCommand>(frame.readText())
                    val response = when (command) {
                        PlayerCommand.Play -> {
                            PlayerCommand.UpdateState(PlayerState(playing = true))
                        }
                        PlayerCommand.Pause -> TODO()
                        PlayerCommand.SkipLast -> TODO()
                        PlayerCommand.SkipNext -> TODO()
                        is PlayerCommand.SeekPosition -> TODO()
                        PlayerCommand.RequestState -> TODO()
                        is PlayerCommand.UpdateState -> TODO()
                    }
                    for (connection in connections) {
                        connection.send(Frame.Text(json.encodeToString<PlayerCommand>(response)))
                    }
                }
            }
        }
        get("/ping") {
            call.respondText("pong")
        }
    }
}
