package org.mantis.muse.repositories

import android.database.sqlite.SQLiteConstraintException
import androidx.core.net.toUri
import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
                songDao.getSongsInPlaylist(playlistEntity.id).first().map { Song(it,  artistDao.getArtistsBySong(it.id).first().map { it.name }) },
                playlistEntity.fileUri,
                playlistEntity.thumbnailUri,
            )
        }
    }
    val songsStream: Flow<List<Song>> = songDao.getAll().map { songs ->
        songs.map { song -> Song(song, artistDao.getArtistsBySong(song.id).first().map { artist -> artist.name }) }
    }
    val artistStream: Flow<List<Artist>> = artistDao.getAllArtists().map { artists ->
        artists.map { artist -> Artist(artist.name) }
    }

    @Transaction
    suspend fun getSongById(songId: Long): Song?{
        val songEnt = songDao.getSongById(songId)?:return null
        val artists = artistDao.getArtistsBySong(songId).first().map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt, artists)
    }

    @Transaction
    suspend fun getSongByName(songName: String): Song? {
        val songEnt = songDao.getSongByName(songName)?:return null
        val artists = artistDao.getArtistsBySong(songEnt.id).first().map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt, artists)
    }

    @Transaction
    suspend fun getSongByFilename(songFilename: String): Song? {
        val songEnt = songDao.getSongByFilename(songFilename)?:return null
        val artists = artistDao.getArtistsBySong(songEnt.id).first().map{ artist -> Artist(artist.name) }.map{ it.name }
        return Song(songEnt, artists)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Transaction
    fun getPlaylistByName(playlistName: String): Flow<Playlist?> =
        playlistDao.getPlaylistByName(playlistName).flatMapLatest{ playlistEntity ->
            if (playlistEntity == null) return@flatMapLatest flowOf(null)
            songDao.getSongsInPlaylist(playlistEntity.id).flatMapLatest { songs ->
                if (songs.isEmpty()) {
                    return@flatMapLatest flowOf(
                        Playlist(
                            playlistEntity.name,
                            emptyList(),
                            playlistEntity.fileUri,
                            playlistEntity.thumbnailUri
                        )
                    )
                }

                val songsWithArtists = songs.map { song ->
                    artistDao.getArtistsBySong(song.id).map { artistEntities ->
                        Song(
                            song,
                            artistEntities.map{it.name}
                        )
                    }
                }

                combine(
                    songsWithArtists
                ) { songs ->
                    Playlist(
                        playlistEntity.name,
                        songs.toList(),
                        playlistEntity.fileUri,
                        playlistEntity.thumbnailUri
                    )
                }
            }

        }

    suspend fun getArtistByName(artistName: String): Artist? {
        val artistEnt = artistDao.getArtistByName(artistName)
        return Artist(artistEnt.name)
    }

    @Transaction
    suspend fun getSongsByArtistName(artistName: String): List<Song>{
        val artistEntity = artistDao.getArtistByName(artistName)
        return songDao.getSongsFromArtist(artistEntity.id)
            .map { Song(it.name, artistDao.getArtistsBySong(it.id).first().map { it.name }, it.uri) }
    }

    suspend fun getArtistsBySong(){}

    @Transaction
    suspend fun getSongsByPlaylist(playlistName: String): List<Song>{
        val playlistId = playlistDao.getPlaylistByName(playlistName).first()?.id
        return songDao.getSongsInPlaylist(playlistId!!).first().map { Song(it, artistDao.getArtistsBySong(it.id).first().map { it.name }) }
    }
    @Transaction
    suspend fun getSongsByPlaylist(playlist: Playlist): List<Song>{
        val playlistId = playlistDao.getPlaylistByName(playlist.name).first()?.id
        return songDao.getSongsInPlaylist(playlistId!!).first().map { Song(it, artistDao.getArtistsBySong(it.id).first().map { it.name }) }
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
            var songId = songDao.insertSongs(SongEntity(song))
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
        val playlistEntity = playlistDao.getPlaylistByName(playlist.name).first() ?: throw IllegalArgumentException("Playlist does not exist")
        val songEntity = songDao.getSongByName(song.name) ?: throw IllegalArgumentException("Song does not exist")

        playlistSongRelationshipDao.insert(PlaylistSongEntryEntity(songEntity.id, playlistEntity.id, position))
    }

    val removalMutex = Mutex()

    @Transaction
    @Throws(IllegalArgumentException::class)
    suspend fun removeSongFromPlaylist(playlist: Playlist, song: Song, position: Long){
        val playlistEntity = playlistDao.getPlaylistByName(playlist.name).first() ?: throw IllegalArgumentException("Playlist does not exist")
        val songEntity = songDao.getSongsInPlaylist(playlistEntity.id).first()[position.toInt()]
//        positions.forEach { position ->
//            removalMutex.withLock {
//                println("MUTEX LOCKED")
//                playlistSongRelationshipDao.delete(PlaylistSongEntryEntity(playlistEntity.id, songEntity.id, position))
//                val entries = playlistSongRelationshipDao.playlistEntries
//                    .first()
//                    .filter { entry -> entry.position > position }
//                    .map { entry -> entry.copy(position = entry.position - 1)}
////                playlistSongRelationshipDao.update(entries)
////                playlistSongRelationshipDao.decrementPositions(playlistEntity.id, position)
//                println("MUTEX UN-LOCKED")
//            }
//        }
        playlistSongRelationshipDao.delete(PlaylistSongEntryEntity(playlistId = playlistEntity.id,  songId = songEntity.id, position = position))
    }

    @Transaction
    suspend fun deFragmentPlaylist(playlist: Playlist){
        val playlistEntity = playlistDao.getPlaylistByName(playlist.name).first() ?: throw IllegalArgumentException("Playlist does not exist")
        val songs = songDao.getSongsInPlaylist(playlistEntity.id).first()
        playlistSongRelationshipDao.playlistEntries
            .first()
            .filter{entity -> entity.playlistId == playlistEntity.id}
            .forEach { playlistSongEntry -> playlistSongRelationshipDao.delete(playlistSongEntry) }
        songs.forEachIndexed { idx, song ->  playlistSongRelationshipDao.insert(PlaylistSongEntryEntity(playlistId = playlistEntity.id, songId = song.id, position = idx.toLong())) }
    }

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