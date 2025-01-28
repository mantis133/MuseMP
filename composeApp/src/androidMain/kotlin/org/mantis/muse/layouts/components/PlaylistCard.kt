package org.mantis.muse.layouts.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mantis.muse.R
import org.mantis.muse.util.Playlist

@Preview
@Composable
fun PlaylistCardPreview(
) {
    PlaylistCard(
        Playlist(
            "","cool name",
            listOf()
        ),
        {},
        {},
    )
}

const val playlistTitleTextSize = 20
const val playlistTrackNumberTextSize = 10

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onPlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cover: Bitmap? = playlist.coverArt
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier){
        Image(
            painter = if (cover != null) BitmapPainter(cover.asImageBitmap()) else painterResource(R.drawable.home_icon),
            contentDescription = null,
            modifier.clickable { onPlay() }
        )
        Text(text = playlist.name, color = Color.White, fontSize = playlistTitleTextSize.sp)
        Text("Tracks: ${playlist.size}", color = Color.White, fontSize = playlistTrackNumberTextSize.sp, maxLines = 1)
    }
}