package org.mantis.muse.util

class Song(val name: String, val artist: String, val lengthMs: Float, val filePath: String) {
    constructor(filePath: String): this(
        name = "",
        artist = "",
        0f,
        filePath = filePath,
    )

    override fun toString(): String {
        return "$name, $artist, $filePath"
    }

}