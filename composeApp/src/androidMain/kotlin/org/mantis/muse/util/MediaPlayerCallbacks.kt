package org.mantis.muse.util

data class MediaPLayerCallbacks (
    val playPause: (isPlaying: Boolean) -> Unit,
    val skipNext: () -> Unit,
    val skipLast: () -> Unit,
    val loop: () -> Unit,
    val shuffle: () -> Unit
)