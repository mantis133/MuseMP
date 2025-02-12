package org.mantis.muse.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.util.Playlist
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
        Playlist.Companion.fromURI(playlistFile.toURI())
    }
    }

//    suspend fun updateCache(){
//        localFiles.localPlaylistFiles.collect {
//            this.addNewPlaylist(Playlist(it.path, it.name, listOf()))
//        }
//    }


    suspend fun addNewPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylists(PlaylistEntity(0, playlist.name, playlist.filePath))
    }

    suspend fun removePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(PlaylistEntity(0, playlist.name, playlist.filePath))
    }
}