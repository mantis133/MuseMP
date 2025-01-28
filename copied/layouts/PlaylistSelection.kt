package com.example.muse.layouts
//
//import android.graphics.BitmapFactory
//import android.util.Log
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.tooling.preview.PreviewParameter
//import androidx.compose.ui.tooling.preview.PreviewParameterProvider
//import androidx.compose.ui.unit.dp
//import com.example.muse.util.Playlist
//import com.example.muse.R
//import com.example.muse.util.Song
//import java.util.Vector
//
//private class SequencePlaylistProvider : PreviewParameterProvider<Playlist> {
//    override val values: Sequence<Playlist> = sequenceOf(
//        Playlist("","reply",null, Vector<Song>().also { it.add(Song("","","", null)) }.toList(), size = 1),
////        Playlist("goodnight","", Vector<String>().also { it.add("") }, size = 1)
//    )
//}
//
//private class VecPlaylistProvider : PreviewParameterProvider<Vector<Playlist>> {
//    override val values: Sequence<Vector<Playlist>> = sequenceOf(
//        Vector<Playlist>()
//            .also{ it.add(Playlist("","reply",null, Vector<Song>().also { it.add(Song("","","", null)) }.toList(), size = 1)) }
//            .also{ it.add(Playlist("","goodnight",null, Vector<Song>().also { it.add(Song("","","", null)) }.toList(), size = 1)) },
////
//    )
//}
//
//@Composable
//fun PlaylistCard(@PreviewParameter(SequencePlaylistProvider::class) playlist: Playlist){
//    Column(
//        modifier = Modifier
//            .padding(horizontal = 10.dp, vertical = 5.dp)
//            .clickable { Log.d("you can ","call code");Log.d("you can t","call code") },
//
//
//    ) {
//        Image(
//            painter = painterResource(R.drawable.home_icon),
//            contentDescription = "some",
//            modifier = Modifier
//               .fillMaxWidth(1f)
////               .clip(CircleShape)
//        )
//        Text(
//            text = playlist.title,
//            textAlign = TextAlign.Center,
//            modifier = Modifier
//                .fillMaxWidth(1f),
////              .wrapContentWidth(Alignment.Start,true),
//            color = Color(0xFFCCC2DC)
//        )
//    }
//}
//
//
//
//@Preview
//@Composable
//fun PlaylistSelectionList(@PreviewParameter(VecPlaylistProvider::class)playlists : List<Playlist>){
//    Column(
//
//    ) {
//        for (playlist in playlists) {
//            PlaylistCard(playlist = playlist)
//        }
//    }
//}