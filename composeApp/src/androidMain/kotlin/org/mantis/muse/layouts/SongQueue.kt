package org.mantis.muse.layouts

import android.util.SparseArray
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.mantis.muse.R
import org.mantis.muse.util.Song
import org.mantis.muse.util.toAlbumArt

@Composable
fun SongQueue(
    songQueue: List<Song>,
    currentQueueItemIndex: Int,
    selectSong: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val songIcons = remember { SparseArray<ImageBitmap?>() }
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(songQueue) { idx, song ->
            val selected = idx == currentQueueItemIndex
            val thumbnail = songIcons[idx]?:song.toAlbumArt()?.asImageBitmap()
            LaunchedEffect(song) {
                if (songIcons[idx] == null) {
                    songIcons[idx] = song.toAlbumArt()?.asImageBitmap()
                }
            }
            Row(
                modifier = Modifier
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                    .clickable(onClick = { selectSong(idx) })
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Image(
                    painter = if (thumbnail == null) painterResource(R.drawable.home_icon) else BitmapPainter(thumbnail),
                    contentDescription = null
                )
                Column {
                    Text(
                        text = song.name,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = song.artist,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}