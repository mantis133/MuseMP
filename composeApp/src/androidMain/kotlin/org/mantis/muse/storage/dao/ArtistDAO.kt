package org.mantis.muse.storage.dao

import android.content.Context
import org.mantis.muse.storage.MusicCasheManager
import org.mantis.muse.storage.MusicCasheManager.Companion.ARTIST_TABLE_NAME
import org.mantis.muse.storage.MusicCasheManager.Companion.ARTIST_TABLE_NAME_COLUMN

fun getArtistIDByName(artistName: String, context: Context): Long {
    val dbManager = MusicCasheManager(context)
    val dbConn = dbManager.readableDatabase

    val cursor = dbConn.query (
        ARTIST_TABLE_NAME,
        arrayOf("id"),
        "$ARTIST_TABLE_NAME_COLUMN = ?",
        arrayOf(artistName),
        null,
        null,
        null
    )

    var result: Long? = null
    with (cursor) {
        result = getLong(getColumnIndexOrThrow("id"))
    }

    cursor.close()
    return result!!
}