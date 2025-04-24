package org.mantis.muse.layouts.components

import android.media.browse.MediaBrowser
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import org.mantis.muse.R
import org.mantis.muse.util.Song
import org.mantis.muse.util.toAlbumArt

@Composable
fun SongCard(
    songTitle: String,
    songArtist: String,
    coverArt: ImageBitmap?,
    modifier: Modifier = Modifier
){
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = modifier
        ) {
            val imagePainter = try {
                BitmapPainter(coverArt!!)
            } catch (_: Exception) {
                painterResource(R.drawable.home_icon)
            }
            Image(
                painter = imagePainter,
                contentDescription = "Album cover of song: $songTitle",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
                Text(
                    text = songArtist,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }
    }
}


@Preview
@Composable
fun SongCardPreview(){
    val exampleSong = Song(
        "Orbit",
        listOf("Good Kid"),
        "NULL".toUri()
    )
    SongCard(
        exampleSong.name,
        exampleSong.artist.joinToString(", "),
        null,
    )
}