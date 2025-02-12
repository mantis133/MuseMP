package org.mantis.muse.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.mantis.muse.R
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import java.net.URI

@Preview
@Composable
fun SinglePlaylistPreview(){
    val playlist: Playlist = Playlist(
        filePath = "",
        name = "the list of the century",
        songList = listOf(
            Song("cool guy","cool guys", 0f, ""),
            Song("cool guy","cool guys2", 0f, ""),
            Song("bad man","evil things", 0f, ""),
        ),
        URI("")
    )
    PlaylistSingleUI(
        playlist = playlist,
        modifier = Modifier
            .width(600.dp)
            .height(900.dp)
    )
}

@Composable
fun PlaylistSingleView() {

}

@Composable
fun PlaylistSingleView(
    playlist: Playlist
) {

}

@Composable
fun PlaylistSingleUI(
    playlist: Playlist,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.home_icon),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()){
            Spacer(
                Modifier
                    .clip(RoundedCornerShape(20,20,0,0))
                    .background(Color(0xFF_D0_BC_FF))
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            )
        }
    }
}