package org.mantis.muse

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.mantis.muse.layouts.PlayerUI
import org.mantis.muse.layouts.PlaylistSelectionScreen
import org.mantis.muse.util.MediaPLayerCallbacks
import org.mantis.muse.util.Playlist

sealed class Screen(val route: String) {
    data object MusicPlayer : Screen("MusicPlayerScreen")
    data object PlaylistSelectionScreen : Screen("PlaylistSelectionScreen")
}

@Composable
fun NavHostContainer(context: Context, mediaCallbacks: MediaPLayerCallbacks, loadPlaylist: (Playlist) -> Unit) {
    val navController = rememberNavController();
    NavHost(navController = navController, startDestination = "MusicPlayerScreen", Modifier.fillMaxSize()){
        composable("MusicPlayerScreen"){
            NavControlsContainer(navController, modifier = Modifier.fillMaxSize()){
//                PlayerUI(
//                    callbacks = mediaCallbacks,
//                    modifier = Modifier
////                        .height(400.dp)
//
//                )
            }
        }
        composable("PlaylistSelectionScreen"){
            var playlists by remember{ mutableStateOf(emptyList<Playlist>()) }
            NavControlsContainer(navController, modifier = Modifier.fillMaxSize()){

                LaunchedEffect(Unit) {
                    playlists = loadPlaylistScreen(context)
                }
                PlaylistSelectionScreen(playlists, loadPlaylist)
            }
        }
    }
}

@Composable fun NavControls(navController: NavController, iconTint: Color = Color.White, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {navController.navigate("MusicPlayerScreen")}) {
            Icon(
                painter = painterResource(id = R.drawable.music_note),
                contentDescription = "Navigate to the music player screen",
                tint = iconTint,
                modifier = Modifier.padding(0.dp).fillMaxSize(.5f)
            )
        }
        IconButton(onClick = {navController.navigate("PlaylistSelectionScreen")}) {
            Icon(
                painter = painterResource(id = R.drawable.download),
                contentDescription =  "Navigate to the playlist selection screen",
                tint = iconTint,
                modifier = Modifier.padding(0.dp).fillMaxSize()
            )
        }
    }
}

@Composable
fun NavControlsContainer(navController: NavController, overlayNav: Boolean = false ,modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (overlayNav) {
        Box(modifier = modifier) {
            content()
            NavControls(navController)
        }
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.Bottom) {
            Box(Modifier.weight(1f)){content()}
            NavControls(navController, modifier = Modifier.background(Color.Magenta))
        }
    }
}

//@Preview
//@Composable
//fun playerNavControlsPreview() {
//    NavHostContainer()
//}