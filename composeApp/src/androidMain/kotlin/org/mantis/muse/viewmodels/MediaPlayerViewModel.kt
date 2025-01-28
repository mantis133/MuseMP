package org.mantis.muse.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.mantis.muse.util.LoopState
import org.mantis.muse.util.MediaPlayer
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import kotlin.math.max
import kotlin.math.min

class MediaPlayerViewModel(
    private val player: MediaPlayer
): ViewModel() {
    var currentSongIndex: Int by mutableIntStateOf(0)
    var currentPlaylist: Playlist? by mutableStateOf(null)
    var currentSong: Song? = null
        get() = currentPlaylist?.songList?.get(currentSongIndex)
        private set

    var playing: Boolean by mutableStateOf(false)
    var loopState: LoopState by mutableStateOf(LoopState.None)
    var shuffling: Boolean by mutableStateOf(false)

    fun play(){
        this.playing = true
        player.play()
    }

    fun pause(){
        this.playing = false
        player.pause()
    }

    fun togglePlayPauseState(){
        this.playing = !this.playing
        if (playing) player.play() else player.pause()
    }

    fun skipNext(){
        if (currentPlaylist == null) return
        currentSongIndex = min(currentSongIndex+1, currentPlaylist!!.size)
        player.skipNext()
    }

    fun skipLast(){
        if (currentPlaylist == null) return
        currentSongIndex = max(currentSongIndex-1, 0)
        player.skipLast()
    }

    fun toggleShuffle(){
        this.shuffling = !this.shuffling
        player.setShuffle(this.shuffling)
    }

    fun nextLoopState(){
//        this.loopState = state
//        player.setLooping(this.loopState)
    }
}