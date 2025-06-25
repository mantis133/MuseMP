package org.mantis.muse.layouts

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.Screen
import org.mantis.muse.viewmodels.PlaylistPickerViewModel
import org.mantis.muse.viewmodels.PlaylistsScreenUiState
import org.mantis.muse.layouts.components.PlaylistCard
import org.mantis.muse.util.*

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun PlaylistScreenPreview() {
    val playlist = Playlist(
        "cool list name",
        listOf(
            Song("songName", listOf("artistName"), "filePath".toUri()),
            Song("songName2",listOf("artistName2"),"filePath2".toUri())
        ),
        "".toUri(),
        null,
    )
    val playlist2 = Playlist(
        "cool list name2",
        listOf(
            Song("songName", listOf("artistName"), "filePath".toUri()),
            Song("songName3",listOf("artistName3"), "filePath3".toUri())
        ),
        "".toUri(),
        null,
    )
    val playlist3 = Playlist(
        "cool list name3",
        listOf(
            Song("songName", listOf("artistName"), "filePath".toUri()),
            Song("songName3",listOf("artistName3"),"filePath3".toUri())
        ),
        "".toUri(),
        null,
    )
    val playlists = listOf(playlist, playlist2,playlist3, playlist3,playlist2)
    MaterialTheme {
        PlaylistSelectionScreen(playlists, {})
    }
}


@OptIn(UnstableApi::class)
@Composable
fun PlaylistSelectionScreenState(
    navController: NavController,
    viewModel: PlaylistPickerViewModel = koinViewModel<PlaylistPickerViewModel>(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaylistSelectionScreenState(
        uiState = uiState,
        loadPlaylist = { playlist -> navController.navigate(Screen.SinglePlaylistViewScreen(playlist.name)) },
        modifier = modifier
    )
}

@Composable
fun PlaylistSelectionScreenState(
    uiState: PlaylistsScreenUiState,
    loadPlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        PlaylistsScreenUiState.Loading -> {}
        is PlaylistsScreenUiState.Loaded -> {
            PlaylistSelectionScreen(
                playlists = uiState.playlists,
                loadPlaylist = loadPlaylist,
                modifier = modifier
            )
        }
    }
}

@Composable
fun PlaylistSelectionScreen(playlists: List<Playlist>, loadPlaylist: (Playlist) -> Unit, modifier: Modifier = Modifier) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(playlists) { item ->
            PlaylistCard(
                playlist = item,
                modifier = Modifier
                    .clickable { loadPlaylist(item) }
                    .height(100.dp)
            )
        }
    }

}
