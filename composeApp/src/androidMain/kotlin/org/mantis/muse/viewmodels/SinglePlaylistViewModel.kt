package org.mantis.muse.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song

private data class UiExtras (
    val editing: Boolean = false,
    val selectedSongs: Set<Int> = emptySet()
)

sealed interface SinglePlaylistViewState{
    data class Loaded(
        val playlist: Playlist,
        val editing: Boolean,
        val selectedSongIndices: Set<Int>,
    ): SinglePlaylistViewState
    data object Loading: SinglePlaylistViewState
}

@UnstableApi
class SinglePlaylistViewModel(
    val playlistName: String,
    val mediaRepository: MediaRepository,
    val context: Application
): ViewModel() {

    private val browser = MediaBrowser.Builder(
        context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java)),
    ).buildAsync()

    private val _uiState = MutableStateFlow(UiExtras())

    val uiState: StateFlow<SinglePlaylistViewState> = mediaRepository
        .getPlaylistByName(playlistName)
        .filterNotNull()
        .combine(_uiState) { playlist, extras -> SinglePlaylistViewState.Loaded(playlist, extras.editing, extras.selectedSongs) }
        .stateIn(
            scope = viewModelScope,
            initialValue = SinglePlaylistViewState.Loading,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L)
        )

    fun playPlaylist(){
        if (uiState.value is SinglePlaylistViewState.Loaded) {
            val state = (uiState.value as SinglePlaylistViewState.Loaded)
            browser.addListener ({
                browser.get().apply {
                    clearMediaItems()
                    addMediaItem(browser.get().getItem("PLAYLIST"+ state.playlist.name).get()?.value?:throw IllegalArgumentException("harahar"))
                    prepare()
                    play()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun playPlaylistFromPosition(position: Int){
        if (uiState.value is SinglePlaylistViewState.Loaded) {
            val state = (uiState.value as SinglePlaylistViewState.Loaded)
            browser.addListener({
                browser.get().apply {
                    clearMediaItems()
                    addMediaItem(
                        browser.get().getItem("PLAYLIST" + state.playlist.name).get()?.value
                            ?: throw IllegalArgumentException("harahar")
                    )
                    seekTo(position, 0)
                    prepare()
                    play()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun setEditPlaylistState(editing: Boolean) {
        _uiState.update { it.copy(editing) }
    }

    fun moveSong(source: Int, destination: Int) {
        if (uiState.value is SinglePlaylistViewState.Loaded) {
            val state = (uiState.value as SinglePlaylistViewState.Loaded)
            val songs = state.playlist.songList.toMutableList()
            val song = songs.removeAt(source)
            songs.add(destination, song)
            // TODO
        }
    }

    fun setSongSelection(songIndex: Int, state: Boolean) {
        _uiState.update { it.copy(selectedSongs = _uiState.value.selectedSongs.toMutableSet().apply {
            if (state) {
                add(songIndex)
            } else {
                remove(songIndex)
            }
        }) }
    }

    fun removeSong(position: Long){
        if (uiState.value !is SinglePlaylistViewState.Loaded){
            return
        }
        viewModelScope.launch(Dispatchers.IO){
            val state = uiState.value as SinglePlaylistViewState.Loaded
            mediaRepository.removeSongFromPlaylist(
                playlist = state.playlist,
                song = state.playlist.songList[position.toInt()],
                position
            )
        }
    }

    fun removeSelectedSongs() {
        if (uiState.value !is SinglePlaylistViewState.Loaded){
            return
        }
        val state = uiState.value as SinglePlaylistViewState.Loaded
        viewModelScope.launch(Dispatchers.IO){
            state.selectedSongIndices.forEach { songIdx ->
                val state = uiState.value as SinglePlaylistViewState.Loaded
                mediaRepository.removeSongFromPlaylist(
                    playlist = state.playlist,
                    song = state.playlist.songList[songIdx],
                    songIdx.toLong()
                )
                setSongSelection(songIdx, false)
            }
            mediaRepository.deFragmentPlaylist(state.playlist)
        }

    }
}