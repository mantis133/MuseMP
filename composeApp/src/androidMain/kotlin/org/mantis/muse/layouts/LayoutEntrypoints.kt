package org.mantis.muse.layouts

import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.R
import org.mantis.muse.util.toAlbumArt
import org.mantis.muse.viewmodels.MediaPlayerUIState
import org.mantis.muse.viewmodels.MediaPlayerViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

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
//            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
//                .fillMaxSize()
//                .padding(10.dp)) {
//                MinimisedMediaPlayerUI(
//                    image = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.home_icon).asImageBitmap(),
//                    songName = "Nothing to see here",
//                    artistName = "No one",
//                    playing = false,
//                    playPauseOnClick = viewModel::togglePlayPauseState,
//                    skipNextOnClick = viewModel::skipNext,
//                    fogColor = fogColor,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp)
//                )
//            }
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
                when (state.expanded) {
                    false ->
                        Box(
                            contentAlignment = Alignment.BottomCenter, modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        ) {
                            HorizontallyDismissible({viewModel.unloadSong()}){
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
                            onSeek = { pos: Long -> viewModel.seekTo(pos) },
                            modifier = Modifier
                                .clickable { viewModel.toggleExpansion() }
                        )
//                    Button(onClick = {viewModel.toggleExpansion(MediaPlayerUIState.Expanded(true))}) { Text(text = "open queue") }
                }
            }
        }
}


@Composable
fun HorizontallyDismissible(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    threshold: Float = 0.4f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dismissed by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }

    if (!dismissed) {
        Box(
            modifier = modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                val width = size.width.toFloat()
                                if (abs(offsetX.value) > width * threshold) {
                                    val targetOffset = if (offsetX.value > 0) width else -width
                                    offsetX.animateTo(
                                        targetOffset,
                                        animationSpec = tween(durationMillis = 300)
                                    )
                                    dismissed = true
                                    onDismiss()
                                } else {
                                    offsetX.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}