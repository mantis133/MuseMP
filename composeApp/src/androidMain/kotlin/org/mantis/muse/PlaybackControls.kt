package org.mantis.muse

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.util.Song

fun playPause(playing: Boolean, con : MediaController) {
    Log.d("CONTROLS", "play/pause: $playing")
    if (playing) {
        con.pause()
    } else {
        con.play()
    }
}

fun skipNext(conn : MediaController) {
    Log.d("CONTROLS", "Next")
    conn.seekToNextMediaItem()
}

fun skipLast(conn : MediaController) {
    Log.d("CONTROLS", "Last")
    conn.seekToPreviousMediaItem()
}

fun shuffle(con : MediaController) {
    Log.d("CONTROLS", "Shuffle")

}

fun loop(con : MediaController) {
    Log.d("CONTROLS", "Loop")

}

fun loadSingleSong(song: Song, context: Context) {
    val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
    val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
    controllerFuture.addListener({
        val con = controllerFuture.get()
        con.clearMediaItems()
        con.addMediaItem(MediaItem.fromUri(song.filePath))
        con.prepare()
        con.play()
     }, MoreExecutors.directExecutor())
}