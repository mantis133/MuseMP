package org.mantis.muse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.mantis.muse.layouts.PlaylistSelectionScreenState
import org.mantis.muse.layouts.components.HorizontalNavView
import org.mantis.muse.layouts.components.ScrollableNavigation

@Serializable sealed class Screen(val display: String) {
    @Serializable data object Home: Screen("Home")
    @Serializable data object MusicPlayer : Screen("Player")
    @Serializable data object PlaylistSelectionScreen : Screen("Playlists")
    @Serializable data class SinglePlaylistViewScreen(val playlistURI: String): Screen("")
    @Serializable data object SongSelectionScreen : Screen("Songs")
    @Serializable data object ArtistsScreen : Screen("Artists")
}

@Composable
fun NavHostContainer(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val navigableScreens = listOf(
        Screen.Home,
        Screen.PlaylistSelectionScreen,
        Screen.SongSelectionScreen,
        Screen.ArtistsScreen
    )
    Column {
        val fontStyling = TextStyle.Default.copy(fontSize = 50.sp, color = Color.White)

        HorizontalNavView(
            navigableScreens,
            navHost = navController,
            fontStyling = fontStyling,
            modifier = Modifier
                .fillMaxHeight(0.1f)
                .background(Color.Black)
        )
        NavHost(navController = navController, startDestination = Screen.PlaylistSelectionScreen, modifier){
            composable<Screen.MusicPlayer>{
    //            PlayerUI(
    //                callbacks = mediaCallbacks,
    //                modifier = Modifier
    //                    .height(400.dp)
    //            )
            }
            composable<Screen.Home>{
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                }
            }
            composable<Screen.PlaylistSelectionScreen>{
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    PlaylistSelectionScreenState(
                        modifier = Modifier
                            .padding(10.dp)
                    )
                }
            }
            composable<Screen.SongSelectionScreen> {
                Column {

                }
            }
            composable<Screen.ArtistsScreen> {

            }
        }
    }
}