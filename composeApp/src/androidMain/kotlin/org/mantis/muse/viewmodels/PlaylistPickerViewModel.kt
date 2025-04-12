package org.mantis.muse.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.cheapFromURI
import org.mantis.muse.util.fromURI

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

    fun loadPlaylist(playlist: Playlist) = viewModelScope.launch {
        val songs = mediaRepository.getSongsByPlaylist(playlist)
//        val pmi: MediaItem = browser.get().getItem("PLAYLISTAllSongs").get()?.value?:throw IllegalArgumentException()
//        println(mi.mediaId)
//        val mediaSongs: List<MediaItem> = browser.get().getChildren(pmi.mediaId, 1, 1, null).get().value!!
//        println(mediaSongs)
        browser.addListener ({
            browser.get().apply {
                clearMediaItems()
//                addMediaItems(mediaSongs)
                songs.forEach { song->
                    addMediaItem(browser.get().getItem("SONG" + song.name).get()?.value?:throw IllegalArgumentException("harahar"))
                }
                prepare()
                play()
            }
        }, MoreExecutors.directExecutor())
    }

//    fun updateCaches() = viewModelScope.launch {  }

//    fun newPlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.addNewPlaylist(playlist) }
//
//    fun removePlaylist(playlist: Playlist) = viewModelScope.launch { playlistsRepo.removePlaylist(playlist) }
}