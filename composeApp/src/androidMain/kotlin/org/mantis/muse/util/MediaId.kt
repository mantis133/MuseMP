package org.mantis.muse.util

import org.mantis.muse.util.MediaId.Folder
import org.mantis.muse.util.MediaId.Root

sealed class MediaId(val rep: String){
    data object Root: MediaId("ROOT")
    data class Folder(val type: String): MediaId("FOLDER")
    data class Playlist(val selector: String): MediaId("PLAYLIST")
    data class Song(val selector: String): MediaId("SONG")
}

fun MediaId.toId(): String {
    return when (this) {
        is Root -> this.rep
        is Folder -> this.rep + this.type
        is MediaId.Playlist -> this.rep + this.selector
        is MediaId.Song -> this.rep + this.selector
    }
}

fun String.toMuseMediaId(): MediaId{
    return when {
        this.startsWith(Root.rep) -> Root
        this.startsWith(Folder("").rep) -> Folder(this.removePrefix(Folder("").rep))
        this.startsWith(MediaId.Playlist("").rep) -> MediaId.Playlist(this.removePrefix(MediaId.Playlist("").rep))
        this.startsWith(MediaId.Song("").rep) -> MediaId.Song(this.removePrefix(MediaId.Song("").rep))
        else -> throw IllegalArgumentException("")
    }
}