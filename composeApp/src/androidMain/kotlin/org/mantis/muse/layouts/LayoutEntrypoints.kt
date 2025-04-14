package org.mantis.muse.layouts

import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.R
import org.mantis.muse.util.toAlbumArt
import org.mantis.muse.viewmodels.MediaPlayerUIState
import org.mantis.muse.viewmodels.MediaPlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerUI(
    modifier: Modifier = Modifier,
    viewModel: MediaPlayerViewModel = koinViewModel<MediaPlayerViewModel>()
) {
    val res = LocalContext.current.resources
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val expansionState by viewModel.mediaPlayerExpanded.collectAsStateWithLifecycle()

    var fogColor: Color by remember{mutableStateOf(Color.Transparent)}
    LaunchedEffect(fogColor) {
        fogColor = sampleImage(BitmapFactory.decodeResource(res, R.drawable.home_icon))
    }



    when (uiState) {
        MediaPlayerUIState.Empty -> {
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)) {
                MinimisedMediaPlayerUI(
                    image = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.home_icon).asImageBitmap(),
                    songName = "Nothing to see here",
                    artistName = "No one",
                    playing = false,
                    playPauseOnClick = viewModel::togglePlayPauseState,
                    skipNextOnClick = viewModel::skipNext,
                    fogColor = fogColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }
        is MediaPlayerUIState.LoadedSong -> {
            val state = (uiState as MediaPlayerUIState.LoadedSong).state
            var trackPosition by remember { mutableLongStateOf(state.trackPosition) }

            LaunchedEffect(true) {
                println("Launched Effect Started")
                while (true) {
                    trackPosition = viewModel.getTrackPosition()
                    delay(1000L)
                }
            }
            when (state.expanded){
                false ->
                    Box(
                        contentAlignment = Alignment.BottomCenter, modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        MinimisedMediaPlayerUI(
                            image = viewModel.getArt(),
                            songName = state.songTitle,
                            artistName = state.songArtists,
                            playing = state.isPlaying,
                            playPauseOnClick = viewModel::togglePlayPauseState,
                            skipNextOnClick = viewModel::skipNext,
                            fogColor = fogColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clickable { viewModel.toggleExpansion() }
                        )
                    }
                true ->
                    ExpandedMediaPlayerUI(
                        songName = state.songTitle,
                        artistsName = state.songArtists,
                        getSongThumbnail = viewModel::getArt,
                        trackPosition = trackPosition,
                        trackDurationMS = state.trackDuration,
                        playing = state.isPlaying,
                        loopState = state.loopState,
                        shuffleState = state.shuffling,
                        togglePlayPause = viewModel::togglePlayPauseState,
                        skipLast = viewModel::skipLast,
                        skipNext = viewModel::skipNext,
                        nextLoopState = viewModel::nextLoopState,
                        toggleShuffle = viewModel::toggleShuffle,
                        onSeek = {pos:Long -> viewModel.seekTo(pos)},
                        modifier = Modifier
                            .clickable { viewModel.toggleExpansion() }
                    )
//                    Button(onClick = {viewModel.toggleExpansion(MediaPlayerUIState.Expanded(true))}) { Text(text = "open queue") }
            }
        }
    }


//    var trackPosition by remember { mutableLongStateOf(uiState.trackPositionMS) }
//    var duration: Long by remember { mutableLongStateOf(uiState.trackDurationMS) }

//    val player = koinInject<AndroidMediaPlayer>()

//    val currentSong = if (uiState.queue.size > uiState.queuePosition) uiState.queue[uiState.queuePosition] else null
//    val songThumbnail = currentSong?.toAlbumArt()?.asImageBitmap()?:BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.home_icon).asImageBitmap()

//    when (expansionState) {
//        is MediaPlayerUIState.Expanded -> {
//            when ((expansionState as MediaPlayerUIState.Expanded).songListVisible){
//                true -> {
//                    Column{
//                        Button(onClick = {
//                            viewModel.toggleExpansion(
//                                MediaPlayerUIState.Expanded(
//                                    false
//                                )
//                            )
//                        }) { Text(text = "Close") }
//                        SongQueue(
//                            songQueue = uiState.queue,
//                            currentQueueItemIndex = uiState.queuePosition,
//                            selectSong = {idx -> viewModel.seekToSong(queueIndex = idx)},
//                            modifier = Modifier
//                                .fillMaxWidth()
//                        )
//                    }
//                }
//                false -> {
//                    ExpandedMediaPlayerUI(
//                        songName = songName,
//                        artistsName = songArtists,
//                        songThumbnail = songThumbnail,
//                        trackPosition = trackPosition,
//                        trackDurationMS = uiState.trackDurationMS,
//                        playing = uiState.playing,
//                        loopState = uiState.loopState,
//                        shuffleState = uiState.shuffling,
//                        togglePlayPause = viewModel::togglePlayPauseState,
//                        skipLast = viewModel::skipLast,
//                        skipNext = viewModel::skipNext,
//                        nextLoopState = viewModel::nextLoopState,
//                        toggleShuffle = viewModel::toggleShuffle,
//                        onSeek = {pos:Long -> viewModel.seekTo(pos)},
//                        modifier = Modifier
//                            .clickable { viewModel.toggleExpansion(MediaPlayerUIState.Minimised) }
//                    )
//                    Button(onClick = {viewModel.toggleExpansion(MediaPlayerUIState.Expanded(true))}) { Text(text = "open queue") }
//                }
//            }
//        }
//
//        is MediaPlayerUIState.Minimised -> {
//            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
//                .fillMaxSize()
//                .padding(10.dp)) {
//                MinimisedMediaPlayerUI(
//                    image = songThumbnail,
//                    songName = songName,
//                    artistName = songArtists,
//                    playing = uiState.isPlaying,
//                    playPauseOnClick = viewModel::togglePlayPauseState,
//                    skipNextOnClick = viewModel::skipNext,
//                    fogColor = fogColor,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp)
////                        .clickable { viewModel.toggleExpansion(MediaPlayerUIState.Expanded(false)) }
//                )
//            }
//        }
//    }
}