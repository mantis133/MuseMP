package org.mantis.muse.util

import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController

class AndroidMediaPlayer(
    var mediaConn: MediaController?
) : MediaPlayer {
    override var currentSong: Song
        get() = queue[queuePosition]
        set(value) {
            val idx = queue.indexOf(value)
            if (idx >= 0) queuePosition = idx
        }

    override var queue: List<Song> = listOf()
    private var queuePosition: Int = 0
    override var playingState: Boolean = false
    override var loopState: LoopState = LoopState.None
    override var shuffleState: Boolean = false

    private val onChangeCallbacks: MutableList<() -> Unit> = mutableListOf()

    override fun play() {
        println("player is playing")
        mediaConn?.play().let {
            playingState = true
        }
    }

    override fun pause() {
        println("player is paused")
        mediaConn?.pause().let{
            playingState = false
        }
    }

    override fun skipNext() {
        println("player is skipping to the next song")
        mediaConn?.seekToNext().let {
            queuePosition += 1
        }
    }

    override fun skipLast() {
        TODO("Not yet implemented")
    }

    override fun setShuffle(shuffling: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setLooping(state: LoopState) {
        TODO("Not yet implemented")
    }

    override fun seekPosition(position: Int): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun clearQueue() {
        TODO("Not yet implemented")
    }

    override fun loadSong(song: Song) {
        println("loading song: ${song.name}")
        val songMediaItem = song.toMediaItem()
        println("song info artist: ${songMediaItem.mediaMetadata.artist}")
        mediaConn?.addMediaItem(song.toMediaItem())
        mediaConn?.prepare()
    }

    override fun addOnChangeListener(callback: () -> Unit) {
        if (!onChangeCallbacks.contains(callback)) onChangeCallbacks.add(callback)
    }
}