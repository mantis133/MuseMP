package org.mantis.muse.util

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

sealed interface AndroidMediaPlayerState {
    data class LoadedSong(
        val albumArt: ImageBitmap?,
        val queue: List<Song>,
        var queuePosition: Int,
        var playing: Boolean,
        var shuffling: Boolean,
        var loopState: LoopState,
        var trackPositionMS: Long,
        var trackDurationMS: Long
    ): AndroidMediaPlayerState
//    data object NoConnection: AndroidMediaPlayerState
//    data object Empty: AndroidMediaPlayerState
}


class AndroidMediaPlayer(
    var mediaConn: MediaController?,
    val context: Context
) {
    private var queue: MutableList<Song> = mutableListOf()

    var loopState: LoopState
        get() {return when(mediaConn?.repeatMode){
            Player.REPEAT_MODE_OFF -> LoopState.None
            Player.REPEAT_MODE_ONE -> LoopState.Single
            Player.REPEAT_MODE_ALL -> LoopState.Full
            else -> LoopState.None
        }}
        set(state) {
            mediaConn?.repeatMode = when(state) {
                LoopState.None -> Player.REPEAT_MODE_OFF
                LoopState.Single -> Player.REPEAT_MODE_ONE
                LoopState.Full -> Player.REPEAT_MODE_ALL
            }
            syncWithExoPlayer()
        }

    var trackPositionMS: Long
        get() { mediaConn?.let{syncWithExoPlayer()};return mediaConn?.currentPosition?:1L }
        set(pos) { this.seekPosition(pos)}

    private val onChangeCallbacks: MutableList<() -> Unit> = mutableListOf()

    @OptIn(ExperimentalResourceApi::class)
    val playerState: MutableStateFlow<AndroidMediaPlayerState.LoadedSong> = MutableStateFlow(
        AndroidMediaPlayerState.LoadedSong(
            albumArt = mediaConn?.currentMediaItem?.mediaMetadata?.artworkData?.decodeToImageBitmap(),
            queue = mutableListOf(),
            queuePosition = 0,
            playing = false,
            shuffling = false,
            loopState = LoopState.None,
            trackPositionMS = 0L,
            trackDurationMS = 0L
        )
    )

    fun play() {
        if (mediaConn?.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) == true){
            mediaConn?.play().let {
                syncWithExoPlayer()
            }
        }
    }

    fun pause() {
        if (mediaConn?.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) == true){
            mediaConn?.pause().let{
                syncWithExoPlayer()
            }
        }
    }

    fun skipNext() {
        mediaConn?.seekToNextMediaItem().let {
            syncWithExoPlayer()
        }
    }

    fun skipLast() {
        mediaConn?.seekToPreviousMediaItem().let {
            syncWithExoPlayer()
        }
    }

    fun skipToIndex(queueIndex: Int) {
        mediaConn?.seekTo(queueIndex,0).let {
            syncWithExoPlayer()
        }
    }

    fun setShuffle(shuffling: Boolean) {
        val queueToLoad = if (shuffling) queue.shuffled() else queue
        mediaConn?.clearMediaItems()
        for (song in queueToLoad) {
            mediaConn?.addMediaItem(song.toMediaItem())
        }
        playerState.update { it.copy(shuffling = shuffling, queue = queueToLoad) }
        syncWithExoPlayer()
    }

    private fun seekPosition(position: Long) {
        mediaConn?.seekTo(position)
    }

    fun clearQueue() {
        mediaConn?.clearMediaItems().let {
            queue = emptyList<Song>().toMutableList()
            playerState.update { it.copy(queue = emptyList()) }
        }
    }

    fun loadSong(song: Song) {
        mediaConn?.addMediaItem(song.toMediaItem())?.let {
            queue.add(song)
            playerState.update { it.copy(queue = it.queue + song) }
        }
        mediaConn?.prepare()
    }

    private fun syncWithExoPlayer() {
        playerState.update {
            it.copy(
                queuePosition = mediaConn?.currentMediaItemIndex?:0,
                playing = mediaConn?.isPlaying?:false,
                loopState = when (mediaConn?.repeatMode){
                    Player.REPEAT_MODE_OFF -> {LoopState.None}
                    Player.REPEAT_MODE_ONE -> {LoopState.Single}
                    Player.REPEAT_MODE_ALL -> {LoopState.Full}
                    else -> {LoopState.None}
                },
                trackPositionMS = mediaConn?.currentPosition?:0L,
                trackDurationMS = if (mediaConn != null && mediaConn!!.duration > 0 ) mediaConn!!.duration else 1000L
            )
        }
    }
}