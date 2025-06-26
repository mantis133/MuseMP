package org.mantis.muse.util

import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

@Serializable
data class PlayerState (
    val songTitle: String = "Nothing is Playing",
    val songArtists: String = "No Artist",

    val playing: Boolean = false,
    val shuffling: Boolean = false,
    val loopState: LoopState = LoopState.None,
    val trackPosition: Long = 0L,
    val trackDuration: Long = 1.hours.toLong(DurationUnit.MILLISECONDS),
)