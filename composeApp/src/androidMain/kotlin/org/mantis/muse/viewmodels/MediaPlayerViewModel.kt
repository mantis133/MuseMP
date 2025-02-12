package org.mantis.muse.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.AndroidMediaPlayerState
import org.mantis.muse.util.LoopState
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song

sealed interface MediaPlayerUIState{
    data object Minimised: MediaPlayerUIState
    data class Expanded(var songListVisible: Boolean): MediaPlayerUIState
}

class MediaPlayerViewModel(
    private val player: AndroidMediaPlayer
): ViewModel() {

    private var _mediaPlayerExpanded: MutableStateFlow<MediaPlayerUIState> = MutableStateFlow(MediaPlayerUIState.Minimised)
    var mediaPlayerExpanded = _mediaPlayerExpanded.asStateFlow()

    fun toggleExpansion(state: MediaPlayerUIState) {
//        when (_mediaPlayerExpanded.value) {
//            is MediaPlayerUIState.Expanded -> _mediaPlayerExpanded.update { MediaPlayerUIState.Minimised }
//            is MediaPlayerUIState.Minimised -> _mediaPlayerExpanded.update { MediaPlayerUIState.Expanded(false) }
//        }
        _mediaPlayerExpanded.update { state }
    }
//    val g:Flow<Pair<AndroidMediaPlayerState.LoadedSong, MediaPlayerUIState>> = player.playerState.combine(mediaPlayerExpanded) { a, b ->
//        Pair(a, b)
//    }
//        .stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(stopTimeout = 5000),
//        initialValue = Pair(3,4)
//    )
    val uiState: StateFlow<AndroidMediaPlayerState.LoadedSong> = player.playerState.asStateFlow()

//    val uiState: StateFlow<MediaPlayerUIState> = mediaPlayerExpanded.map{
//        if (it) (MediaPlayerUIState.Expanded) else (MediaPlayerUIState.Minimised)
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
//        initialValue = MediaPlayerUIState.Minimised
//    )

    var currentSongIndex: Int by mutableIntStateOf(0)
    var currentPlaylist: Playlist? by mutableStateOf(null)
    var currentSong: Song? = null
        get() = currentPlaylist?.songList?.get(currentSongIndex)
        private set

    var playing: Boolean by mutableStateOf(false)
    var loopState: LoopState by mutableStateOf(LoopState.None)
    private var shuffling: Boolean by mutableStateOf(false)

    fun play(){
        player.play()
    }

    fun pause(){
        player.pause()
    }

    fun togglePlayPauseState(){
        if (uiState.value.playing) player.pause() else player.play()
    }

    fun skipNext(){
        player.skipNext()
    }

    fun skipLast(){
        player.skipLast()
    }

    fun seekToSong(queueIndex: Int) {
        player.skipToIndex(queueIndex)
    }

    fun seekToSong(song: Song) {
        TODO()
    }

    fun toggleShuffle(){
        this.shuffling = !this.shuffling
        player.setShuffle(this.shuffling)
    }

    fun nextLoopState(){
        player.loopState = when(player.loopState){
            LoopState.None -> LoopState.Single
            LoopState.Single -> LoopState.Full
            LoopState.Full -> LoopState.None
        }
        this.loopState = player.loopState
    }

    fun seekTo(position: Long) {
        player.trackPositionMS = position
    }
}