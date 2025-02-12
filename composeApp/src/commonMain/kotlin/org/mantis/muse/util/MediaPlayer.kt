package org.mantis.muse.util

enum class MediaPlayerEvent{
    PlaylistChange,
    PlaylistFinished,
    SongChange,
    SongLoaded,
}

/**
 * Interface to be implemented by platform specific implementation of a media player.
 *
 * For example a wrapper on Exoplayer may be used for Android while a wrapper on a html <audio> element may be used on web based platforms
 */
interface MediaPlayer {
    var currentSong: Song
    var queue: MutableList<Song>

    var playingState: Boolean
    var loopState: LoopState
    var shuffleState: Boolean

    fun play()
    fun pause()
    fun skipNext()
    fun skipLast()
    fun setShuffle(shuffling: Boolean)
    fun setLooping(state: LoopState)
    fun seekPosition(position: Int): Result<Unit>

    fun clearQueue()
    fun loadSong(song: Song)

    fun addOnChangeListener(callback: () -> Unit)
}