package org.mantis.muse.layouts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.mantis.muse.R
import kotlin.math.roundToInt

@Preview(widthDp = 400, heightDp = 100, backgroundColor = 0xFF_00_00_FFL)
@Composable
fun PlayerChipPreview() {
    val context = LocalContext.current
    val bml = BitmapFactory.decodeResource(context.resources, R.drawable.last_button)
    val bmh = BitmapFactory.decodeResource(context.resources, R.drawable.home_icon)
    val fogColor: Color = remember{sampleImage(bmh)}
    MinimisedMediaPlayerUI(
        bmh.asImageBitmap(),
        "SongName",
        "ArtistName",
        false,
        {}, {},
        fogColor,
        modifier = Modifier.width(400.dp).fillMaxHeight()
    )
}

/** TODO
 * - add the clickable
 *      - play button
 *      - next button
 *      - bring up full playback controls (navigation)
 * - fix the state icon of the play button
 * - implement in scene
 */
@Composable
fun MinimisedMediaPlayerUI(
    image: ImageBitmap,
    songName: String,
    artistName: String,
    playing: Boolean,
    playPauseOnClick: () -> Unit,
    skipNextOnClick: () -> Unit,
    fogColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(40))
    ) {
        Image(
            painter = BitmapPainter(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
        )
        Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.5f).background(Color.Black))


        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.5f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            fogColor
                        )
                    )
                )
        ){}

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(20.dp)
        ){
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth(0.4f)){
                Text(
                    text = songName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(Int.MAX_VALUE)
                )
                Text(
                    text = artistName,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(Int.MAX_VALUE)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(if (playing) R.drawable.pause_button else R.drawable.play_button),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
//                        .fillMaxHeight(0.8f)
//                        .width(100.dp)
                        .fillMaxHeight(0.9f)
                        .aspectRatio(1f)
                        .clickable { playPauseOnClick() }
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    painterResource(R.drawable.next_button),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxHeight(0.45f)
                        .aspectRatio(1f)
                        .clickable { skipNextOnClick() }
                )
            }
        }
    }
}

fun sampleImage(painter: Bitmap): Color{
    val imageWidth = painter.width
    val imageHeight = painter.height
    val zoneX = ((0f*imageWidth).roundToInt())..((1f*imageWidth).roundToInt())
    val zoneY = ((imageHeight * 0.4f).roundToInt())..((imageHeight * 0.6f).roundToInt())
    val colourMap = mutableMapOf<Long, Long>()
    for (y in zoneY) {
        for (x in zoneX) {
            val colourLong: Long = try {
                painter.getPixel(x,y).toLong()
            } catch (e: IllegalArgumentException){
                continue
            }
            if (colourLong in colourMap.keys) {
                colourMap[colourLong] = colourMap[colourLong]!! + 1
            } else {
                colourMap[colourLong] = 1
            }
        }
    }
    return Color(colourMap.maxBy{ it.value }.key)
}