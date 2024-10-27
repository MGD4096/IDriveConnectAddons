package io.bimmergestalt.idriveconnectaddons.aaidriveha.ha

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.awaitString
import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfig
import java.net.URL

class HaHttpClient(val serverConfig: ServerConfig) {
   companion object {
       fun parseUri(httpUri: String, path: String): URL {
           val uri = Uri.parse(httpUri)
           val uriBuilder = uri.buildUpon()
           uriBuilder.encodedPath("/api/")
           uriBuilder.encodedPath(path)
           return URL(uriBuilder.build().toString());
       }

       suspend fun testUri(haURI: URL, serverConfig: ServerConfig): Boolean {
           if(!serverConfig.canLogout)
               return false;
            val responseString = Fuel.get(haURI.toString())
               .authentication()
               .bearer((serverConfig.authState?.accessToken ?: ""))
               .awaitString(Charsets.UTF_8)// { request, response, result ->  }
           Log.d("Request result", responseString)
           return responseString.contains("API running.");
       }
   }
   suspend fun send_get(url: URL) {
       val responseString = Fuel.get(url.toString())
           .authentication()
           .bearer((serverConfig.authState?.accessToken ?: ""))
           .awaitString(Charsets.UTF_8)
       Log.d("Request result", responseString)
   }
    suspend fun send_post(url: URL, body:String) {
        Log.d("http", url.toString())
        Log.d("http_body", body)
        try {
           val responseString = Fuel.post(url.toString())
               .authentication()
               .bearer((serverConfig.authState?.accessToken ?: ""))
               .appendHeader("Content-Type","application/json")
               .body(body)
               .awaitString(Charsets.UTF_8)
                Log.d("Request result", responseString)
        }catch (e:Exception){
            Log.d("httpError", url.toString())
        }
   }

}