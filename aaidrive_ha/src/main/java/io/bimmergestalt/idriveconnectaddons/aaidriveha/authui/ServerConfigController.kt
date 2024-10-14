package io.bimmergestalt.idriveconnectaddons.aaidriveha.authui

import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import io.bimmergestalt.idriveconnectaddons.aaidriveha.OauthAccess
import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfig
import io.bimmergestalt.idriveconnectaddons.aaidriveha.ha.HaApiDemo
import io.bimmergestalt.idriveconnectaddons.aaidriveha.ha.HaWsClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ServerConfigController(val lifecycleScope: LifecycleCoroutineScope, val serverConfig: ServerConfig, val oauthAccess: OauthAccess) {
    private var pendingServerName: String? = serverConfig.serverName
    private var setServerNameJob: Job? = null
    fun setServerName(serverName: CharSequence) {
        pendingServerName = serverName.toString()
        setServerNameJob?.cancel()
        setServerNameJob = lifecycleScope.launch {
            serverConfig.isValidServerName.value = null

            // debounce, will get cancelled for new input
            delay(1500)

            val changed = serverConfig.serverName != pendingServerName
            if (changed) {
                println("Pending $pendingServerName is different than ${serverConfig.serverName}")
                serverConfig.authState = null
            }
            var pendingServerName = pendingServerName ?: ""
            if (pendingServerName.isNotBlank() && !pendingServerName.contains("://")) {
                pendingServerName = "https://$pendingServerName"
            }
            serverConfig.serverName = pendingServerName

            if (pendingServerName.isNotBlank()) {
                if (pendingServerName == HaApiDemo.DEMO_URL) {
                    serverConfig.isValidServerName.value = true
                } else {
                    serverConfig.isValidServerName.value = HaWsClient.testUri(HaWsClient.parseUri(pendingServerName))
                }
            }
        }
    }

    fun useDemo() {
        serverConfig.serverName = HaApiDemo.DEMO_URL
        serverConfig.authState = null
    }

    fun startLogin() {
        oauthAccess.startAuthRequest(Uri.parse(pendingServerName ?: ""))
    }
    fun logout() {
        serverConfig.authState = null
        oauthAccess.logout()
    }
}