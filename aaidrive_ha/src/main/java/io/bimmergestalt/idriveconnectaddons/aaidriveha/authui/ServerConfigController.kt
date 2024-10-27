package io.bimmergestalt.idriveconnectaddons.aaidriveha.authui

import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import io.bimmergestalt.idriveconnectaddons.aaidriveha.OauthAccess
import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfig
import io.bimmergestalt.idriveconnectaddons.aaidriveha.ha.HaHttpClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL

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
                serverConfig.isValidServerName.value = HaHttpClient.testUri(HaHttpClient.parseUri(pendingServerName,""), serverConfig)
            }
        }
    }

    fun startLogin() {
        oauthAccess.startAuthRequest(Uri.parse(pendingServerName ?: ""))
    }
    fun logout() {
        serverConfig.authState = null
        oauthAccess.logout()
    }
}