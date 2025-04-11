package org.mantis.muse.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.cheapFromURI
import org.mantis.muse.util.fromURI

sealed interface PlaylistsScreenUiState {
    data object Loading : PlaylistsScreenUiState
    data class Loaded(val playlists: List<Playlist>) : PlaylistsScreenUiState
}

class PlaylistPickerViewModel(
    private val localFiles: LocalFileSource,
    private val mediaRepository: MediaRepository,
    private val player: AndroidMediaPlayer
    // val songRepo: SongRepository
): ViewModel() {

    val uiState: StateFlow<PlaylistsScreenUiState> = mediaRepository.playlistsStream.map { playlists ->
        PlaylistsScreenUiState.Loaded(playlists)
    }.stateIn(
        scope = viewModelScope,
        initialValue = PlaylistsScreenUiState.Loading,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun loadPlaylist(playlist: Playlist) = viewModelScope.launch {
//        player.clearQueue()
        val songs = mediaRepository.getSongsByPlaylist(playlist)
        println("LOADED PLAYLIST: $songs")
//        if (loadablePlaylist.songList.isNotEmpty()) loadablePlaylist.songList.forEach { player.loadSong(it); yield() }
    }

//    fun updateCaches() = viewModelScope.launch {  }

//    fun newPlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.addNewPlaylist(playlist) }
//
//    fun removePlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.removePlaylist(playlist) }
}