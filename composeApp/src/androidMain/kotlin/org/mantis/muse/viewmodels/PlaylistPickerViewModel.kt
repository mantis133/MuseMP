package org.mantis.muse.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.util.Playlist

sealed interface PlaylistsScreenUiState {
    data object Loading : PlaylistsScreenUiState
    data class Loaded(val playlists: List<Playlist>) : PlaylistsScreenUiState
}

@UnstableApi
class PlaylistPickerViewModel(
    private val mediaRepository: MediaRepository,
    private val context: Application
): ViewModel() {

    private val browser = MediaBrowser.Builder(
        context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java)),
    ).buildAsync()

    val uiState: StateFlow<PlaylistsScreenUiState> = mediaRepository.playlistsStream.map { playlists ->
        PlaylistsScreenUiState.Loaded(playlists)
    }.stateIn(
        scope = viewModelScope,
        initialValue = PlaylistsScreenUiState.Loading,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun playPlaylist(playlist: Playlist) = viewModelScope.launch {
        browser.addListener ({
            browser.get().apply {
                clearMediaItems()
                addMediaItem(browser.get().getItem("PLAYLIST"+playlist.name).get()?.value?:throw IllegalArgumentException("harahar"))
                prepare()
                play()
            }
        }, MoreExecutors.directExecutor())
    }

    fun playPlaylistFromSong(playlist: Playlist, songIndex: Int){
        browser.addListener ({
            browser.get().apply {
                clearMediaItems()
                addMediaItem(browser.get().getItem("PLAYLIST"+playlist.name).get()?.value?:throw IllegalArgumentException("harahar"))
                seekTo(songIndex, 0)
                prepare()
                play()
            }
        }, MoreExecutors.directExecutor())
    }

    fun getPlaylist(playlistName: String): Playlist {
        return runBlocking { mediaRepository.getPlaylistByName(playlistName)!! }
    }

//    fun updateCaches() = viewModelScope.launch {  }

//    fun newPlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.addNewPlaylist(playlist) }
//
//    fun removePlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.removePlaylist(playlist) }
}