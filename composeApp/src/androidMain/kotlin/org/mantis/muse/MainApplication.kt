package org.mantis.muse

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.mantis.muse.repositories.MediaRepository
import org.mantis.muse.repositories.PlaylistRepository
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.storage.LocalFileSource
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.util.AndroidMediaPlayer
import org.mantis.muse.viewmodels.MediaPlayerViewModel
import org.mantis.muse.viewmodels.PlaylistPickerViewModel


class MainApplication: Application() {

//    private val koinModules = module {
//        // Media Player instance
//        single { AndroidMediaPlayer(null) }
//        single { AudioPlayer(get()) }
//
//        // physical storage location instances
//        single { Room.databaseBuilder(
//            context = get(),
//            klass = MusicCacheDB::class.java,
//            name = "MusicCache"
//        ).fallbackToDestructiveMigration()
//            .build() } withOptions {
//            createdAtStart()
//        }
//        single { LocalFileSource(get()) }
//
//        // DAO instances
//        single { get<MusicCacheDB>().playlistDAO() }
//        single { get<MusicCacheDB>().songDAO() }
//
//        // Repository instances
//        single { SongRepository(get(), get()) }
//        single { PlaylistRepository(get(), get()) }
//
//        // ViewModel instances
//        viewModel { MediaPlayerViewModel(get<AndroidMediaPlayer>()) }
//        viewModel { PlaylistPickerViewModel(get(),get(),get<AndroidMediaPlayer>()) }
//    }

    override fun onCreate() {
        super.onCreate()
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
                        ).fallbackToDestructiveMigration()
                            .build() } withOptions {
                            createdAtStart()
                        }
                        single { LocalFileSource(get()) }

                        // DAO instances
                        single { get<MusicCacheDB>().playlistDAO() }
                        single { get<MusicCacheDB>().songDAO() }
                        single { get<MusicCacheDB>().artistDAO() }
                        single { get<MusicCacheDB>().artistSongRelationDao() }
                        single { get<MusicCacheDB>().playlistSongRelationDao() }

                        // Repository instances
                        single { SongRepository(get(), get()) }
                        single { PlaylistRepository(get(), get()) }
                        single { MediaRepository(get(), get(), get(), get(), get()) }

                        // ViewModel instances
                        viewModel { MediaPlayerViewModel(get<AndroidMediaPlayer>()) }
                        viewModel { PlaylistPickerViewModel(get(),get(),get<AndroidMediaPlayer>()) }
                    }
                )
            }
        }
    }
}