package org.mantis.muse.layouts

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.layouts.components.SongCard
import org.mantis.muse.util.Song
import org.mantis.muse.viewmodels.SongPickerViewModel
import org.mantis.muse.viewmodels.SongsScreenUiState

@OptIn(UnstableApi::class)
@Composable
fun SongPicker(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SongPickerViewModel = koinViewModel<SongPickerViewModel>(),
){
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    SongPicker(
        navController, uiState.value, viewModel::getSongArt, viewModel::playSong, modifier,
    )
}

@Composable
fun SongPicker(
    navController: NavController,
    uiState: SongsScreenUiState,
    getSongArt: (Song) -> ImageBitmap?,
    playSong: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        SongsScreenUiState.Loading -> {/*TODO*/}
        is SongsScreenUiState.Loaded -> {
            SongPicker(
                uiState.songs,
                getSongArt,
                playSong,
                modifier
            )
        }
    }
}

@Composable
fun SongPicker(
    songs: List<Song>,
    getSongArt: (Song) -> ImageBitmap?,
    playSong: (Song) -> Unit,
    modifier:Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(songs) { song ->
            SongCard(
                song.name,
                song.artist.joinToString(", "),
                getSongArt(song),
                Modifier
                    .clickable{ playSong(song) }
//                    .background(Color.Red)
            )
        }
    }
}

@Preview
@Composable
fun SongPickerPreview(){}
