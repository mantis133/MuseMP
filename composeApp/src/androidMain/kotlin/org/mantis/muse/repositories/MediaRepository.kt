package org.mantis.muse.repositories

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.mantis.muse.storage.dao.ArtistDao
import org.mantis.muse.storage.dao.ArtistSongRelationshipDao
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.PlaylistSongRelationshipDao
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.storage.entity.ArtistEntity
import org.mantis.muse.storage.entity.ArtistSongEntity
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity
import org.mantis.muse.storage.entity.SongEntity
import org.mantis.muse.util.Artist
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song

class MediaRepository(
    val playlistDao: PlaylistDAO,
    val songDao: SongDao,
    val artistDao: ArtistDao,
    val artistSongRelationshipDao: ArtistSongRelationshipDao,
    val playlistSongRelationshipDao: PlaylistSongRelationshipDao
) {
    val playlistsStream: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { playlistEntities ->
        playlistEntities.map{ playlistEntity ->
            Playlist(
                playlistEntity.name,
                songDao.getSongsInPlaylist(playlistEntity.id).map { Song(it.name, artistDao.getArtistsBySong(it.id).map { it.name }, it.uri) },
                playlistEntity.fileUri
            )
        }
    }
    val songsStream: Flow<List<Song>> = songDao.getAll().map { songs ->
        songs.map { song ->
            Song(
                song.name,
                artistDao.getArtistsBySong(song.id).map { artist -> artist.name },
                song.uri
            )
        }
    }

    @Transaction
    suspend fun getSongById(songId: Long): Song?{
        val songEnt = songDao.getSongById(songId)?:return null
        val artists = artistDao.getArtistsBySong(songId).map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt.name, artists, songEnt.uri)
    }

    @Transaction
    suspend fun getSongByName(songName: String): Song? {
        val songEnt = songDao.getSongByName(songName)?:return null
        val artists = artistDao.getArtistsBySong(songEnt.id).map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt.name, artists, songEnt.uri)
    }

    @Transaction
    suspend fun getPlaylistByName(playlistName: String): Playlist? {
        val playlistEntity = playlistDao.getPlaylistByName(playlistName)?:return null
        return Playlist(
            playlistEntity.name,
            songDao.getSongsInPlaylist(playlistEntity.id).map { Song(it.name, artistDao.getArtistsBySong(it.id).map { it.name }, it.uri) },
            playlistEntity.fileUri
        )
    }

    suspend fun getSongsByArtist(){}

    suspend fun getArtistsBySong(){}

    @Transaction
    suspend fun getSongsByPlaylist(playlistName: String): List<Song>{
        val playlistId = playlistDao.getPlaylistByName(playlistName)?.id
        return songDao.getSongsInPlaylist(playlistId!!).map { Song(it.name, artistDao.getArtistsBySong(it.id).map { it.name }, it.uri) }
    }
    @Transaction
    suspend fun getSongsByPlaylist(playlist: Playlist): List<Song>{
        val playlistId = playlistDao.getPlaylistByName(playlist.name)?.id
        return songDao.getSongsInPlaylist(playlistId!!).map { Song(it.name, artistDao.getArtistsBySong(it.id).map { it.name }, it.uri) }
    }

    suspend fun insertPlaylist(playlist: Playlist) {
        try{ playlistDao.insertPlaylists(PlaylistEntity(0, playlist.name, playlist.fileURI)) }
        catch (e: SQLiteConstraintException) {
            // TODO: More robust error handling.
            // runs when the unique constraints fail
        }
    }

    @Transaction
    suspend fun insertSong(song: Song){
        try {
            var songId = songDao.insertSongs(SongEntity(0, song.name, song.fileUri.path.toString(), song.fileUri))
            if (songId==-1L){ songId = songDao.getSongByName(song.name)!!.id }
            song.artist[0].split(", ").onEach { artistName ->
                var artistId = artistDao.insertArtists(ArtistEntity(0, artistName, null, null))
                if (artistId == -1L) { artistId = artistDao.getArtistByName(artistName).id }
                artistSongRelationshipDao.insert(ArtistSongEntity(artistId, songId))
            }
        }
        catch (e: SQLiteConstraintException) { }
    }

    suspend fun insertArtist(artist: Artist) {
        artistDao.insertArtists(ArtistEntity(0, artist.name, null, null))
    }

    suspend fun addSongToPlaylist(playlist: Playlist, song: Song){
        val playlistEntity = playlistDao.getPlaylistByName(playlist.name) ?: throw IllegalArgumentException("Playlist does not exist")
        val songEntity = songDao.getSongByName(song.name) ?: throw IllegalArgumentException("Song does not exist")

        playlistSongRelationshipDao.insert(PlaylistSongEntryEntity(songEntity.id, playlistEntity.id))
    }

    suspend fun removeSongFromPlaylist(){}

    suspend fun addArtistToSong(){}

    suspend fun removeArtistFromSong(){}
}