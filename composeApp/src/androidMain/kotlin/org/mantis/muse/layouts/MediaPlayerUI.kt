package org.mantis.muse.layouts

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.OffsetEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.mantis.muse.MainActivity
import org.mantis.muse.R
import org.mantis.muse.util.MediaPLayerCallbacks
import org.mantis.muse.layouts.components.*
import org.mantis.muse.loop
import org.mantis.muse.util.LoopState
import org.mantis.muse.util.Song
import org.mantis.muse.viewmodels.MediaPlayerViewModel

val playIcon: ImageVector = Icons.Default.PlayArrow
val pauseIcon: ImageVector = Icons.Default.Add



@Preview(apiLevel = 30, showBackground = true, backgroundColor = 0xFF_FF_00_FF, widthDp = 400, heightDp = 600)
@Composable
private fun PlayerUIPreview() {
    PlayerUI(
        Song(name = "cool song", artist = "badass artist", lengthMs = 10000f, filePath = ""),
        playing = false,
        LoopState.None,
        shuffleState = true,
        {},{},{},{},{}
    )
}

@Composable
fun MediaPlayer(
    song: Song,
    playingState: Boolean,
    loopState: LoopState,
    shuffleState: Boolean,
    togglePlayPause: () -> Unit,
    skipLast: () -> Unit,
    skipNext: () -> Unit,
    nextLoopState: () -> Unit,
    toggleShuffle: () -> Unit,
    modifier: Modifier = Modifier
){
    PlayerUI(
        song = song,
        playing = playingState,
        loopState = loopState,
        shuffleState = shuffleState,
        togglePlayPause = togglePlayPause,
        skipLast = skipLast,
        skipNext = skipNext,
        nextLoopState = nextLoopState,
        toggleShuffle = toggleShuffle,
        modifier = modifier
    )
}

