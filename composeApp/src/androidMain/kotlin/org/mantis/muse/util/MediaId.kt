package org.mantis.muse.util

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.util.MediaId.Folder
import org.mantis.muse.util.MediaId.Root
import kotlin.getValue

sealed class MediaId(val rep: String){
    val repo: MediaRepository by inject<MediaRepository>(MediaRepository::class.java)

    abstract fun getInstance(): MediaItem
    abstract fun getChildren(page: Int, pageSize: Int): ImmutableList<MediaItem>

    data object Root: MediaId("ROOT"){
        override fun getInstance(): MediaItem {
            return MediaItem.Builder()
                .setMediaId(this.toId())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
        }

        override fun getChildren(page: Int, pageSize: Int): ImmutableList<MediaItem> {
            return listOf(
                Folder.Playlists.getInstance(),
                Folder.Songs.getInstance(),
                Folder.Artists.getInstance(),
            ) as ImmutableList<MediaItem>
        }
    }
    sealed class Folder: MediaId("FOLDER"){
        data object Playlists: Folder(){
            override val type: String = "PLAYLISTS"
        }
        data object Songs: Folder(){
            override val type: String = "SONGS"
        }
        data object Artists: Folder(){
            override val type: String = "ARTISTS"
        }

        abstract val type: String

        override fun getInstance(): MediaItem {
            return MediaItem.Builder()
                .setMediaId(this.toId())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(type.lowercase().replaceFirstChar { it.uppercase() })
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
        }

        override fun getChildren(page: Int, pageSize: Int): ImmutableList<MediaItem> {
            return when (this) {
                Playlists ->
                    runBlocking {
                        repo.playlistsStream.first()
                            .map { playlist -> Playlist(playlist.name).getInstance() }
                    } as ImmutableList<MediaItem>

                Songs ->
                    runBlocking {
                        repo.songsStream.first()
                            .map { song -> Song(song.name).getInstance() }
                    } as ImmutableList<MediaItem>

                Artists ->
                    runBlocking {
                        repo.artistStream.first()
                            .map { artist -> Artist(artist.name).getInstance() }
                    } as ImmutableList<MediaItem>
            }
        }

    }
    data class Playlist(val selector: String): MediaId("PLAYLIST"){
        override fun getInstance(): MediaItem {
            val playlist = runBlocking {
                repo.getPlaylistByName(this@Playlist.selector)
            }?:throw IllegalStateException("Selector does not map to an existing playlist")
            return MediaItem.Builder()
                .setUri(playlist.fileURI)
                .setMediaId(this.toId())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(playlist.name)
                        .setIsBrowsable(true)
                        .setIsPlayable(true)
                        .build()
                )
                .build()
        }

        override fun getChildren(page: Int, pageSize: Int): ImmutableList<MediaItem> {
            return runBlocking{
                repo.getSongsByPlaylist(this@Playlist.selector)
                    .map { song -> Song(song.name).getInstance() }
            } as ImmutableList<MediaItem>
        }
    }
    data class Song(val selector: String): MediaId("SONG"){
        override fun getInstance(): MediaItem {
            val song = runBlocking {
                repo.getSongByName(this@Song.selector)
            }?:throw IllegalStateException("Selector does not map to an existing song")
            return MediaItem.Builder()
                        .setUri(song.fileUri)
                        .setMediaId(this.toId())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.name)
                                .setArtist(song.artist.joinToString(", "))
//                                .setArtworkUri("haphazard".toUri())
                                .setIsBrowsable(false)
                                .setIsPlayable(true)
                                .build()
                        )
                        .build()

        }

        override fun getChildren(page: Int, pageSize: Int): ImmutableList<MediaItem> {
            return emptyList<MediaItem>() as ImmutableList<MediaItem>
        }
    }
    data class Artist(val selector: String): MediaId("ARTIST"){
        override fun getInstance(): MediaItem {
            val artist = runBlocking {
                repo.getArtistByName(selector)
            }?:throw IllegalStateException("Selector does not map to an existing artist")
            return MediaItem.Builder()
                .setMediaId(selector)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(artist.name)
                        .setIsBrowsable(true)
                        .setIsPlayable(true)
                        .build()
                )
                .build()
        }

        override fun getChildren(page: Int, pageSize: Int): ImmutableList<MediaItem> {
            return runBlocking{
                repo.getSongsByArtistName(this@Artist.selector)
                    .map{ song -> Song(song.name).getInstance() }
            } as ImmutableList<MediaItem>
        }
    }
}

fun MediaId.toId(): String {
    return when (this) {
        is Root -> this.rep
        is Folder -> this.rep + this.type
        is MediaId.Playlist -> this.rep + this.selector
        is MediaId.Song -> this.rep + this.selector
        is MediaId.Artist -> this.rep + this.selector
    }
}

fun String.toMuseMediaId(): MediaId{
    return when {
        this.startsWith(Root.rep) -> Root
        this.startsWith(Folder.Playlists.rep+Folder.Playlists.type) -> Folder.Playlists
        this.startsWith(Folder.Songs.rep+Folder.Songs.type) -> Folder.Songs
        this.startsWith(Folder.Artists.rep+Folder.Artists.type) -> Folder.Artists
        this.startsWith(MediaId.Playlist("").rep) -> MediaId.Playlist(this.removePrefix(MediaId.Playlist("").rep))
        this.startsWith(MediaId.Song("").rep) -> MediaId.Song(this.removePrefix(MediaId.Song("").rep))
        this.startsWith(MediaId.Artist("").rep) -> MediaId.Artist(this.removePrefix(MediaId.Artist("").rep))
        else -> throw IllegalArgumentException("")
    }
}