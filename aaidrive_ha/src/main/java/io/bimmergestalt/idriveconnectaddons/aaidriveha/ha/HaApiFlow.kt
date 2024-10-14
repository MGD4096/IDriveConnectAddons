package io.bimmergestalt.idriveconnectaddons.aaidriveha.ha

import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun Flow<ServerConfig>.haApi(): Flow<HaApi> = flatMapLatest { serverConfig ->
    callbackFlow<HaApi> {
        val authState = serverConfig.authState
        val haApiConnection = if (authState != null) {
            HaApiConnection.create(serverConfig.serverName, authState)
        } else {
            null
        }
        val apiFlow = when {
            serverConfig.serverName == HaApiDemo.DEMO_URL -> flowOf(HaApiDemo())
            haApiConnection != null -> haApiConnection.connect()
            else -> { flowOf(null) }
        }

        launch {
            apiFlow.collectLatest {
                if (it != null) {
                    send(it)
                } else {
                    send(HaApiDisconnected())

                    // retry every so often while the UI is watching
                    // hassApiConnection will send a new connection (or null) through apiFlow after each attempt
                    if (haApiConnection != null) {
                        delay(5_000)
                        println("Trying to reconnect haApiConnection")
                        withContext(Dispatchers.IO) {
                            haApiConnection.connect()
                        }
                    }
                }
            }
        }

        awaitClose {
            println("HassApi flow is out of scope, disconnecting")
            haApiConnection?.disconnect()
        }
    }
}