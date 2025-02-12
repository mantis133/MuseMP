package org.mantis.muse.layouts.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.mantis.muse.R
import org.mantis.muse.util.Song
import org.mantis.muse.util.toAlbumArt

@Composable
fun SongCard(
    song: Song,
    modifier: Modifier = Modifier
){
    val coverArt = song.toAlbumArt()
    Row(
        modifier = modifier
    ) {
        Image(
            painter = if (coverArt!= null) BitmapPainter(coverArt.asImageBitmap()) else painterResource(R.drawable.home_icon),
            contentDescription = null,
        )
        Column {
            Text(song.name)
            Text(song.artist)
        }
    }
}


@Preview
@Composable
fun SongCardPreview(){
    val exampleSong = Song(
        "Orbit",
        "Good Kid",
        0f,
        "NULL"
    )
    SongCard(
        song = exampleSong
    )
}