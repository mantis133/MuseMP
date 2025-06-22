package org.mantis.muse.layouts

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import org.mantis.muse.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.mantis.muse.Screen
import org.mantis.muse.layouts.components.BufferedImage
import org.mantis.muse.layouts.components.PlaylistCard
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.coverArt
import org.mantis.muse.viewmodels.PlaylistCreationViewModel

@Composable
fun AddToPlaylistUI(
    songNames: List<String>,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PlaylistCreationViewModel = koinViewModel<PlaylistCreationViewModel>()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSongs(songNames)
    }

    AddToPlaylistUI(
        playlists = uiState.value.playlists,
        createPlaylist = {playlistName, playlistThumbnailUri ->
            viewModel.createPlaylist(playlistName, playlistThumbnailUri)
            navController.popBackStack()
            navController.navigate(Screen.SinglePlaylistViewScreen(playlistName))
        },
        addToPlaylist = {playlistName ->
            viewModel.addSongsToPlaylist(playlistName)
            navController.popBackStack()
            navController.navigate(Screen.SinglePlaylistViewScreen(playlistName = playlistName))
        },
        modifier = modifier
    )
}

@Composable
fun AddToPlaylistUI(
    playlists: List<Playlist>,
    createPlaylist: (playlistName: String, playlistThumbnailUri: Uri?) -> Unit,
    addToPlaylist: (playlistName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var createPlaylistPopupVisible: Boolean by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
    ) {
        item {
            Card(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .clickable { createPlaylistPopupVisible = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_add_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                    Spacer(
                        Modifier.padding(10.dp)
                    )
                    Text(text = "Create New Playlist")
                }
            }
        }
        items(playlists) { playlist ->
            AddToPlaylistOptionCard(
                playlist,
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .clickable { addToPlaylist(playlist.name) }
            )
        }
    }

    AnimatedVisibility(createPlaylistPopupVisible) {
        CreatePlaylistForm(
            onCreate = createPlaylist,
            onCancel = {createPlaylistPopupVisible = false },
            modifier = Modifier
                .fillMaxSize()
                .clickable{}
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
fun AddToPlaylistOptionCard(
    playlist: Playlist,
    modifier: Modifier = Modifier
){
    Card(
        modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter =
                    try {
                        BitmapPainter(playlist.coverArt!!.asImageBitmap())
                    } catch (_: Exception) {
                        painterResource(R.drawable.pause_button)
                    },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
            )
            Spacer(
                Modifier.padding(10.dp)
            )
            Text(
                text = playlist.name,
                color = Color.White,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun CreatePlaylistForm(
    onCreate: (playlistName: String, playlistThumbnailUri: Uri?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playlistThumbnail: Uri? by remember { mutableStateOf(null) }
    var playlistName: String by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            playlistThumbnail = uri
        }
    )

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
                    .clickable { filePickerLauncher.launch(arrayOf("image/*")) }
            ) {
                if (playlistThumbnail == null) {
                    Icon(
                        painter = painterResource(R.drawable.outline_add_24),
                        contentDescription = null,
                    )
                }
                playlistThumbnail?.let {
                    val context = LocalContext.current
                    val imageBitmap by remember(playlistThumbnail) {
                        mutableStateOf(playlistThumbnail?.let {
                            context.contentResolver.openInputStream(it)?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                            }
                        })
                    }

                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                    )
                }
            }
        }

        TextField(
            placeholder = {Text("Playlist Name")},
            value = playlistName,
            onValueChange = { playlistName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 20.dp)
        )

        Row {
            Button(
                onClick = { onCreate(playlistName, null) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 0.dp, horizontal = 10.dp)
            ) { Text("Create Playlist") }
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 0.dp, horizontal = 10.dp)
            ) { Text("Cancel") }
        }
    }
}