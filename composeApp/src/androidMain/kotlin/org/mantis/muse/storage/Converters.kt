package org.mantis.muse.storage

import androidx.core.net.toUri
import androidx.room.TypeConverter
import android.net.Uri

class Converters {
    @TypeConverter fun fromUri(uri: Uri): String? = uri.toString()
    @TypeConverter fun fromUriNullable(uri: Uri?): String? = uri?.toString()
    @TypeConverter fun toUri(stringURI: String) = stringURI.toUri()
    @TypeConverter fun toUriNullable(stringURI: String?) = stringURI?.toUri()
}