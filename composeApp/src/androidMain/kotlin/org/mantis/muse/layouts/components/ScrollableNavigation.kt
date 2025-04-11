package org.mantis.muse.layouts.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.mantis.muse.Screen
import kotlin.math.abs
import kotlin.math.min

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
                color = Color.Red,
                modifier = Modifier
                    .graphicsLayer { scaleX = 1.1f; scaleY = 1.1f }
                    .clickable { navHost.navigate(route) }
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HorizontalNavView(
    routes: List<Screen>,
    navHost: NavHostController,
    fontStyling: TextStyle,
    modifier: Modifier = Modifier,
){
    val routeStrings = remember { routes.map{it.display} }
    BoxWithConstraints(
        modifier = modifier
    ) {
        val context: Context = LocalContext.current
        val canvasWidth = context.resources.displayMetrics.density * maxWidth.value.toInt()
        val spacerSize = 100

        val textMeasurer = rememberTextMeasurer()

        var scrollOffset by remember { mutableFloatStateOf(canvasWidth / 2f - textMeasurer.measure(routeStrings[0], fontStyling).size.width/2f) }
        var selectedIndex by remember { mutableIntStateOf(0) }

        val trackLength = remember { routeStrings.map{textMeasurer.measure(it, fontStyling).size.width}.fold(0f){acc, route -> acc + route} + (routeStrings.size-1)*spacerSize }
        val routeMidPoints = remember {
            routeStrings
                .map { routeString -> textMeasurer.measure(routeString, fontStyling).size.width }
                .runningReduceIndexed { idx, acc, stringWidth -> acc + stringWidth + if (idx!=0)spacerSize else 0 }
                .mapIndexed { idx, pos ->  pos - textMeasurer.measure(routeStrings[idx], fontStyling).size.width/2  }
        }

        Canvas(
            Modifier
                .fillMaxHeight()
                .width(canvasWidth.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scrollOffset = (canvasWidth/2f) - routeMidPoints[selectedIndex]
                            navHost.navigate(routes[selectedIndex])
                            println(routeStrings[selectedIndex])
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val leftOffset = textMeasurer.measure(routeStrings.first(), fontStyling).size.width / 2
                            val rightOffset = trackLength - textMeasurer.measure(routeStrings.last(), fontStyling).size.width / 2
                            val midpoint = canvasWidth/2f
                            if (scrollOffset > (midpoint - leftOffset))
                                scrollOffset = midpoint - leftOffset
                            else if (scrollOffset < midpoint - rightOffset)
                                scrollOffset = midpoint - rightOffset
                            else scrollOffset += dragAmount


                            selectedIndex = routes.foldIndexed(selectedIndex){ idx, acc, _ ->
                                val routeCardPosition = scrollOffset + routeMidPoints[idx]
                                val routeCardDifference = abs(routeCardPosition-canvasWidth/2)

                                val heldPosition = scrollOffset + routeMidPoints[acc]
                                val heldCardDifference = abs(heldPosition-canvasWidth/2)

                                if (min(routeCardDifference, heldCardDifference) == routeCardDifference) idx else acc
                            }

                        }
                    )
                }
        ) {
            var localPlacementOffset = 0
            routeStrings.forEachIndexed { idx, route ->
                val selected = routeStrings[selectedIndex] == route
                val measuredText = textMeasurer.measure(
                    text = route,
                    style = if (selected) fontStyling.copy(fontSize = 55.sp) else fontStyling,
                )
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(scrollOffset + localPlacementOffset,0f)
                )
                localPlacementOffset += measuredText.size.width + spacerSize
            }
        }
    }

}