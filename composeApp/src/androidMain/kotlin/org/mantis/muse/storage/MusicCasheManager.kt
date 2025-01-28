package org.mantis.muse.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MusicCasheManager(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "musicCache.db"
        const val DATABASE_VERSION = 1


        const val SONG_TABLE_NAME = "Songs"
        const val SONG_TABLE_NAME_COLUMN = "name"
        const val SONG_TABLE_ARTIST_COLUMN = "artist"
        const val SONG_TABLE_FILE_NAME_COLUMN = "file_name"
        const val SONG_TABLE_FILE_PATH_COLUMN = "file_path"
        const val SONG_TABLE_GENRE_COLUMN = "genre"

        const val ARTIST_TABLE_NAME = "Artists"
        const val ARTIST_TABLE_NAME_COLUMN = "name"


        const val GENRE_TABLE_NAME = "Genre"


        const val PLAYLIST_TABLE_NAME = "Playlists"
    }

    private val songTable = """
        CREATE TABLE $SONG_TABLE_NAME (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            $SONG_TABLE_NAME_COLUMN TEXT NOT NULL,
            $SONG_TABLE_ARTIST_COLUMN INTEGER NOT NULL,
            $SONG_TABLE_FILE_NAME_COLUMN TEXT NOT NULL,
            $SONG_TABLE_FILE_PATH_COLUMN TEXT NOT NULL,
            $SONG_TABLE_GENRE_COLUMN INTEGER,

            FOREIGN KEY ($SONG_TABLE_ARTIST_COLUMN) REFERENCES Artists(id) ON DELETE CASCADE ON UPDATE CASCADE,
            FOREIGN KEY ($SONG_TABLE_GENRE_COLUMN) REFERENCES Genres(id) ON DELETE CASCADE ON UPDATE CASCADE
        );
    """.trimIndent()

    private val artistTable = """
        CREATE TABLE $ARTIST_TABLE_NAME (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            $ARTIST_TABLE_NAME_COLUMN TEXT NOT NULL,
            thumbnail_filepath TEXT
        );
    """.trimIndent();

    private val playlistTable = """
        CREATE TABLE $PLAYLIST_TABLE_NAME (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            thumbnail_filepath TEXT,
            filepath TEXT NOT NULL
        );
    """.trimIndent()

    private val genreTable  = """
        CREATE TABLE $GENRE_TABLE_NAME (
            id INTEGER PRIMARY KEY AUTOINCREMENT
        );
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(artistTable)
        db?.execSQL(playlistTable)
        db?.execSQL(genreTable)
        db?.execSQL(songTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, prevVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $ARTIST_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $PLAYLIST_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $GENRE_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $SONG_TABLE_NAME")
        onCreate(db)
        Log.d("DATABASE UPDATE", "database updated $prevVersion -> $newVersion")
    }
}




/*
Many-Many Relationships That I Am too Tired to Deal With
- Genre <-> Artist: Each artis could perform songs in many genres, each genre is performed by many artists. SOLUTION -> Query through the song table?
- Playlist <-> Song: Each song could be in multiple playlists, Each playlist is made up of multiple songs. SOLUTION ->
*/

