package org.mantis.muse.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.ContentType.Application
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

class RemotePlayer(
    val ip: String,
    val port: Short,
    val advertising: Boolean,
    val initialPlayerState: PlayerState = PlayerState()
) {
    private val client = HttpClient {
        install(WebSockets)
    }
    private var session: WebSocketSession? = null
    private val outgoingMessages = Channel<String>(Channel.UNLIMITED)

    private var _connectedState = MutableStateFlow(false)
    val connectedState = _connectedState.asStateFlow()

    private var _playerState = MutableStateFlow(initialPlayerState)
    val playerState = _playerState.asStateFlow()

    fun start() = CoroutineScope(Dispatchers.IO).launch {
        client.webSocket("ws://${ip}:${port}/") {
            session = this
            _connectedState.update { true }

            val sendJob = launch {
                for (msg in outgoingMessages) {
                    send(Frame.Text(msg))
                }
            }


            for (frame in incoming) {
                onReceive(frame)
            }

            sendJob.cancel()
            _connectedState.update { false }
        }
    }

    fun close(){}

    private fun onReceive(frame: Frame) {
        println("RECEIVED A FRAME")
        if (frame is Frame.Text) {
            val command = json.decodeFromString<PlayerCommand>(frame.readText())
            when (command) {
                PlayerCommand.Play -> {_playerState.update { playerState.value.copy(playing = true) }}
                PlayerCommand.Pause -> {_playerState.update { playerState.value.copy(playing = false) }}
                PlayerCommand.SkipLast -> TODO()
                PlayerCommand.SkipNext -> TODO()
                is PlayerCommand.SeekPosition -> TODO()
                PlayerCommand.RequestState -> { send(PlayerCommand.UpdateState(playerState.value)) }
                is PlayerCommand.UpdateState -> { _playerState.update { command.state } }
            }
        }
    }

    fun send(command: PlayerCommand) {
        val serializedCommand = json.encodeToString(command)
        val result = outgoingMessages.trySend(serializedCommand)
    }
}

