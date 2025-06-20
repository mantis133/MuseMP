package org.mantis.muse.viewmodels

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.media.MediaMetadataRetriever
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    val imageCashe: LruCache<String, ImageBitmap> = LruCache<String, ImageBitmap>(100)

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

    fun playSongs(songs: List<Song>) {
        val queuePlaylist: Playlist = runBlocking{ mediaRepository.getPlaylistByName("Queue") }!!
        viewModelScope.launch(Dispatchers.IO) {
            queuePlaylist.songList.forEachIndexed { idx, song ->
                mediaRepository.removeSongFromPlaylist(queuePlaylist, song, idx.toLong())
            }
            songs.forEachIndexed { idx, song ->
                mediaRepository.addSongToPlaylist(queuePlaylist, song, idx.toLong())
            }
        }
        browser.addListener({
            browser.get().apply {
                clearMediaItems()
                addMediaItem(browser.get().getItem("PLAYLISTQueue").get()?.value?:throw IllegalArgumentException("harahar"))
                prepare()
                play()
            }
        }, MoreExecutors.directExecutor())
    }



    suspend fun getSongArt(song: Song): ImageBitmap = withContext(Dispatchers.IO) {
        imageCashe[song.name]?: run {
            val bitmap = try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, song.fileUri)
                val data = mmr.embeddedPicture
                mmr.release()

                if (data != null) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(data, 0, data.size, options)

                    // Compute sample size for downscaling
                    options.inSampleSize = calculateInSampleSize(options, 256, 256)
                    options.inJustDecodeBounds = false
                    options.inPreferredConfig = Bitmap.Config.RGB_565 // more memory-efficient

                    BitmapFactory.decodeByteArray(data, 0, data.size, options)
                } else null
            } catch (e: Exception) {
                null
            } ?: BitmapFactory.decodeResource(context.resources, R.drawable.home_icon)

            val imageBitmap = bitmap.asImageBitmap()
            imageCashe.put(song.name, imageBitmap)
            imageBitmap
        }
    }
}

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1

    while ((height / inSampleSize) >= reqHeight && (width / inSampleSize) >= reqWidth) {
        inSampleSize *= 2
    }
    return inSampleSize
}
