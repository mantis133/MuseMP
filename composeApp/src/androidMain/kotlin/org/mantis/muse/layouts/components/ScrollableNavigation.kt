package org.mantis.muse.layouts.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.mantis.muse.Screen

@Preview
@Composable
fun ScrollableNavigationPreview(){
    val navController = rememberNavController();
    val exampleRoutes = listOf(
        Screen.PlaylistSelectionScreen,
        Screen.SongSelectionScreen
    )
    ScrollableNavigation(routes = exampleRoutes, navController)
}

@Composable
fun ScrollableNavigation(
    routes: List<Screen>,
    navHost: NavHostController,
    modifier: Modifier = Modifier
){

    val translationX = remember { Animatable(0f) }
    translationX.updateBounds(0f, 300f)
    val decay = rememberSplineBasedDecay<Float>()
    val scope = rememberCoroutineScope()

    val draggableState = rememberDraggableState(onDelta = {delta -> scope.launch { translationX.snapTo(translationX.value + delta) } })

    var offset by remember{mutableStateOf(Offset(0f,0f))}
    Row(
        horizontalArrangement = Arrangement.spacedBy(100.dp),
        modifier = modifier

            .graphicsLayer {
                this.translationX = translationX.value
            }
            .draggable(draggableState, Orientation.Horizontal, onDragStopped = {

            })
    ) {
        for (route in routes) {
            Text(text = route.display, fontSize = 30.sp, maxLines = 1, overflow = TextOverflow.Visible,
                modifier = Modifier
                    .graphicsLayer { scaleX = 1.1f; scaleY = 1.1f }
                    .clickable { navHost.navigate(route) }
            )
        }
    }
}