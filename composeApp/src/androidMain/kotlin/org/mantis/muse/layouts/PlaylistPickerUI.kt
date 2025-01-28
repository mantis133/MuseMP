package org.mantis.muse.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.mantis.muse.viewmodels.PlaylistPickerViewModel
import org.mantis.muse.viewmodels.PlaylistsScreenUiState
import org.mantis.muse.layouts.components.PlaylistCard
import org.mantis.muse.util.*

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun PlaylistScreenPreview() {
    val playlist = Playlist(
        "path",
        "cool list name",
        listOf(
            Song("songName", "artistName", 0f, "filePath"),
            Song("songName2","artistName2", 0f,"filePath2")
        )
    )
    val playlist2 = Playlist(
        "path",
        "cool list name2",
        listOf(
            Song("songName", "artistName", 0f, "filePath"),
            Song("songName3","artistName3", 0f,"filePath3")
        )
    )
    val playlist3 = Playlist(
        "path",
        "cool list name3",
        listOf(
            Song("songName", "artistName", 0f, "filePath"),
            Song("songName3","artistName3", 0f,"filePath3")
        )
    )
    val playlists = listOf(playlist, playlist2,playlist3, playlist3,playlist2)
    MaterialTheme {
        PlaylistSelectionScreen(playlists, {})
    }
}

const val NUMBER_PLAYLISTS_PER_ROW: Int = 1

@Composable
fun PlaylistSelectionScreenState(
    viewModel: PlaylistPickerViewModel = viewModel<PlaylistPickerViewModel>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaylistSelectionScreenState(uiState = uiState, loadPlaylist = viewModel::loadPlaylist)
}

@Composable
fun PlaylistSelectionScreenState(
    uiState: PlaylistsScreenUiState,
    loadPlaylist: (Playlist) -> Unit
) {
    when (uiState) {
        PlaylistsScreenUiState.Loading -> {}
        is PlaylistsScreenUiState.Loaded -> {
            PlaylistSelectionScreen(playlists = listOf(Playlist("","inserted",listOf(Song("inserted","faker",0f,"/storage/9C33-6BBD/Android/data/org.mantis.muse/files/UnderTheShadesOfGreen/[08] Cold Turkey.mp3")))) + uiState.playlists, loadPlaylist = loadPlaylist)
        }
    }
}

@Composable
fun PlaylistSelectionScreen(playlists: List<Playlist>, loadPlaylist: (Playlist) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()){
        for (i in playlists.indices step NUMBER_PLAYLISTS_PER_ROW){
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(Color.Black)

            ) {
                for (j in 0..<NUMBER_PLAYLISTS_PER_ROW) {
                    if ((i + j) <= playlists.indices.max()) {
//                        Button(
//                            onClick = {
//                                loadPlaylist(playlists[i])
//                            },
//                            colors = ButtonColors(containerColor = playlistButtonBackgroundColour, contentColor = playlistButtonContentColour, disabledContainerColor = Color.Transparent, disabledContentColor = Color.Transparent)
//                            ) {
                            PlaylistCard(playlist = playlists[i], onPlay = { loadPlaylist(playlists[i]) }, onClick = { println("interaction")}, modifier = Modifier.fillMaxWidth(0.45f))
//                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun PlaylistCard(playlist: Playlist, modifier: Modifier = Modifier) {
//    Box(
//        contentAlignment = Alignment.TopStart,
//        modifier = modifier
//    ){
//        Text(text = playlist.name)
//    }
//}