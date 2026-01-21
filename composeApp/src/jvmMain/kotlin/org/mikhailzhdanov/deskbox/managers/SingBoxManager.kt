package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.mikhailzhdanov.deskbox.LogLine
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.tools.ConfigTunPatcher
import org.mikhailzhdanov.deskbox.tools.OSChecker
import org.mikhailzhdanov.deskbox.tools.OSType
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object SingBoxManager {

    private const val CONFIG_FILE_NAME = "config.json"
    private const val CONFIG_TEMP_FILE_NAME = "config_temp.json"
    private const val MAX_LOG_LINES = 300

    private val scope = CoroutineScope(Dispatchers.IO)
    private val osType = OSChecker.currentOS.type
    private val coreFileName = osType.getCoreFileName()
    private val coreFile = File(osType.getWorkingDir(), coreFileName)
    private val configFile = File(osType.getWorkingDir(), CONFIG_FILE_NAME)
    private val configTempFile = File(osType.getWorkingDir(), CONFIG_TEMP_FILE_NAME)
    private val _logs = MutableStateFlow(emptyList<LogLine>())
    private val _isRunning = MutableStateFlow(false)
    private val _version = MutableStateFlow("")

    private var process: Process? = null
    private var logsJob: Job? = null
    private var stopCompletion: (() -> Unit)? = null

    const val ERROR_PREFIX = "Error:"
    const val CONFIG_OVERRIDE_VALUE_KEY = "deskbox_override_value"

    val logs = _logs.asStateFlow()
    val isRunning = _isRunning.asStateFlow()
    val version = _version.asStateFlow()

    var lastStartedProfile: Profile? = null
        private set

    init {
        fetchVersion()
    }

    fun start(profile: Profile) {
        if (_isRunning.value || _version.value.startsWith(ERROR_PREFIX)) return
        killExistingCore()
        var config = configWithoutOverrideAndroidVPN(profile.config)
        config = ConfigTunPatcher.patchTunInterfaceName(
            os = OSChecker.currentOS,
            configJson = config
        )
        val overrideText = SettingsManager.configOverrideValue.value
        if (overrideText.isNotEmpty()) {
            config = config.replace(CONFIG_OVERRIDE_VALUE_KEY, overrideText)
        }
        configFile.writeText(config)
        process = ProcessBuilder(coreFile.absolutePath, "run", "-c", configFile.absolutePath)
            .redirectErrorStream(true)
            .start()
        _logs.value = emptyList()
        _isRunning.value = true
        RemoteConfigsManager.startConfigUpdates(profile)
        lastStartedProfile = profile
        logsJob?.cancel()
        logsJob = scope.launch(Dispatchers.IO) {
            try {
                process?.inputStream?.bufferedReader()?.useLines { lines ->
                    lines.forEach { line ->
                        appendLog(line)
                    }
                }
            } catch (e: IOException) {}
            process?.waitFor()
            withContext(Dispatchers.Main) {
                _isRunning.value = false
                RemoteConfigsManager.stopConfigUpdates()
                stopCompletion?.let { completion ->
                    completion()
                    stopCompletion = null
                }
            }
        }
    }

    fun stop(completion: (() -> Unit)? = null) {
        if (!_isRunning.value) {
            completion?.let { it() }
            return
        }
        stopCompletion = completion
        process?.destroy()
    }

    fun validateConfig(config: String): String {
        val config = configWithoutOverrideAndroidVPN(config)
        configTempFile.writeText(config)
        val process = ProcessBuilder(coreFile.absolutePath, "check", "-c", configTempFile.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        var lines = output.lines().filter { line ->
            line.contains("FATAL")
        }.map { line ->
            var result = line.split("] ").lastOrNull() ?: ""
            result = result.replace(
                oldValue = "config at " + configTempFile.absolutePath,
                newValue = CONFIG_TEMP_FILE_NAME
            )
            result.replaceFirstChar { it.uppercaseChar() }
        }
        if (lines.count() > 1) {
            lines = lines.map { "— $it" }
        }
        return lines.joinToString("\n")
    }

    fun isValidConfig(config: String): Boolean {
        return validateConfig(config).isEmpty()
    }

    private fun appendLog(line: String) {
        if (line.trim().isEmpty()) return
        val logLine = LogLine(value = line)
        val newLines = (_logs.value + logLine).takeLast(MAX_LOG_LINES)
        _logs.value = newLines
    }

    private fun killExistingCore() {
        when (osType) {
            OSType.Windows -> {
                ProcessHandle.allProcesses()
                    .filter { it.info().command().orElse("").endsWith(coreFileName) }
                    .forEach { it.destroy() }
            }
            OSType.MacOS -> {
                val process = ProcessBuilder("ps", "axo", "pid,command").start()
                process.inputStream.bufferedReader().useLines { lines ->
                    lines
                        .filter { it.contains(coreFile.absolutePath) }
                        .forEach { line ->
                            val pid = line.trim().split("\\s+".toRegex())[0].toLong()
                            println(pid)
                            ProcessHandle.of(pid).ifPresent { it.destroy() }
                        }
                }
            }
            OSType.Linux -> {
                val process = ProcessBuilder("pgrep", "-f", coreFile.absolutePath).start()
                val pids = process.inputStream.bufferedReader()
                    .readLines()
                    .mapNotNull { it.toLongOrNull() }
                pids.forEach { pid ->
                    ProcessHandle.of(pid).ifPresent { it.destroy() }
                }
            }
        }
    }

    private fun fetchVersion() {
        scope.launch(Dispatchers.Main) {
            _version.value = try {
                if (!coreFile.exists()) {
                    throw FileNotFoundException("$coreFileName not found")
                }
                val process = ProcessBuilder(coreFile.absolutePath, "version")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                output.lines().first()
            } catch (e: Exception) {
                "$ERROR_PREFIX ${e.message}"
            }
        }
    }

    private fun configWithoutOverrideAndroidVPN(config: String): String {
        return configWithoutParamInRoute(
            config = config,
            paramName = "override_android_vpn"
        )
    }

    private fun configWithoutParamInRoute(config: String, paramName: String): String {
        if (!config.contains(paramName)) return config
        try {
            val root = Json.parseToJsonElement(config)
            if (root !is JsonObject) return config
            val route = root["route"]
            if (route !is JsonObject) return config
            val updatedRoute = JsonObject(route.filterKeys { it != paramName })
            val updatedRoot = JsonObject(
                root.toMutableMap().apply {
                    put("route", updatedRoute)
                }
            )
            return Json.encodeToString(JsonElement.serializer(), updatedRoot)
        } catch (e: Exception) {
            return config
        }
    }

}