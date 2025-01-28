package org.mantis.muse.storage.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import org.mantis.muse.storage.MusicCasheManager
import org.mantis.muse.storage.MusicCasheManager.Companion.SONG_TABLE_ARTIST_COLUMN
import org.mantis.muse.storage.MusicCasheManager.Companion.SONG_TABLE_FILE_NAME_COLUMN
import org.mantis.muse.storage.MusicCasheManager.Companion.SONG_TABLE_FILE_PATH_COLUMN
import org.mantis.muse.storage.MusicCasheManager.Companion.SONG_TABLE_GENRE_COLUMN
import org.mantis.muse.storage.MusicCasheManager.Companion.SONG_TABLE_NAME
import org.mantis.muse.storage.MusicCasheManager.Companion.SONG_TABLE_NAME_COLUMN
import org.mantis.muse.util.Song

fun getSongsBySongName(songName: String, context: Context) : List<Song> {
    val dbManager = MusicCasheManager(context)
    val dbConn = dbManager.readableDatabase

    val cursor = dbConn.query (
        SONG_TABLE_NAME,
        arrayOf(SONG_TABLE_NAME_COLUMN, SONG_TABLE_ARTIST_COLUMN, SONG_TABLE_FILE_PATH_COLUMN),
        "$SONG_TABLE_NAME_COLUMN = ?",
        arrayOf(songName),
        null,
        null,
        null
    )

    val results = unpackSongs(cursor)

    cursor.close()
    return results
}

fun getSongBySongFilename(songFilename: String, context: Context): List<Song> {
    val dbManager = MusicCasheManager(context)
    val dbConn = dbManager.readableDatabase

    val cursor = dbConn.query (
        SONG_TABLE_NAME,
        arrayOf(SONG_TABLE_NAME_COLUMN, SONG_TABLE_ARTIST_COLUMN, SONG_TABLE_FILE_PATH_COLUMN),
         "$SONG_TABLE_FILE_NAME_COLUMN = ?",
        arrayOf(songFilename),
        null,
        null,
        null
    )
    val results = unpackSongs(cursor)
    cursor.close()
    return results
}

private fun unpackSongs(cursor: Cursor): List<Song> {
    val results = mutableListOf<Song>()
    with(cursor){
        while (moveToNext()) {
            val name = getString(getColumnIndexOrThrow(SONG_TABLE_NAME_COLUMN))
            val artist = getInt(getColumnIndexOrThrow(SONG_TABLE_ARTIST_COLUMN)).toString()
            val filepath = getString(getColumnIndexOrThrow(SONG_TABLE_FILE_PATH_COLUMN))
            results.add(Song(name, artist, 0f, filepath))
        }
    }
    return results
}

fun insertSong(song: Song, context: Context) {
    val dbHelper = MusicCasheManager(context)
    val db = dbHelper.writableDatabase

    val values = ContentValues().apply {
        put(SONG_TABLE_NAME_COLUMN, song.name)
        put(SONG_TABLE_ARTIST_COLUMN, song.artist)
        put(SONG_TABLE_FILE_NAME_COLUMN, "")
        put(SONG_TABLE_FILE_PATH_COLUMN, song.filePath)
        put(SONG_TABLE_GENRE_COLUMN, 0)
    }
}

//fun insertData(context: Context, name: String, age: Int): Long {
//    val dbHelper = MyDatabaseHelper(context)
//    val db = dbHelper.writableDatabase
//
//    val values = ContentValues().apply {
//        put(MyDatabaseHelper.COLUMN_NAME, name)
//        put(MyDatabaseHelper.COLUMN_AGE, age)
//    }
//
//    return db.insert(MyDatabaseHelper.TABLE_NAME, null, values)
//}

//fun updateData(context: Context, id: Int, newName: String, newAge: Int): Int {
//    val dbHelper = MyDatabaseHelper(context)
//    val db = dbHelper.writableDatabase
//
//    val values = ContentValues().apply {
//        put(MyDatabaseHelper.COLUMN_NAME, newName)
//        put(MyDatabaseHelper.COLUMN_AGE, newAge)
//    }
//
//    return db.update(
//        MyDatabaseHelper.TABLE_NAME,
//        values,
//        "${MyDatabaseHelper.COLUMN_ID} = ?",
//        arrayOf(id.toString())
//    )
//}

//fun deleteData(context: Context, id: Int): Int {
//    val dbHelper = MyDatabaseHelper(context)
//    val db = dbHelper.writableDatabase
//
//    return db.delete(
//        MyDatabaseHelper.TABLE_NAME,
//        "${MyDatabaseHelper.COLUMN_ID} = ?",
//        arrayOf(id.toString())
//    )
//}

//fun getChildrenByParentId(context: Context, parentId: Int): List<String> {
//    val dbHelper = MyDatabaseHelper(context)
//    val db = dbHelper.readableDatabase
//
//    val selection = "parent_id = ?"
//    val selectionArgs = arrayOf(parentId.toString())
//
//    val cursor = db.query(
//        "Child",                        // Table name
//        arrayOf("description"),         // Columns to return
//        selection,                      // Selection (WHERE clause)
//        selectionArgs,                  // Selection arguments
//        null, null, null                // GroupBy, Having, OrderBy
//    )
//
//    val children = mutableListOf<String>()
//    with(cursor) {
//        while (moveToNext()) {
//            val description = getString(getColumnIndexOrThrow("description"))
//            children.add(description)
//        }
//    }
//    cursor.close()
//
//    return children
//}

//fun getChildrenByParentName(context: Context, parentName: String): List<String> {
//    val dbHelper = MyDatabaseHelper(context)
//    val db = dbHelper.readableDatabase
//
//    val query = """
//        SELECT description
//        FROM Child
//        WHERE parent_id = (SELECT id FROM Parent WHERE name = ?)
//    """
//
//    val cursor = db.rawQuery(query, arrayOf(parentName))
//
//    val children = mutableListOf<String>()
//    with(cursor) {
//        while (moveToNext()) {
//            val description = getString(getColumnIndexOrThrow("description"))
//            children.add(description)
//        }
//    }
//    cursor.close()
//
//    return children
//}
