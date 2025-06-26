package org.mantis.muse




import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.ContentType.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.mantis.muse.network.PlayerCommand
import org.mantis.muse.network.RemotePlayer
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.util.PlayerState
import java.io.File


val databaseKoinModule = module {
    // physical storage location instances
    single {
        getRoomDatabase(getDatabaseBuilder())
    } withOptions {
        createdAtStart()
    }

    // DAO instances
    single { get<MusicCacheDB>().playlistDAO() }
    single { get<MusicCacheDB>().songDAO() }
    single { get<MusicCacheDB>().artistDAO() }
    single { get<MusicCacheDB>().artistSongRelationDao() }
    single { get<MusicCacheDB>().playlistSongRelationDao() }
    single { get<MusicCacheDB>().recentlyPlayedDao() }
}

val repositoryKoinModule = module {

}

val viewModelsKoinModule = module {
    viewModel { RemotePlayerViewModel() }
}

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

class RemotePlayerViewModel(): ViewModel() {

    var remotePlayer: RemotePlayer = RemotePlayer("localhost", 19742, false)

    init {
        remotePlayer.start()
    }

    private var _uiState = MutableStateFlow(PlayerState)
    val uiState = _uiState.asStateFlow()

    fun connect(ip: String, port: Short) {
        remotePlayer = RemotePlayer(ip, port, false)
        remotePlayer.start()
    }

    fun play(){
        remotePlayer.send(PlayerCommand.Play)
    }


}

fun main() = application {

//    CoroutineScope(Dispatchers.Default).launch {
        startKoin {
            modules(
                databaseKoinModule,
                repositoryKoinModule,
                viewModelsKoinModule
            )
        }
//    }

    KoinContext{
        Window(
            onCloseRequest = ::exitApplication,
            title = "Muse",
        ) {
            val viewModel: RemotePlayerViewModel = koinViewModel()
            val uiState by viewModel.remotePlayer.playerState.collectAsStateWithLifecycle()
            val connected by viewModel.remotePlayer.connectedState.collectAsStateWithLifecycle()
            Column{
                if (uiState.playing) {
                    Box(Modifier.background(Color.Red).size(100.dp))
                } else {
                    Box(Modifier.background(Color.Blue).size(100.dp))
                }
                Spacer(Modifier.size(10.dp))
                if (connected) {
                    Box(Modifier.background(Color.Red).size(100.dp))
                } else {
                    Box(Modifier.background(Color.Blue).size(100.dp))
                }
            }
            Button(
                onClick = {
                    viewModel.play()}
            ){
                Text("Play")
            }

        }
    }
}