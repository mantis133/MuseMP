package org.mantis.muse.network

import androidx.compose.ui.graphics.decodeToImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.ContentType.Application
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.core.component.getScopeId
import org.mantis.muse.storage.dao.RemoteConnectionDao
import org.mantis.muse.storage.entity.RemoteConnectionEntity
import org.mantis.muse.util.PlayerState

@Serializable
sealed interface PlayerCommand {
    @Serializable data object Play: PlayerCommand
    @Serializable data object Pause: PlayerCommand
    @Serializable data object SkipNext: PlayerCommand
    @Serializable data object SkipLast: PlayerCommand
    @Serializable data class SeekPosition(val posMs: Long): PlayerCommand
    @Serializable data object RequestState: PlayerCommand
    @Serializable data class UpdateState(val state: PlayerState): PlayerCommand
}

val json = Json {
        prettyPrint = true
        classDiscriminator = "PlayerCommand"
        serializersModule = SerializersModule {
            polymorphic(PlayerCommand::class) {
                subclass(PlayerCommand.Play::class, PlayerCommand.Play.serializer())
                subclass(PlayerCommand.Pause::class, PlayerCommand.Pause.serializer())
                subclass(PlayerCommand.SkipNext::class, PlayerCommand.SkipNext.serializer())
                subclass(PlayerCommand.SkipLast::class, PlayerCommand.SkipLast.serializer())
                subclass(PlayerCommand.SeekPosition::class, PlayerCommand.SeekPosition.serializer())
                subclass(PlayerCommand.RequestState::class, PlayerCommand.RequestState.serializer())
                subclass(PlayerCommand.UpdateState::class, PlayerCommand.UpdateState.serializer())
            }
        }
    }

data class RemotePlayerState(
    val playerState: PlayerState,
    val connectedDevice: RemoteConnectionEntity?,
    val connected: Boolean,
)

class RemotePlayer(
    val initialPlayerState: PlayerState = PlayerState()
) {
    private val client = HttpClient {
        install(WebSockets)
    }

    private var session: WebSocketSession? = null
    private var socketJob: Job? = null
    private val outgoingMessages = Channel<String>(Channel.UNLIMITED)

    private var _playerState = MutableStateFlow(RemotePlayerState(
        playerState = initialPlayerState,
        connectedDevice = null,
        connected = false
    ))
    val playerState = _playerState.asStateFlow()

    fun start() {
        socketJob = CoroutineScope(Dispatchers.IO).launch {
            if (playerState.value.connectedDevice == null) return@launch
            println("Connecting to: ${playerState.value.connectedDevice?.ip}:${playerState.value.connectedDevice?.port}")
            client.webSocket("ws://${playerState.value.connectedDevice?.ip}:${playerState.value.connectedDevice?.port}/") {
                session = this
                _playerState.update { playerState.value.copy(connected = true) }
                send(PlayerCommand.RequestState)

                val sendJob = launch {
                    for (msg in outgoingMessages) {
                        println("SENDING FRAME: $msg")
                        send(Frame.Text(msg))
                    }
                }


                for (frame in incoming) {
                    onReceive(frame)
                }
                println("Quiting session")
                sendJob.cancel()
                _playerState.update { playerState.value.copy(connected = false) }
            }
        }
    }

    fun close() {
        session?.cancel()
//        socketJob?.cancel()
        _playerState.update { playerState.value.copy(connected = false) }
    }

    private fun onReceive(frame: Frame) {
        println("RECEIVED A FRAME")
        if (frame is Frame.Text) {
            val command = json.decodeFromString<PlayerCommand>(frame.readText())
            val currentPlayerState = playerState.value.playerState
            when (command) {
                PlayerCommand.Play -> {
                    println("SETTING PLAY")
                    _playerState.update { playerState.value.copy(playerState = currentPlayerState.copy(playing = true)) }
                }
                PlayerCommand.Pause -> {_playerState.update { playerState.value.copy(playerState = currentPlayerState.copy(playing = false)) }}
                PlayerCommand.SkipLast -> TODO()
                PlayerCommand.SkipNext -> TODO()
                is PlayerCommand.SeekPosition -> TODO()
                PlayerCommand.RequestState -> { send(PlayerCommand.UpdateState(playerState.value.playerState)) }
                is PlayerCommand.UpdateState -> { _playerState.update { playerState.value.copy(playerState = command.state) } }
            }
        }
    }

    fun send(command: PlayerCommand) {
        val serializedCommand = json.encodeToString<PlayerCommand>(command)
        val result = outgoingMessages.trySend(serializedCommand)
        println( "$result + $serializedCommand")
    }

    fun connectDevice(device: RemoteConnectionEntity?) {
        _playerState.update { playerState.value.copy(connectedDevice = device) }
    }
}

