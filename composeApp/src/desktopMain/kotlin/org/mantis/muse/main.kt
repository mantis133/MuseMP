package org.mantis.muse




import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.mantis.muse.storage.MusicCacheDB
import java.io.File





fun getDatabaseBuilder(): RoomDatabase.Builder<MusicCacheDB> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
    return Room.databaseBuilder<MusicCacheDB>(
        name = dbFile.absolutePath,
    )
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<MusicCacheDB>
): MusicCacheDB {
    return builder
        .addMigrations()
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}


fun main() = application {

    val db: MusicCacheDB = getRoomDatabase(getDatabaseBuilder())

    Window(
        onCloseRequest = ::exitApplication,
        title = "Muse",
    ) {
        App()
    }
}