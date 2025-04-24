package org.mantis.muse.repositories

import android.database.sqlite.SQLiteConstraintException
import androidx.core.net.toUri
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mantis.muse.storage.dao.ArtistDao
import org.mantis.muse.storage.dao.ArtistSongRelationshipDao
import org.mantis.muse.storage.dao.PlaylistDAO
import org.mantis.muse.storage.dao.PlaylistSongRelationshipDao
import org.mantis.muse.storage.dao.RecentlyPlayedDao
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.storage.entity.ArtistEntity
import org.mantis.muse.storage.entity.ArtistSongEntity
import org.mantis.muse.storage.entity.PlaylistEntity
import org.mantis.muse.storage.entity.PlaylistSongEntryEntity
import org.mantis.muse.storage.entity.RecentlyPlayedEntity
import org.mantis.muse.storage.entity.SongEntity
import org.mantis.muse.util.Artist
import org.mantis.muse.util.MediaId
import org.mantis.muse.util.Playlist
import org.mantis.muse.util.Song
import org.mantis.muse.util.toId

class MediaRepository(
    private val playlistDao: PlaylistDAO,
    private val songDao: SongDao,
    private val artistDao: ArtistDao,
    private val artistSongRelationshipDao: ArtistSongRelationshipDao,
    private val playlistSongRelationshipDao: PlaylistSongRelationshipDao,
    private val recentsDao: RecentlyPlayedDao
) {
    val playlistsStream: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { playlistEntities ->
        playlistEntities.map{ playlistEntity ->
            Playlist(
                playlistEntity.name,
                songDao.getSongsInPlaylist(playlistEntity.id).map { Song(it,  artistDao.getArtistsBySong(it.id).map { it.name }) },
                playlistEntity.fileUri,
                playlistEntity.thumbnailUri,
            )
        }
    }
    val songsStream: Flow<List<Song>> = songDao.getAll().map { songs ->
        songs.map { song -> Song(song, artistDao.getArtistsBySong(song.id).map { artist -> artist.name }) }
    }
    val artistStream: Flow<List<Artist>> = artistDao.getAllArtists().map { artists ->
        artists.map { artist -> Artist(artist.name) }
    }

    @Transaction
    suspend fun getSongById(songId: Long): Song?{
        val songEnt = songDao.getSongById(songId)?:return null
        val artists = artistDao.getArtistsBySong(songId).map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt, artists)
    }

    @Transaction
    suspend fun getSongByName(songName: String): Song? {
        val songEnt = songDao.getSongByName(songName)?:return null
        val artists = artistDao.getArtistsBySong(songEnt.id).map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt, artists)
    }

    @Transaction
    suspend fun getSongByFilename(songFilename: String): Song? {
        val songEnt = songDao.getSongByFilename(songFilename)?:return null
        val artists = artistDao.getArtistsBySong(songEnt.id).map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt, artists)
    }

    @Transaction
    suspend fun getPlaylistByName(playlistName: String): Playlist? {
        val playlistEntity = playlistDao.getPlaylistByName(playlistName)?:return null
        return Playlist(
            playlistEntity.name,
            songDao.getSongsInPlaylist(playlistEntity.id).map { Song(it, artistDao.getArtistsBySong(it.id).map { it.name }) },
            playlistEntity.fileUri,
            playlistEntity.thumbnailUri
        )
    }

    suspend fun getArtistByName(artistName: String): Artist? {
        val artistEnt = artistDao.getArtistByName(artistName)
        return Artist(artistEnt.name)
    }

    @Transaction
    suspend fun getSongsByArtistName(artistName: String): List<Song>{
        val artistEntity = artistDao.getArtistByName(artistName)
        return songDao.getSongsFromArtist(artistEntity.id)
            .map { Song(it.name, artistDao.getArtistsBySong(it.id).map { it.name }, it.uri) }
    }

    suspend fun getArtistsBySong(){}

    @Transaction
    suspend fun getSongsByPlaylist(playlistName: String): List<Song>{
        val playlistId = playlistDao.getPlaylistByName(playlistName)?.id
        return songDao.getSongsInPlaylist(playlistId!!).map { Song(it, artistDao.getArtistsBySong(it.id).map { it.name }) }
    }
    @Transaction
    suspend fun getSongsByPlaylist(playlist: Playlist): List<Song>{
        val playlistId = playlistDao.getPlaylistByName(playlist.name)?.id
        return songDao.getSongsInPlaylist(playlistId!!).map { Song(it, artistDao.getArtistsBySong(it.id).map { it.name }) }
    }

    suspend fun insertPlaylist(playlist: Playlist) {
        try{ playlistDao.insertPlaylists(PlaylistEntity(0, playlist.name, playlist.fileURI, playlist.thumbnailUri)) }
        catch (e: SQLiteConstraintException) {
            // TODO: More robust error handling.
            // runs when the unique constraints fail
        }
    }

    @Transaction
    suspend fun insertSong(song: Song){
        try {
            var songId = songDao.insertSongs(SongEntity(0, song.name, song.fileName, song.fileUri))
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

    suspend fun addSongToPlaylist(playlist: Playlist, song: Song, position: Long){
        val playlistEntity = playlistDao.getPlaylistByName(playlist.name) ?: throw IllegalArgumentException("Playlist does not exist")
        val songEntity = songDao.getSongByName(song.name) ?: throw IllegalArgumentException("Song does not exist")

        playlistSongRelationshipDao.insert(PlaylistSongEntryEntity(songEntity.id, playlistEntity.id, position))
    }

    suspend fun removeSongFromPlaylist(){}

    suspend fun addArtistToSong(){}

    suspend fun removeArtistFromSong(){}

    suspend fun setRecent(mediaId: MediaId, position: Long?) {
        val ent = RecentlyPlayedEntity(1, mediaId.toId(), position)
        recentsDao.insert(ent)
    }

    suspend fun getRecent(): RecentlyPlayedEntity {
        return recentsDao.getId(1)
    }
}