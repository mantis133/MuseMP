package org.mantis.muse.viewmodels

import android.app.Application
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.media.Image
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.SparseArray
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.java.KoinJavaComponent.inject
import org.mantis.muse.R
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.AndroidMediaPlayerState
import org.mantis.muse.util.LoopState
import org.mantis.muse.util.MediaId
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.toAlbumArt
import org.mantis.muse.util.toMuseMediaId

sealed interface MediaPlayerUIState{
    data object Empty: MediaPlayerUIState
    data class LoadedSong(val state: PlayerUiState): MediaPlayerUIState
}

data class PlayerUiState (
    val expanded: Boolean,
    val songTitle: String,
    val songArtists: String,
    val isPlaying: Boolean,
    val loopState: LoopState,
    val shuffling: Boolean,
    val trackPosition: Long,
    val trackDuration: Long,

)

@UnstableApi
class MediaPlayerViewModel(
    private val app: Application,
    private val mediaRepository: MediaRepository
): ViewModel() {

    private val artArray: MutableMap<String, ImageBitmap> = mutableMapOf()

    private var _mediaPlayerExpanded: MutableStateFlow<MediaPlayerUIState> = MutableStateFlow(MediaPlayerUIState.Empty)
    var uiState = _mediaPlayerExpanded.asStateFlow()

    fun toggleExpansion() {
        if (_mediaPlayerExpanded.value is MediaPlayerUIState.Empty) return
        if (_mediaPlayerExpanded.value is MediaPlayerUIState.LoadedSong){
            when ((_mediaPlayerExpanded.value as MediaPlayerUIState.LoadedSong).state.expanded) {
                true -> _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state = (_mediaPlayerExpanded.value as MediaPlayerUIState.LoadedSong).state.copy(expanded = false)) }
                false -> _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state = (_mediaPlayerExpanded.value as MediaPlayerUIState.LoadedSong).state.copy(expanded = true)) }
            }
        }
    }

    fun setLoadedSong(){
        _mediaPlayerExpanded.update {
            MediaPlayerUIState.LoadedSong(PlayerUiState(
                expanded = false,
                songTitle = "",
                songArtists = "",
                isPlaying = false,
                loopState = LoopState.None,
                shuffling = false,
                trackPosition = 0,
                trackDuration = 1_000_000
            ))
        }
    }

    val mediaBrowser: ListenableFuture<MediaBrowser> = MediaBrowser.Builder(
        app,
        SessionToken(app, ComponentName(app, PlaybackService::class.java))
    ).buildAsync()

    fun getArt(): ImageBitmap {
        return getArt(mediaBrowser.get().currentMediaItemIndex)
    }

    fun getArt(idx: Int): ImageBitmap{
        val key:String = (mediaBrowser.get().getMediaItemAt(idx).mediaId.toMuseMediaId() as MediaId.Song).selector
        if (artArray[key] == null) {
            mediaBrowser.get().apply {
                val song = runBlocking(Dispatchers.IO){ mediaRepository.getSongByName(key) }
                if (song == null) {
                    val img = BitmapFactory.decodeResource(
                        app.resources,
                        R.drawable.home_icon
                    ).asImageBitmap()
                    return img
                } else {
                    song.toAlbumArt()?.let {
                        artArray[key] = it.asImageBitmap()
                        return it.asImageBitmap()
                    }
                    return BitmapFactory.decodeResource(
                        app.resources,
                        R.drawable.home_icon
                    ).asImageBitmap()
                }
            }
        } else {
            return artArray[key]!!
        }
    }

    fun getTrackPosition(): Long {
        println(mediaBrowser.get().currentPosition)
        return mediaBrowser.get().currentPosition
    }

    init{
        mediaBrowser.addListener({
            mediaBrowser.get().addListener(object: Player.Listener{
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        super.onPlaybackStateChanged(playbackState)
//                        if(playbackState == Player.STATE_READY) {
////                            songPosition = conn!!.currentPosition.toInt()
////                            Log.d("time", conn!!.currentPosition.toString())
//
//                        }
//                    }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (_mediaPlayerExpanded.value is MediaPlayerUIState.Empty) {
                        setLoadedSong()
                    }
                    else {
                        val state = (_mediaPlayerExpanded.value as MediaPlayerUIState.LoadedSong).state
                        _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state.copy(isPlaying = isPlaying)) }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    super.onRepeatModeChanged(repeatMode)
                    val state = (_mediaPlayerExpanded.value as MediaPlayerUIState.LoadedSong).state
                    when (repeatMode){
                        Player.REPEAT_MODE_OFF -> _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state.copy(loopState = LoopState.None)) }
                        Player.REPEAT_MODE_ONE -> _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state.copy(loopState = LoopState.Single)) }
                        Player.REPEAT_MODE_ALL -> _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state.copy(loopState = LoopState.Full)) }
                    }
                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    if (_mediaPlayerExpanded.value is MediaPlayerUIState.Empty) {
                        setLoadedSong()

                    }
                    else {
                        val state = (_mediaPlayerExpanded.value as MediaPlayerUIState.LoadedSong).state
                        _mediaPlayerExpanded.update { MediaPlayerUIState.LoadedSong(state.copy(
                            songTitle = mediaMetadata.title.toString(),
                            songArtists = mediaMetadata.artist.toString(),
//                            trackDuration = mediaMetadata.durationMs!!,
                        )) }
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

//    val mediaBrowser: ListenableFuture<MediaBrowser> by inject(MediaBrowser::class.java, qualifier = named("browserFuture"))
    fun unloadSong(){
        _mediaPlayerExpanded.update { MediaPlayerUIState.Empty }
    }

    fun play(){
        mediaBrowser.get().play()
    }

    fun pause(){
        mediaBrowser.get().pause()
    }

    fun togglePlayPauseState(){
        mediaBrowser.get().apply {
            if (isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }

    fun skipNext(){
        mediaBrowser.get().apply {
            seekToNext()
        }
    }

    fun skipLast(){
        getArt()
        mediaBrowser.get().apply {
            seekToPrevious()
        }
    }

    fun seekToSong(queueIndex: Int) {
//        player.skipToIndex(queueIndex)
    }

    fun seekToSong(song: Song) {
        TODO()
    }

    fun toggleShuffle(){
//        this.shuffling = !this.shuffling
//        player.setShuffle(this.shuffling)
    }

    fun nextLoopState(){
        mediaBrowser.get().apply {
            repeatMode = when(repeatMode){
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun seekTo(position: Long) {
//        player.trackPositionMS = position
    }
}