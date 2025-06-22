package org.mantis.muse.viewmodels

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song

data class PlaylistCreationState(
    val loading: Boolean,
    val playlists: List<Playlist>,
    val songs: List<Song>
)

class PlaylistCreationViewModel(
    val mediaRepository: MediaRepository
): ViewModel() {

    private var _uiState: MutableStateFlow<PlaylistCreationState> = MutableStateFlow(
        PlaylistCreationState(
            loading = true,
            playlists = emptyList(),
            songs = emptyList()
        )
    )
    val uiState: StateFlow<PlaylistCreationState> = _uiState.asStateFlow()

    fun loadSongs(songNames: List<String>) {
        viewModelScope.launch {
            _uiState.update { uiState.value.copy(loading = true) }
            val songs = withContext(Dispatchers.IO) { songNames.mapNotNull { songName -> mediaRepository.getSongByName(songName) } }
            val playlists: List<Playlist> = withContext(Dispatchers.IO) { mediaRepository.playlistsStream.first() }
            _uiState.update { uiState.value.copy(loading = false, songs = songs, playlists = playlists) }
        }
    }


    fun createPlaylist(playlistName: String, thumbnailUri: Uri?) {
        viewModelScope.launch {
            val createdPlaylist = Playlist(playlistName, emptyList(), "NULL".toUri(), thumbnailUri)
            mediaRepository.insertPlaylist(createdPlaylist)
//            val playlist = mediaRepository.getPlaylistByName(playlistName)
            uiState.value.songs.onEachIndexed { idx, song ->
                mediaRepository.addSongToPlaylist(createdPlaylist, song, idx.toLong())
            }
        }
    }

    fun addSongsToPlaylist(playlistName: String) {
        viewModelScope.launch(Dispatchers.IO){
            val playlist = mediaRepository.getPlaylistByName(playlistName)
            var idx: Long = playlist!!.size.toLong()
            uiState.value.songs.onEach { song -> mediaRepository.addSongToPlaylist(playlist, song, idx.toLong()); idx += 1 }
        }
    }


}