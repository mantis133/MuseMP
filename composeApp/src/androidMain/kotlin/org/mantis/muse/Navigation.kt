package org.mantis.muse

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.mantis.muse.layouts.PlaylistSelectionScreenState
import org.mantis.muse.layouts.components.ScrollableNavigation

@Serializable sealed class Screen(val display: String) {
    @Serializable data object MusicPlayer : Screen("Player")
    @Serializable data object PlaylistSelectionScreen : Screen("Playlists")
    @Serializable data object SongSelectionScreen : Screen("Songs")
}

@Composable
fun NavHostContainer(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val navigatableScreens = listOf(
        Screen.PlaylistSelectionScreen,
        Screen.SongSelectionScreen
    )

    NavHost(navController = navController, startDestination = Screen.PlaylistSelectionScreen, modifier){
        composable<Screen.MusicPlayer>{
//            PlayerUI(
//                callbacks = mediaCallbacks,
//                modifier = Modifier
//                    .height(400.dp)
//            )
        }
        composable<Screen.PlaylistSelectionScreen>{
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ){
                ScrollableNavigation(
                    routes = navigatableScreens,
                    navHost = navController
                )
                PlaylistSelectionScreenState(
                    modifier = Modifier
                        .padding(10.dp)
                )
            }
        }
        composable<Screen.SongSelectionScreen> {
            Column {
                ScrollableNavigation(
                    routes = navigatableScreens,
                    navHost = navController
                )
            }
        }
    }
}