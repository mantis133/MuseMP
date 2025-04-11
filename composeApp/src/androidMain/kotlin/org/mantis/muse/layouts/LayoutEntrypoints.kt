package org.mantis.muse.layouts

import android.graphics.BitmapFactory
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
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.R
import org.mantis.muse.util.toAlbumArt
import org.mantis.muse.viewmodels.MediaPlayerUIState
import org.mantis.muse.viewmodels.MediaPlayerViewModel

@Composable
fun MediaPlayerUI(
    modifier: Modifier = Modifier,
    viewModel: MediaPlayerViewModel = koinViewModel<MediaPlayerViewModel>()
) {
    val res = LocalContext.current.resources
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val expansionState by viewModel.mediaPlayerExpanded.collectAsStateWithLifecycle()

    var fogColor: Color by remember{mutableStateOf(Color.Transparent)}
    LaunchedEffect(fogColor) {
        fogColor = sampleImage(BitmapFactory.decodeResource(res, R.drawable.home_icon))
    }
    var trackPosition by remember { mutableLongStateOf(uiState.trackPositionMS) }
    var duration: Long by remember { mutableLongStateOf(uiState.trackDurationMS) }

//    val player = koinInject<AndroidMediaPlayer>()
//    LaunchedEffect(true) {
//        println("Launched Effect Started")
//        while (true) {
//            trackPosition = player.trackPositionMS
//            println("alalal: $trackPosition")
//            delay(1000L)
//        }
//    }
    val currentSong = if (uiState.queue.size > uiState.queuePosition) uiState.queue[uiState.queuePosition] else null
    val songThumbnail = currentSong?.toAlbumArt()?.asImageBitmap()?:BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.home_icon).asImageBitmap()
    val songName = currentSong?.name?:"Nothing is playing"
    val songArtists = currentSong?.artist?.joinToString(", ")?:"Unknown"
    when (expansionState) {
        is MediaPlayerUIState.Expanded -> {
            when ((expansionState as MediaPlayerUIState.Expanded).songListVisible){
                true -> {
                    Column{
                        Button(onClick = {
                            viewModel.toggleExpansion(
                                MediaPlayerUIState.Expanded(
                                    false
                                )
                            )
                        }) { Text(text = "Close") }
                        SongQueue(
                            songQueue = uiState.queue,
                            currentQueueItemIndex = uiState.queuePosition,
                            selectSong = {idx -> viewModel.seekToSong(queueIndex = idx)},
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                false -> {
                    ExpandedMediaPlayerUI(
                        songName = songName,
                        artistsName = songArtists,
                        songThumbnail = songThumbnail,
                        trackPosition = trackPosition,
                        trackDurationMS = uiState.trackDurationMS,
                        playing = uiState.playing,
                        loopState = uiState.loopState,
                        shuffleState = uiState.shuffling,
                        togglePlayPause = viewModel::togglePlayPauseState,
                        skipLast = viewModel::skipLast,
                        skipNext = viewModel::skipNext,
                        nextLoopState = viewModel::nextLoopState,
                        toggleShuffle = viewModel::toggleShuffle,
                        onSeek = {pos:Long -> viewModel.seekTo(pos)},
                        modifier = Modifier
                            .clickable { viewModel.toggleExpansion(MediaPlayerUIState.Minimised) }
                    )
                    Button(onClick = {viewModel.toggleExpansion(MediaPlayerUIState.Expanded(true))}) { Text(text = "open queue") }
                }
            }
        }

        is MediaPlayerUIState.Minimised -> {
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)) {
                MinimisedMediaPlayerUI(
                    image = songThumbnail,
                    songName = songName,
                    artistName = songArtists,
                    playing = uiState.playing,
                    playPauseOnClick = viewModel::togglePlayPauseState,
                    skipNextOnClick = viewModel::skipNext,
                    fogColor = fogColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { viewModel.toggleExpansion(MediaPlayerUIState.Expanded(false)) }
                )
            }
        }
    }
}