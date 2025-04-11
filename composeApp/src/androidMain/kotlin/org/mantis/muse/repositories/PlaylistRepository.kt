package org.mantis.muse.repositories

import androidx.core.net.toUri
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.cheapFromURI
import org.mantis.muse.util.fromURI

class PlaylistRepository(
    private val playlistDao: PlaylistDAO,
    private val localFiles: LocalFileSource
) {
//    val playlistStream: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { playlists ->
//        playlists.map { playlist ->
//            Playlist (
//                "",
//                playlist.name,
//                listOf()
//            )
//        }
//    }

    val playlistStream: Flow<List<Playlist>> = localFiles.localPlaylistFiles.map{ it.map { playlistFile ->
            Playlist.Companion.cheapFromURI(playlistFile.toUri())
        }
    }

//    suspend fun updateCache(){
//        localFiles.localPlaylistFiles.collect {
//            this.addNewPlaylist(Playlist(it.path, it.name, listOf()))
//        }
//    }

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylists()
    }

    suspend fun addNewPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylists(PlaylistEntity(0, playlist.name, playlist.fileURI))
    }

    suspend fun removePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(PlaylistEntity(0, playlist.name, playlist.fileURI))
    }
}