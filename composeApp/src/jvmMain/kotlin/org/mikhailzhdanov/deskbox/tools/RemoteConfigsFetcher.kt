package org.mikhailzhdanov.deskbox.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object RemoteConfigsFetcher {

    private const val TIMEOUT_INTERVAL: Long = 10

    suspend fun fetchConfig(url: String): String = withContext(Dispatchers.IO) {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_INTERVAL))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI(url))
            .timeout(Duration.ofSeconds(TIMEOUT_INTERVAL))
            .header("User-Agent", "SFM")
            .GET()
            .build()
        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) {
                response.body()
            } else {
                throw Exception("HTTP error ${response.statusCode()}")
            }
        } catch (e: Exception) {
            if (e.message == null) {
                throw Exception("Failed to fetch data", e)
            } else {
                throw Exception("Failed to fetch data: ${e.message}", e)
            }
        }
    }

}