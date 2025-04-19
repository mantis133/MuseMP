package org.mantis.muse

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.room.Room
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.services.PlaybackService
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.viewmodels.MediaPlayerViewModel
import org.mantis.muse.viewmodels.PlaylistPickerViewModel
import java.io.File


class MainApplication: Application() {

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        // Save Crash logs to file
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        // Save Logcat logs to file
        val logFile = File(this.getExternalFilesDir(null), "logcat.txt")
        Runtime.getRuntime().exec("logcat " +
                "-f ${logFile.absolutePath}" +
                "-r 10240" +
                "-n 3"
        )

        println("Its Starting")
        CoroutineScope(Dispatchers.Default).launch {
            startKoin{
                androidLogger()
                androidContext(this@MainApplication)
                modules(
                    module {
                        // Media Player instance
                        single { AndroidMediaPlayer(null, get()) }

                        // physical storage location instances
                        single { Room.databaseBuilder(
                            context = get(),
                            klass = MusicCacheDB::class.java,
                            name = "MusicCache"
                        ).enableMultiInstanceInvalidation().fallbackToDestructiveMigration().build()
                        } withOptions {
                            createdAtStart()
                        }
                        single { LocalFileSource(get()) }

                        // DAO instances
                        single { get<MusicCacheDB>().playlistDAO() }
                        single { get<MusicCacheDB>().songDAO() }
                        single { get<MusicCacheDB>().artistDAO() }
                        single { get<MusicCacheDB>().artistSongRelationDao() }
                        single { get<MusicCacheDB>().playlistSongRelationDao() }
                        single { get<MusicCacheDB>().recentlyPlayedDao() }

                        // Repository instances
                        single { SongRepository(get(), get()) }
                        single { PlaylistRepository(get(), get()) }
                        single { MediaRepository(get(), get(), get(), get(), get(), get()) }

                        // ViewModel instances
                        viewModel { MediaPlayerViewModel(get(), get()) }
                        viewModel { PlaylistPickerViewModel(get(),get()) }
                    }
                )
            }
        }
    }
}


/**
 * ChatGPT implementation to save Stack traces (app crashes) to log files
 */
private const val MAX_CRASH_FILES = 200
private const val TRIM_TO_COUNT = 150

fun trimCrashLogsDirectory(dir: File, maxFiles: Int, trimTo: Int) {
    val crashFiles = dir.listFiles()?.filter { it.isFile }?.sortedBy { it.lastModified() } ?: return
    if (crashFiles.size >= maxFiles) {
        val filesToDelete = crashFiles.take(crashFiles.size - trimTo)
        filesToDelete.forEach { it.delete() }
    }
}

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crashDir = File(context.getExternalFilesDir(null), "crash_logs")
            crashDir.mkdirs()

            trimCrashLogsDirectory(crashDir, MAX_CRASH_FILES, TRIM_TO_COUNT)

            val crashFile = File(crashDir, "crash_${System.currentTimeMillis()}.txt")
            crashFile.printWriter().use { writer ->
                writer.println("Thread: ${thread.name}")
                writer.println("Exception: ${throwable.javaClass.name}")
                writer.println("Message: ${throwable.message}")
                writer.println()
                throwable.printStackTrace(writer)
            }
        } catch (e: Exception) {
            // If writing fails, continue with default handler
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

