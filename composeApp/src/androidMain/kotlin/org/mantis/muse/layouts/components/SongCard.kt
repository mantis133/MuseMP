package org.mantis.muse.layouts.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.mantis.muse.layout.components.BufferedImage

@Composable
fun SongCard(
    songTitle: String,
    songArtist: String,
    coverArt: suspend () -> ImageBitmap,
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
            BufferedImage(
                coverArt,
                contentDescription = "Album cover of song: $songTitle",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
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


//@Preview
//@Composable
//fun SongCardPreview(){
//    val exampleSong = Song(
//        "Orbit",
//        listOf("Good Kid"),
//        "NULL".toUri()
//    )
//    SongCard(
//        exampleSong.name,
//        exampleSong.artist.joinToString(", "),
//        null,
//    )
//}