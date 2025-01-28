package org.mantis.muse.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.util.Playlist

class PlaylistRepository(
    private val playlistDao: PlaylistDAO
) {
    val playlistStream: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { playlists ->
        playlists.map { playlist ->
            Playlist (
                "",
                playlist.name,
                listOf()
            )
        }
    }

    suspend fun invalidateAndRemakeCache() {
        // crawl the filesystem

        // delete all database data

        // insert found data
    }

    suspend fun addNewPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylists(PlaylistEntity(0, playlist.name, playlist.filePath))
    }

    suspend fun removePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(PlaylistEntity(0, playlist.name, playlist.filePath))
    }
}