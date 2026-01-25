package org.mikhailzhdanov.deskbox.managers

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object MacOSLocalDNSManager {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val originalDNSMap = mutableMapOf<String, List<String>>()
    private val monitoringInterval = 5L

    private var task: ScheduledFuture<*>? = null
    private var tunIP: String = ""

    @Volatile
    private var lastActiveNetwork: String? = null

    fun startService(tunIP: String) {
        this.tunIP = tunIP
        if (task != null && !task!!.isDone) return // уже запущен
        task = scheduler.scheduleAtFixedRate({
            try {
                val activeNetwork = getActiveNetworkService() ?: run {
                    lastActiveNetwork?.let { oldNet ->
                        originalDNSMap[oldNet]?.let { dns ->
                            setDNSServers(oldNet, dns)
                        }
                    }
                    lastActiveNetwork = null
                    return@scheduleAtFixedRate
                }
                if (activeNetwork == lastActiveNetwork) return@scheduleAtFixedRate
                lastActiveNetwork?.let { oldNet ->
                    originalDNSMap[oldNet]?.let { dns ->
                        setDNSServers(oldNet, dns)
                    }
                }
                if (!originalDNSMap.containsKey(activeNetwork)) {
                    originalDNSMap[activeNetwork] = getDNSServers(activeNetwork)
                }
                setDNSServers(activeNetwork, listOf(tunIP))
                lastActiveNetwork = activeNetwork
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, monitoringInterval, TimeUnit.SECONDS)
    }

    fun stopService() {
        task?.cancel(true)
        task = null
        lastActiveNetwork?.let { network ->
            originalDNSMap[network]?.let { dns ->
                setDNSServers(network, dns)
            }
        }
        originalDNSMap.clear()
        lastActiveNetwork = null
    }

    private fun getActiveNetworkService(): String? {
        val output = runCommand("networksetup -listnetworkserviceorder")
        val services = output.lines()
            .mapNotNull { line -> Regex("\\) (.+)").find(line)?.groupValues?.get(1) }
        for (service in services) {
            val status = runCommand("networksetup -getinfo \"$service\"")
            val ipv4 = status.lines()
                .find { it.startsWith("IP address:") }
                ?.substringAfter("IP address:")?.trim()
            val ipv6 = status.lines()
                .find { it.startsWith("IPv6 IP address:") }
                ?.substringAfter("IPv6 IP address:")?.trim()

            if (!ipv4.isNullOrEmpty() && ipv4.lowercase() != "none") return service
            if (!ipv6.isNullOrEmpty() && ipv6.lowercase() != "none") return service
        }
        return null
    }

    private fun getDNSServers(service: String): List<String> {
        val output = runCommand("networksetup -getdnsservers \"$service\"")
        val lines = output.lines().filter { it.isNotBlank() }
        return if (lines.firstOrNull()?.contains(" ") == true) emptyList() else lines
    }

    private fun setDNSServers(service: String, dns: List<String>) {
        if (dns.isEmpty()) {
            runCommand("networksetup -setdnsservers \"$service\" Empty")
        } else {
            runCommand("networksetup -setdnsservers \"$service\" ${dns.joinToString(" ")}")
        }
    }

    private fun runCommand(command: String): String {
        val process = ProcessBuilder("bash", "-c", command)
            .redirectErrorStream(true)
            .start()
        val output = StringBuilder()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                output.appendLine(line)
                line = reader.readLine()
            }
        }
        process.waitFor()
        return output.toString().trim()
    }

}
