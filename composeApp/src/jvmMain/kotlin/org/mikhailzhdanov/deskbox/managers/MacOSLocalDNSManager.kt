package org.mikhailzhdanov.deskbox.managers

import org.mikhailzhdanov.deskbox.extensions.isValidIP
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object MacOSLocalDNSManager {

    private const val MONITORING_INTERVAL = 5L
    private const val COMMANDS_TIMEOUT = 3L

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    private var task: ScheduledFuture<*>? = null
    private var tunIP: String = ""

    @Volatile
    private var lastDefaultInterface: String? = null

    fun startService(tunIP: String) {
        if (!tunIP.isValidIP()) return
        this.tunIP = tunIP
        if (task != null && !task!!.isDone) return
        task = scheduler.scheduleAtFixedRate({
            if (tunIP.isBlank()) return@scheduleAtFixedRate
            try {
                val currentInterface = getDefaultInterface()
                if (currentInterface == null && lastDefaultInterface != null) {
                    setDNSServers(lastDefaultInterface!!, emptyList())
                    lastDefaultInterface = null
                    return@scheduleAtFixedRate
                }
                if (currentInterface != null && currentInterface == lastDefaultInterface) return@scheduleAtFixedRate
                lastDefaultInterface?.let { setDNSServers(it, emptyList()) }
                currentInterface?.let { setDNSServers(it, listOf(tunIP)) }
                lastDefaultInterface = currentInterface
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, MONITORING_INTERVAL, TimeUnit.SECONDS)
    }

    fun stopService() {
        task?.cancel(true)
        task = null
        lastDefaultInterface?.let { setDNSServers(it, emptyList()) }
        lastDefaultInterface = null
        tunIP = ""
    }

    private fun getDefaultInterface(): String? {
        val output = runCommand("route get default | grep interface")
        return output?.lines()
            ?.firstNotNullOfOrNull { line ->
                if (line.trim().startsWith("interface:")) {
                    line.split(":").getOrNull(1)?.trim()
                } else null
            }
            .takeIf { it?.isNotEmpty() == true }
    }

    private fun setDNSServers(interfaceName: String, dns: List<String>) {
        val service = getServiceByInterface(interfaceName) ?: return
        val cmd = if (dns.isEmpty()) {
            "networksetup -setdnsservers \"$service\" Empty"
        } else {
            "networksetup -setdnsservers \"$service\" ${dns.joinToString(" ")}"
        }
        runCommand(cmd)
    }

    private fun getServiceByInterface(interfaceName: String): String? {
        val output = runCommand("networksetup -listnetworkserviceorder")
        val lines = output?.lines() ?: emptyList()
        for (i in 1 until lines.size) {
            val deviceMatch = Regex("Device: ([^,)]+)").find(lines[i])
            if (deviceMatch != null && deviceMatch.groupValues[1].trim() == interfaceName) {
                val serviceLine = lines[i - 1].trim()
                val serviceMatch = Regex("\\) (.+)").find(serviceLine)
                if (serviceMatch != null) {
                    return serviceMatch.groupValues[1].trim()
                }
            }
        }
        return null
    }

    private fun runCommand(command: String): String? {
        return try {
            val process = ProcessBuilder("bash", "-c", command)
                .redirectErrorStream(true)
                .start()
            if (!process.waitFor(COMMANDS_TIMEOUT, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                null
            } else {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.lineSequence().joinToString("\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
