package org.mantis.muse.layouts.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

sealed class ImageLoadState {
    object Loading : ImageLoadState()
    data class Success(val data: ImageBitmap) : ImageLoadState() {}

    data class Error(val exception: Throwable) : ImageLoadState()
}

@Composable
fun BufferedImage(
    imageProvider: suspend () -> ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val imageState = produceState<ImageLoadState>(initialValue = ImageLoadState.Loading) {
        value = try {
            ImageLoadState.Success(imageProvider())
        } catch (e: Exception) {
            ImageLoadState.Error(e)
        }
    }

    when (val state = imageState.value) {
        is ImageLoadState.Loading -> {
            Spacer(
                modifier
            )
        }

        is ImageLoadState.Success -> {
            val bitmap: ImageBitmap = state.data
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier=modifier
            )
        }

        is ImageLoadState.Error -> {
            Text("Failed to load image: ${state.exception.localizedMessage}", color = Color.White)
        }
    }
}