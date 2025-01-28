package org.mantis.muse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.MediaPlayer
import org.mantis.muse.util.Playlist

sealed interface PlaylistsScreenUiState {
    data object Loading : PlaylistsScreenUiState
    data class Loaded(val playlists: List<Playlist>) : PlaylistsScreenUiState
}

class PlaylistPickerViewModel(
    private val localFiles: LocalFileSource,
    private val playlistsRepo: PlaylistRepository,
    private val player: MediaPlayer
    // val songRepo: SongRepository
): ViewModel() {

    val availablePlaylists: Flow<List<Playlist>> = playlistsRepo.playlistStream

    val uiState: StateFlow<PlaylistsScreenUiState> = availablePlaylists.map { playlists ->
        PlaylistsScreenUiState.Loaded(playlists)
    }.stateIn(
        scope = viewModelScope,
        initialValue = PlaylistsScreenUiState.Loading,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun loadPlaylist(playlist: Playlist) = viewModelScope.launch {
        println("load playlist ${playlist.name}")
        if (playlist.songList.isNotEmpty()) player.loadSong(playlist.songList[0])
    }

    fun updateCaches() = viewModelScope.launch {  }

    fun newPlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.addNewPlaylist(playlist) }

    fun removePlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.removePlaylist(playlist) }
}