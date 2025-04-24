package org.mantis.muse.viewmodels

import android.app.Application
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.media.Image
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import org.mantis.muse.R
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.MediaId
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.toAlbumArt
import org.mantis.muse.util.toId

sealed interface SongsScreenUiState {
    data object Loading : SongsScreenUiState
    data class Loaded(val songs: List<Song>) : SongsScreenUiState
}

@UnstableApi
class SongPickerViewModel(
    private val mediaRepository: MediaRepository,
    private val context: Application
): ViewModel() {
    private val browser = MediaBrowser.Builder(
        context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java)),
    ).buildAsync()

    val uiState: StateFlow<SongsScreenUiState> = mediaRepository.songsStream.map { songs ->
        SongsScreenUiState.Loaded(songs)
    }.stateIn(
        scope = viewModelScope,
        initialValue = SongsScreenUiState.Loading,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun playSong(song: Song) = viewModelScope.launch {
        browser.addListener({
            browser.get().apply {
                clearMediaItems()
                addMediaItem(browser.get().getItem(MediaId.Song(song.name).toId()).get()?.value?:throw IllegalArgumentException("harahar"))
                prepare()
                play()
            }
        }, MoreExecutors.directExecutor())
    }

    val imageCashe: LruCache<String, ImageBitmap> = LruCache<String, ImageBitmap>(100)


    fun getSongArt(song: Song): ImageBitmap {
        if (imageCashe[song.name] == null) {
            imageCashe.put(song.name, song.toAlbumArt()?.asImageBitmap()?:BitmapFactory.decodeResource(
                context.resources,
                R.drawable.home_icon
            ).asImageBitmap())
        }
        return imageCashe[song.name]!!
    }
}