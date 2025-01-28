package com.example.muse.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.muse.R
import com.example.muse.util.Song
import com.example.muse.MediaManager
import java.io.File

private class SongProvider : PreviewParameterProvider<Song>{
    override val values: Sequence<Song>
        get() = sequenceOf(
//            Song("Orbit", "Good Kid", BitmapFactory.decodeFile(R.drawable.home_icon.toString())),
            MediaManager.buildSongFromFile(File("E:\\trash\\Android\\Muse\\app\\src\\main\\res\\raw\\mimi.mp3"))
        )
}

@Preview
@Composable
fun SongCard(@PreviewParameter(SongProvider::class) song: Song){
    Row {
        if (song.getAlbumCover() != null) {
            Image(
                bitmap = song.getAlbumCover()!!.asImageBitmap(),
                contentDescription = null
            )
        } else {
            Image(
                painter = painterResource(R.drawable.home_icon),
                contentDescription =null,
                modifier = Modifier
                    .size(50.dp)
            )
        }
        Column(
            Modifier.padding(horizontal = Dp(5f))
        ){
            Text(text = song.name)
            Spacer(modifier = Modifier.weight(1f, false))
            Text(text = song.artists)
        }
    }
}