@Composable
fun MediaPlayer(
    modifier: Modifier = Modifier,
    viewModel: MediaPlayerViewModel = viewModel()
) {
    MediaPlayer(
        song = viewModel.currentPlaylist!!.songList[viewModel.currentSongIndex],
        playingState = viewModel.playing,
        loopState = viewModel.loopState,
        shuffleState = viewModel.shuffling,
        togglePlayPause = viewModel::togglePlayPauseState,
        skipLast = viewModel::skipLast,
        skipNext = viewModel::skipNext,
        nextLoopState = viewModel::nextLoopState,
        toggleShuffle = viewModel::toggleShuffle,
        modifier = modifier
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerUI(
    song: Song,
    playing: Boolean,
    loopState: LoopState,
    shuffleState: Boolean,
    togglePlayPause: () -> Unit,
    skipLast: () -> Unit,
    skipNext: () -> Unit,
    nextLoopState: () -> Unit,
    toggleShuffle: () -> Unit,
    modifier:Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        // backdrop
        Box(
            modifier = Modifier
                .aspectRatio(1f)
        ){
            Image(
                painter = painterResource(R.drawable.home_icon),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            Box (
                modifier = Modifier
                    .alpha(0.35f)
                    .background(Color.Black)
                    .fillMaxSize()
            ){}
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                        )
                    )
            ) {}
        }
        // foreground controls
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            // Song Information
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
            ) {
                Text(text = song.name, color = Color.White)
                Text(text = song.artist, color = Color.White)
            }
            // Seekbar
            var sliderPosition by remember{ mutableFloatStateOf(0f) }
            Slider(
                value = sliderPosition,
                onValueChange = { newPos ->
                    sliderPosition = newPos
                },
                valueRange = 1f..song.lengthMs,
                colors = SliderDefaults.colors(),
//                thumb = {
//                    val shape = CircleShape
//                    Spacer(
//                        modifier = Modifier
//                            .background(Color.White, shape = shape)
//                            .size(20.dp)
//                    )
//                },
                modifier = Modifier.padding(horizontal = 15.dp)
            )
            // current and total times
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)){
                Text(text = sliderPosition.toString(), color = Color.White)
                Text(text = song.lengthMs.toString(), color = Color.White)
            }
            // playback controls
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 40.dp)
            ) {
                IconButton(onClick = toggleShuffle) {
                    Icon(
                        painter = painterResource(R.drawable.shuffle_icon),
                        contentDescription = null,
                        tint = if (shuffleState) Color(0xffFCB3F7) else Color.White
                    )
                }
                IconButton(onClick = nextLoopState) {
                    Icon(
                        painter = when(loopState){
                            LoopState.None -> painterResource(R.drawable.fluent_arrow_repeat_all_20_regular32)
                            LoopState.Single -> painterResource(R.drawable.fluent_arrow_repeat_1_20_filled32)
                            LoopState.Full -> painterResource(R.drawable.fluent_arrow_repeat_all_20_filled32)
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
            ) {
                IconButton(onClick = skipLast) {
                    Icon(painter = painterResource(R.drawable.last_button), contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = togglePlayPause) {
                    Icon(
                        painter = if (playing) painterResource(R.drawable.play_button) else painterResource(R.drawable.pause_button),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                IconButton(onClick = skipNext) {
                    Icon(
                        painter = painterResource(R.drawable.next_button),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SongInformation(songName: String, artistName: String, modifier: Modifier = Modifier, horizontalAlignment:Alignment.Horizontal = Alignment.Start){
    val textColor = Color.White
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Text(text = songName, color = textColor)
        Text(text = artistName, color = textColor)
    }
}

//@Composable
//fun SeekbarControls(modifier: Modifier = Modifier){
//    var sliderPosition: Float by remember { mutableFloatStateOf(10f) }
//    val sliderMax = if (MainActivity.conn != null && MainActivity.conn!!.duration.toFloat() > 1.0f) {MainActivity.conn!!.duration.toFloat()} else {1f}
//    val textColor: Color = Color.White
//    LaunchedEffect(Unit) {
//        while (true) {
//            if (MainActivity.conn != null){
//                sliderPosition = MainActivity.conn!!.currentPosition.toFloat()
//            }
//            delay(1000L) // Update every second
//        }
//    }
//    Column(
//        modifier = modifier
//    ) {
//        Slider(
//            value = sliderPosition,
//            onValueChange = { newPos ->
//                run {
//                    sliderPosition = newPos
//                    if (MainActivity.conn != null) {
//                        MainActivity.conn!!.seekTo(sliderPosition.toLong())
//                    }
//                }
//            },
//            valueRange = 1f..sliderMax
//        )
//        Row(
//            modifier = Modifier.fillMaxWidth(1f),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ){
//            Text(text = "0", color = textColor)
//            Text(text = sliderMax.toString(), color = textColor)
//        }
//    }
//}

@Composable
fun PlayerControls(callbacks: MediaPLayerCallbacks,modifier:Modifier = Modifier) {
    val controlsTint: Color = Color.Red
    var isPlaying by remember { mutableStateOf(false) }
    var isShuffled by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row {
            ShuffleButton(
                onClick = { callbacks.shuffle() },
                tint = controlsTint,
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .fillMaxHeight(0.1f)

            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
            )

            LoopButton(
                onClick = { callbacks.loop() },
                tint = controlsTint,
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .fillMaxHeight(0.1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            IconButton(
                onClick = { callbacks.skipLast() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.last_button),
                    contentDescription = "",
                    tint = controlsTint,
                    modifier = Modifier.padding(0.dp)
                )
            }
            PlayPauseButton(
                isPlaying = isPlaying,
                onClick = { isPlaying = !isPlaying; callbacks.playPause(isPlaying) },
                tint = controlsTint
            )
            IconButton(onClick = { callbacks.skipNext() }) {
                Icon(
                    painter = painterResource(id = R.drawable.next_button),
                    contentDescription = "",
                    tint = controlsTint
                )
            }
        }

    }
}





