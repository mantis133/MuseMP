package org.mantis.muse.layouts

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mantis.muse.R
import org.mantis.muse.layouts.components.BufferedImage
import org.mantis.muse.layouts.theme.MuseTheme
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.coverArt
import org.mantis.muse.util.toAlbumArt
import org.mantis.muse.viewmodels.PlaylistPickerViewModel
import org.mantis.muse.viewmodels.SinglePlaylistViewModel
import org.mantis.muse.viewmodels.SinglePlaylistViewState

@OptIn(UnstableApi::class)
@Composable
fun PlaylistInspector(
    playlistName: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SinglePlaylistViewModel = koinViewModel<SinglePlaylistViewModel>(parameters = { parametersOf(playlistName) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState){
        is SinglePlaylistViewState.Loaded -> {
            val state = (uiState as SinglePlaylistViewState.Loaded)
            PlaylistInspector(
                state.playlist,
                { navController.popBackStack() },
                viewModel::playPlaylist,
                viewModel::playPlaylistFromPosition,
                modifier
            )
        }
        is SinglePlaylistViewState.Loading -> {}
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PlaylistInspector(
    playlist: Playlist,
    navigateBack: () -> Unit,
    playPlaylist: () -> Unit,
    playFromSong: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) //TODO: Material3
    ) {
        val h1 = maxHeight
        val w1 = maxWidth
        val res = LocalContext.current.resources
        BufferedImage(
            imageProvider = {
                try {
                    playlist.coverArt!!.asImageBitmap()
                } catch (_: Exception) {
                    BitmapFactory.decodeResource(res, R.drawable.home_icon).asImageBitmap()
                }
            },
            contentDescription = null,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.6f)
        )
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(h1 - w1 / 2)
                    .background(
                        Color(0xFFD0BCFF),
                        shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp)
                    ) //TODO: Material3
            ){
                PlaylistDetails(playlist)

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {playPlaylist()},
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ) // TODO: Material3
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play_arrow),
                            contentDescription = null,
                        )
                    }
                }

                LazyColumn {
                    itemsIndexed(playlist.songList){ idx, song ->
                        Row(
                            modifier = Modifier
                                .clickable { playFromSong(idx) }
                                .fillMaxWidth()
                                .padding(10.dp)
                                .height(80.dp)
                        ){
                            BufferedImage(
                                imageProvider = {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            song.toAlbumArt()!!.asImageBitmap()
                                        } catch (_: Exception) {
                                            BitmapFactory.decodeResource(res, R.drawable.home_icon)
                                                .asImageBitmap()
                                        }
                                    }
                                },
                                contentDescription = null,
                            )
                            Column {
                                Text(text = song.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = song.artist.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.baseline_arrow_back_24),
            contentDescription = null,
            modifier = Modifier
                .clickable { navigateBack() }
                .padding(10.dp)
                .size(w1 / 10)
        )
    }
}

@Composable
fun PlaylistDetails(playlist: Playlist){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Author: ${"Mantis133"}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Tracks: ${playlist.size}",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun PlaylistInspectorPreview(){
    val songs = listOf(
        Song("Song 1", listOf("artist 1"), "NULL".toUri()),
        Song("Song 2", listOf("artist 2, artist 3"), "NULL".toUri())
    )
    MuseTheme {
        PlaylistInspector(
            Playlist(
                "cool name",
                songs,
                "".toUri(),
                null,
            ),
            {},{},{},
            Modifier.size(400.dp, 800.dp),
        )
    }
}