package org.mantis.muse.layouts

import android.annotation.SuppressLint
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
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


@Composable
fun PlaylistSelectionScreenState(
    viewModel: PlaylistPickerViewModel = koinViewModel<PlaylistPickerViewModel>(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaylistSelectionScreenState(uiState = uiState, loadPlaylist = viewModel::loadPlaylist, modifier = modifier)
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
            )
        }
    }

}
