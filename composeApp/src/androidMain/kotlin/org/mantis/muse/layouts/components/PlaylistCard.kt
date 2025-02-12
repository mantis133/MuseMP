package org.mantis.muse.layouts.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.mantis.muse.R
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.coverArt

@Preview
@Composable
fun PlaylistCardPreview(
) {
    PlaylistCard(
        Playlist(
            "","cool name",
            listOf()
        ),
    )
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    modifier: Modifier = Modifier
) {
    val cover: Bitmap? = playlist.coverArt

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier

    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = if (cover != null) BitmapPainter(cover.asImageBitmap()) else painterResource(
                    R.drawable.home_icon
                ),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
                Text(
                    text = "Tracks: ${playlist.size}",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}