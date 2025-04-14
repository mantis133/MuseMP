package org.mantis.muse.layouts

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import org.mantis.muse.R
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.util.LoopState


val shuffleIconSize = 45.dp
val LoopIconSize = 45.dp
val SkipNextIconSize = 45.dp
val SkipLastIconSize = 45.dp
val pausePlayIconSize = 60.dp

@Preview(showBackground = true, backgroundColor = 0xFF_FF_00_FF, widthDp = 400, heightDp = 600)
@Composable
private fun PlayerUIPreview() {
    val bmh = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.home_icon)
    ExpandedMediaPlayerUI(
        "cool song", "badass artist", { bmh.asImageBitmap() },0L, trackDurationMS = 10000L,
        playing = false,
        LoopState.None,
        shuffleState = true,
        {},{},{},{},{},{}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedMediaPlayerUI(
    songName: String,
    artistsName: String,
    getSongThumbnail: () -> ImageBitmap,
    trackPosition: Long,
    trackDurationMS: Long,
    playing: Boolean,
    loopState: LoopState,
    shuffleState: Boolean,
    togglePlayPause: () -> Unit,
    skipLast: () -> Unit,
    skipNext: () -> Unit,
    nextLoopState: () -> Unit,
    toggleShuffle: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier:Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        // backdrop
        Box(
            modifier = Modifier
                .aspectRatio(1f)
        ){
            Image(
                bitmap = getSongThumbnail(),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            Spacer (
                modifier = Modifier
                    .alpha(0.35f)
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            )
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .alpha(0.5f)
            )
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
                Text(
                    text = songName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = artistsName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.height(30.dp))
            // Seekbar
            var currentTime = trackPosition / 1000f
//            var currentTime = trackPosition / 1000f
//            val player = koinInject<AndroidMediaPlayer>()
//            LaunchedEffect(true) {
//                println("Launched Effect Started")
//                while (true) {
//                    currentTime = player.trackPositionMS / 1000f
//                    delay(1000L)
//                }
//            }
            val totalTime = trackDurationMS / 1000f
            Slider(
                value = currentTime,
                onValueChange = { newPos ->
                    currentTime = newPos
                    onSeek(newPos.toLong() * 1000)
                },
                valueRange = 0f..totalTime,
                thumb = {
                    val thumbColor = MaterialTheme.colorScheme.secondary
                    Canvas(modifier = Modifier.size(24.dp)) {
                        drawCircle(color = thumbColor) // Draw a circle instead of a rounded rectangle
                    }
                },
                track = { sliderState ->
                    Track(
                        sliderState = sliderState,
                        thumbTrackGapSize = 0.dp,
                    )
                },
                modifier = Modifier.padding(horizontal = 15.dp)
            )
            // current and total times
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)){
                Text(
                    text = toMinSecRep(currentTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = toMinSecRep(totalTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.height(30.dp))
            // playback controls
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle_icon),
                    contentDescription = null,
                    tint = if (shuffleState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(shuffleIconSize)
                        .clickable(onClick = toggleShuffle)
                )
                Icon(
                    painter = when(loopState){
                        LoopState.None -> painterResource(R.drawable.fluent_arrow_repeat_all_20_regular32)
                        LoopState.Single -> painterResource(R.drawable.fluent_arrow_repeat_1_20_filled32)
                        LoopState.Full -> painterResource(R.drawable.fluent_arrow_repeat_all_20_filled32)
                    },
                    contentDescription = null,
                    tint = when(loopState){
                        LoopState.None -> MaterialTheme.colorScheme.surfaceVariant
                        LoopState.Single -> MaterialTheme.colorScheme.primary
                        LoopState.Full -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier
                        .size(LoopIconSize)
                        .clickable(onClick = nextLoopState)
                )
            }
            Spacer(Modifier.height(30.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.last_button),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(SkipLastIconSize)
                        .clickable(onClick = skipLast)
                )
                Icon(
                    painter = if (playing) painterResource(R.drawable.pause_button) else painterResource(R.drawable.play_button),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(pausePlayIconSize)
                        .clickable(onClick = togglePlayPause)
                )
                Icon(
                    painter = painterResource(R.drawable.next_button),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(SkipNextIconSize)
                        .clickable(onClick = skipNext)
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

fun toMinSecRep(seconds: Float): String {
val mins = seconds.toInt() / 60
    val secs = seconds.toInt() % 60
    val totalStringMins = mins.toString().format("%02d")
    val totalStringSecs = secs.toString().format("%02d")
    return "$totalStringMins:$totalStringSecs"
}