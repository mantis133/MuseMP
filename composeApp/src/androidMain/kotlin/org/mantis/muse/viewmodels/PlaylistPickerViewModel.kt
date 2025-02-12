package org.mantis.muse.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.fromURI

sealed interface PlaylistsScreenUiState {
    data object Loading : PlaylistsScreenUiState
    data class Loaded(val playlists: List<Playlist>) : PlaylistsScreenUiState
}

class PlaylistPickerViewModel(
    private val localFiles: LocalFileSource,
    private val playlistsRepo: PlaylistRepository,
    private val player: AndroidMediaPlayer
    // val songRepo: SongRepository
): ViewModel() {

    val uiState: StateFlow<PlaylistsScreenUiState> = playlistsRepo.playlistStream.map { playlists ->
        PlaylistsScreenUiState.Loaded(playlists)
    }.stateIn(
        scope = viewModelScope,
        initialValue = PlaylistsScreenUiState.Loading,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun loadPlaylist(playlist: Playlist) = viewModelScope.launch {
        player.clearQueue()
        val loadablePlaylist = Playlist.Companion.fromURI(playlist.fileURI)
        if (loadablePlaylist.songList.isNotEmpty()) loadablePlaylist.songList.forEach { player.loadSong(it) }
    }

    fun updateCaches() = viewModelScope.launch {  }

    fun newPlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.addNewPlaylist(playlist) }

    fun removePlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.removePlaylist(playlist) }
}