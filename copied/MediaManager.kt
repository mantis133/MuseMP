package com.example.muse


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import com.example.muse.util.Song
import com.example.muse.util.Playlist
import java.io.File
import java.lang.IndexOutOfBoundsException
import java.util.Vector



object MediaManager {
    val playlistLocation = "Playlists"
    var playlistPosition: Int = 0
    var isPaused = true
    var shuffled = false
    var looping = 0

    var playlist: Playlist? = null
    var shuffledPlaylist : Playlist? = null
    lateinit var songs : List<Song>

    lateinit var SongName : String
    lateinit var ArtistName : String
    var AlbumArtBitMap : Bitmap? = null

    fun initialize(controller: MediaController){
        isPaused = !controller.isPlaying
        shuffled = false
        looping = controller.repeatMode
//        0 : Player.REPEAT_MODE_OFF : does not loop the list
//        1 : Player.REPEAT_MODE_ONE : repeats the current item indefinitely, prev and next still go to different items as in 0
//        2 : Player.REPEAT_MODE_ALL : repeats the whole list indefinitely, prev and next use moduli as expected
        loadTrack(controller.currentMediaItemIndex)
    }

    fun loadTrack(song: Song){
        SongName = song.name
        ArtistName = song.artists
        AlbumArtBitMap = song.getAlbumCover()
    }

    fun loadTrack(playlist_position: Int) : Boolean{
        this.playlistPosition = playlist_position
        return try {
            loadTrack(songs.get(playlist_position))
            true
        } catch(e: IndexOutOfBoundsException){
            false
        } catch (e: UninitializedPropertyAccessException){
            false
        }
    }

    fun loadPlaylist(list:Playlist){
        playlist = list
        setSongs(list)
    }

    private fun setSongs(list:Playlist){
        songs = list.songs
    }

    public fun doSongsExist() : Boolean {
        return this::songs.isInitialized
    }

    fun buildSongFromFile(file: File) : Song{
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(file.path)
        val path = file.path
        val name = file.nameWithoutExtension
        val artists = when (val a = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)){
            null -> "Unknown"
            else -> a
        }
//        val albumCover = if (mmr.embeddedPicture != null) {BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)} else {null}

        return Song(path, name, artists)
    }

    fun buildPlaylistFromM3UFile(file : File, storageLocation: File) : Playlist{
        if (file.extension != "m3u") {
            throw Exception("not a playlist file")
        }

        val path = file.path
        val title = file.nameWithoutExtension
        var cover : Bitmap? = null
        val songs = Vector<Song>()
        var size = 0

        file.readLines().forEach { line ->
            line.replace("\n","")
            if (File(storageLocation.absolutePath, line).isFile){
                val song = buildSongFromFile(File(storageLocation.absolutePath + "/" + line))
                songs.add(song)
                size++
            } else if (line.startsWith("#EXTIMG")){
                val playlistArtPath = line.replace("#EXTIMG: ", "")
                cover = BitmapFactory.decodeFile("${ storageLocation }/Thumbnails/${playlistArtPath}")
            }
        }
        return Playlist(path, title, cover, songs.toList(), size)
    }

    fun quickBuildPlaylistFromM3UFile(file: File, storageLocation: File) : Playlist{
        if (file.extension != "m3u") {
            throw Exception("not a playlist file")
        }

        val path = file.path
        val title = file.nameWithoutExtension
        var cover : Bitmap? = null
        val songs = Vector<Song>()
        val size = 0

        run runLabel@ {
            file.readLines().forEach { line ->
                line.replace("\n","")
                if (line.startsWith("#EXTIMG")){
                    val playlistArtPath = line.replace("#EXTIMG: ", "")
                    cover = BitmapFactory.decodeFile("${ storageLocation }/Thumbnails/${playlistArtPath}")
                    return@runLabel
                }
            }
        }
        return Playlist(path, title, cover, songs.toList(), size)
    }

    fun loadPlaylistIntoMediaSession(playlist : List<Song>, controller: MediaController) : Boolean{
        controller.clearMediaItems()
        for (song in playlist){
            controller.addMediaItem(song.intoMediaItem())
        }
        return true
    }

    fun generateShuffledList(songList:List<Song>) : List<Song>{
        val shuffledList = songList.toMutableList()
        shuffledList.shuffle()
        return shuffledList
    }

    fun toggleShuffle(controller : MediaController){
        if (playlist == null){return} // find a throw later
        shuffled = !shuffled
        songs = if (shuffled){
            generateShuffledList(playlist!!.songs)
        } else {
            playlist?.songs!!
        }
        if (loadPlaylistIntoMediaSession(songs, controller)){
            controller.prepare()
        }

    }
}

private class MediaFunctions {
    fun getStorageDirectories(context: Context){
        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(context, null)
        val localStorage = externalStorageVolumes[0] // should be primary / local storage
        val sdCardStorage = externalStorageVolumes[1] // should be sd card
        StorageLocations.values().size
    }

    private enum class StorageLocations{
        Internal,
        SDCard
    }
}


