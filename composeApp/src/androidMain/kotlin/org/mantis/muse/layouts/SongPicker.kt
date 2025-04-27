package org.mantis.muse.layouts

import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.R
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

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongPicker(
    songs: List<Song>,
    getSongArt: (Song) -> ImageBitmap?,
    playSong: (Song) -> Unit,
    modifier:Modifier = Modifier
) {
    var songSelectionMode by remember { mutableStateOf(false) }
    var selectedSongs by remember { mutableStateOf(setOf<Song>()) }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
    ){
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(songs) { song ->
                SongCard(
                    song.name,
                    song.artist.joinToString(", "),
                    getSongArt(song),
                    Modifier
                        .combinedClickable(
                            onClick = { if (songSelectionMode) selectedSongs = selectedSongs.toMutableSet().apply { add(song) } else playSong(song) },
                            onLongClick = {
                                songSelectionMode = true
                                selectedSongs = selectedSongs.toMutableSet().apply { add(song) }
                            }
                        )
                )
                if (songSelectionMode && song in selectedSongs) {
                    Box(
                        Modifier
                            .size(10.dp)
                    ){
                        Spacer(
                            Modifier
                                .size(10.dp)
                                .background(Color.Green, shape = CircleShape)
                        )
                    }
                }
            }
        }
        if (songSelectionMode) {
            SongSelectionModeTaskBar(
                selectedSongs = selectedSongs,
                editSongs = {},
                playSongs = {},
                onDismiss = {songSelectionMode = false; selectedSongs = setOf()},
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.7f)
                    .background(Color.Black)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
fun SongSelectionModeTaskBar(
    selectedSongs: Set<Song>,
    editSongs: () -> Unit,
    playSongs: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row (
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        SongSelectionModeTaskBarButton(if (selectedSongs.size > 1) "Batch Edit" else "Edit", editSongs, painterResource(R.drawable.edit_icon), null)
        SongSelectionModeTaskBarButton("Play Songs", playSongs, painterResource(R.drawable.play_arrow), null)
        SongSelectionModeTaskBarButton("Dismiss", onDismiss, painterResource(R.drawable.baseline_arrow_back_24), null)
    }
}

@Composable
fun SongSelectionModeTaskBarButton(
    text: String,
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .alpha(1f)
            .clickable{onClick()}
    ) {
        Icon(
            painter,
            contentDescription,
            tint = Color.White,
            modifier = Modifier
                .size(30.dp)
        )
        Text(
            text,
            color=Color.White
        )
    }
}

@Preview
@Composable
fun SongPickerPreview(){}
