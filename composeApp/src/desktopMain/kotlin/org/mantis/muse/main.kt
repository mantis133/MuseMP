package org.mantis.muse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.DropdownMenuState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.ContentType.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import muse.composeapp.generated.resources.Res
import muse.composeapp.generated.resources.*
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.getScopeId
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.mantis.muse.layout.components.BufferedImage
import org.mantis.muse.network.PlayerCommand
import org.mantis.muse.network.RemotePlayer
import org.mantis.muse.storage.MusicCacheDB
import org.mantis.muse.storage.dao.RemoteConnectionDao
import org.mantis.muse.storage.dao.SongDao
import org.mantis.muse.storage.entity.RemoteConnectionEntity
import org.mantis.muse.util.LoopState
import org.mantis.muse.util.PlayerState
import java.io.File
import javax.sound.midi.Track


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
    single { get<MusicCacheDB>().remoteConnectionDao() }
}

val repositoryKoinModule = module {

}

val viewModelsKoinModule = module {
    viewModel { RemotePlayerViewModel(get()) }
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

data class RemotePlayerViewState (
    val connected: Boolean,
    val connectedDevice: RemoteConnectionEntity?,
    val playerState: PlayerState,
)

class RemotePlayerViewModel(
    val remoteConnectionDao: RemoteConnectionDao,
): ViewModel()
{

    var remotePlayer: RemotePlayer = RemotePlayer()

    private var _uiState = MutableStateFlow(RemotePlayerViewState(
        connected = false,
        connectedDevice = null,
        playerState = PlayerState()
    ))
    val uiState = _uiState.combine(remotePlayer.playerState){ uiState, remoteState ->
        RemotePlayerViewState(
            connected = remoteState.connected,
            connectedDevice = remoteState.connectedDevice,
            playerState = remoteState.playerState
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = RemotePlayerViewState(
            connected = false,
            connectedDevice = null,
            playerState = PlayerState()
        ),
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    val connections = remoteConnectionDao.connections.stateIn(
        scope = viewModelScope,
        initialValue = listOf(),
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun addRemote(name: String, ip: String, port: Short) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteConnectionDao.insert(RemoteConnectionEntity(id = 0, displayName = name, ip = ip, port = port))
        }
    }

    fun connect() {
        remotePlayer.start()
    }

    fun disconnect() {
        remotePlayer.close()
    }

    fun play(){
        remotePlayer.send(PlayerCommand.Play)
    }

    fun pause() {
        remotePlayer.send(PlayerCommand.Pause)
    }

    fun skipNext(){
        remotePlayer.send(PlayerCommand.SkipNext)
    }

    fun skipLast(){
        remotePlayer.send(PlayerCommand.SkipLast)
    }

    fun setRemote(remote: RemoteConnectionEntity?) {
        remotePlayer.connectDevice(remote)
    }
}

fun main() = application {

    startKoin {
        modules(
            databaseKoinModule,
            repositoryKoinModule,
            viewModelsKoinModule
        )
    }

    KoinContext{

//        val placeholderImage = imageResource(Res.drawable.compose_multiplatform)

        Window(
            onCloseRequest = ::exitApplication,
            title = "Muse",
        ) {
            val viewModel: RemotePlayerViewModel = koinViewModel()
            val uiState by viewModel.remotePlayer.playerState.collectAsStateWithLifecycle()
            val viewUiState by viewModel.uiState.collectAsStateWithLifecycle()

            val connectionOptions by viewModel.connections.collectAsStateWithLifecycle()

            var addRemoteFormExpanded by remember { mutableStateOf(false) }

            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .background(Color.Gray)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Button(
                            onClick = { if (viewUiState.connected) viewModel.disconnect() else viewModel.connect() }
                        ){
                            Text(if (viewUiState.connected) "Disconnect" else "Connect")
                        }
                        Spacer(
                            modifier = Modifier
                                .size(50.dp)
                                .background(if (viewUiState.connected) Color.Green else Color.Red)
                        )
                        Box(){
                            Row(modifier = Modifier.clickable { expanded = !expanded }) {
                                Icon(
                                    painterResource(Res.drawable.compose_multiplatform),
                                    contentDescription = "More options"
                                )
                                Text(viewUiState.connectedDevice?.displayName ?: "Select A Host")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier,
                            ) {
                                connectionOptions.forEach { option ->
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.setRemote(option)
                                            expanded = false
                                        }
                                    ) { Text(option.displayName) }
                                }
                                DropdownMenuItem(
                                    onClick = { addRemoteFormExpanded = true; expanded = false }
                                ) { Text("+Add Option") }
                            }
                        }
                    }

                }


                HorizontalMediaPlayerUi(
                    songName = uiState.playerState.songTitle,
                    artistsName = uiState.playerState.songArtists,
//                getSongThumbnail = suspend { placeholderImage },
                    trackPosition = 0,
                    trackDurationMS = 1000,
                    playing = uiState.playerState.playing,
                    loopState = uiState.playerState.loopState,
                    shuffleState = uiState.playerState.shuffling,
                    togglePlayPause = { if (viewUiState.playerState.playing) viewModel.pause() else viewModel.play() },
                    skipLast = viewModel::skipLast,
                    skipNext = viewModel::skipNext,
                    nextLoopState = {  },
                    toggleShuffle = {  },
                    onSeek = {  },
                    modifier = Modifier,
                )
            }
            AnimatedVisibility(addRemoteFormExpanded) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.7f)
                        .background(Color.Gray)
                        .clickable{ addRemoteFormExpanded = false }
                ){
                    AddRemoteForm(
                        onSubmit = { name, ip, port ->
                            viewModel.addRemote(name, ip, port)
                            addRemoteFormExpanded = false
                        },
                        onDismiss = { addRemoteFormExpanded = false },
                        modifier = Modifier
                            .alpha(1f)
                            .background(Color.Black)
                            .padding(5.dp)
                            .border(width = 1.dp, color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalMediaPlayerUi(
    songName: String,
    artistsName: String,
//    getSongThumbnail: suspend () -> ImageBitmap,
    trackPosition: Long,
    trackDurationMS: Long,
    playing: Boolean,
    loopState: LoopState,
    shuffleState: Boolean,
    togglePlayPause: () -> Unit,
    skipLast: () -> Unit,
    skipNext: () -> Unit,
    nextLoopState: () -> Unit,
    toggleShuffle: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier:Modifier = Modifier
) {
    Column (
        modifier = modifier
    ) {
        Row {
//            BufferedImage(
//                imageProvider = getSongThumbnail,
//                contentDescription = null
//            )
            Column {
                Text(text = songName)
                Text(text = artistsName)
                Row {
                    IconButton(
                        onClick = skipLast
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.compose_multiplatform),
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = togglePlayPause,
                        modifier = Modifier
                    ) {
                        Icon(
                            painter = painterResource(if (playing) Res.drawable.pause_button else Res.drawable.play_button),
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = skipNext
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.compose_multiplatform),
                            contentDescription = null
                        )
                    }
                }
            }
        }
        Slider(
            value = trackPosition / 1000f,
            onValueChange = { value ->
                onSeek(value.toLong() * 1000L)
            },
            valueRange = 0f..(trackDurationMS/1000f),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun AddRemoteForm(
    onSubmit: (String, String, Short) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("19742") }

    Column(
        modifier
    ) {
        TextField(
            value = name,
            singleLine = true,
            onValueChange = {newName -> name = newName},
            modifier = Modifier
                .background(Color.Gray)
        )
        TextField(
            value = ip,
            singleLine = true,
            onValueChange = {newIp -> ip = newIp},
            modifier = Modifier
                .background(Color.Gray)
        )
        TextField(
            value = port,
            singleLine = true,
            onValueChange = {newPort -> port = newPort.filter{it.isDigit()} },
            modifier = Modifier
                .background(Color.Gray)
        )
        Row {
            Button(onClick = { onSubmit(name, ip, port.toShort()) }){Text("Submit")}
            Button(onClick = onDismiss){Text("Cancel")}
        }
    }
}

