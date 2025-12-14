package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mikhailzhdanov.deskbox.LogLine
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.tools.ConfigTunPatcher
import java.io.File
import java.io.FileNotFoundException

object SingBoxManager {

    private const val CORE_FILE_NAME = "sing-box.exe"
    private const val CONFIG_FILE_NAME = "config.json"
    private const val CONFIG_TEMP_FILE_NAME = "config_temp.json"
    private const val MAX_LOG_LINES = 300

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val coreFile = File(CORE_FILE_NAME)
    private val configFile = File(CONFIG_FILE_NAME)
    private var configTempFile = File(CONFIG_TEMP_FILE_NAME)
    private val _logs = MutableStateFlow(emptyList<LogLine>())
    private val _isRunning = MutableStateFlow(false)
    private val _version = MutableStateFlow("")

    private var process: Process? = null
    private var logJob: Job? = null

    const val ERROR_PREFIX = "Error:"

    val logs = _logs.asStateFlow()
    val isRunning = _isRunning.asStateFlow()
    val version = _version.asStateFlow()

    var lastStartedProfile: Profile? = null
        private set

    init {
        fetchVersion()
    }

    fun start(profile: Profile) {
        if (_isRunning.value) return
        killExistingCore()
//        val config = configWithoutPlatformSpecificKeys(profile.config)
//        configFile.writeText(config)
        val config = ConfigTunPatcher.patchTunInterfaceName(profile.config)
        configFile.writeText(config)
        process = ProcessBuilder(coreFile.absolutePath, "run", "-c", configFile.absolutePath)
            .redirectErrorStream(true)
            .start()
        _logs.value = emptyList()
        _isRunning.value = true
        RemoteConfigsManager.startConfigUpdates(profile)
        lastStartedProfile = profile
        logJob?.cancel()
        logJob = scope.launch(Dispatchers.IO) {
            process?.inputStream?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    appendLog(line)
                }
            }
            process?.waitFor()
            withContext(Dispatchers.Main) {
                _isRunning.value = false
                RemoteConfigsManager.stopConfigUpdates()
            }
        }
    }

    fun stop() {
        if (!_isRunning.value) return
        process?.destroy()
    }

    fun validateConfig(config: String): String {
//        val config = configWithoutPlatformSpecificKeys(config)
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
        ProcessHandle.allProcesses()
            .filter { it.info().command().orElse("").endsWith(CORE_FILE_NAME) }
            .forEach { it.destroy() }
    }

    private fun fetchVersion() {
        scope.launch(Dispatchers.Main) {
            _version.value = try {
                if (!coreFile.exists()) {
                    throw FileNotFoundException("$CORE_FILE_NAME not found")
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

//    private fun configWithoutPlatformSpecificKeys(config: String): String {
//        val updConfig = configWithoutParamInRoute(
//            config = config,
//            paramName = "override_android_vpn" // Android only key
//        )
//        return configWithoutParamInRoute(
//            config = updConfig,
//            paramName = "default_mark" // Linux only key
//        )
//    }
//
//    private fun configWithoutParamInRoute(config: String, paramName: String): String {
//        if (!config.contains(paramName)) return config
//        try {
//            val root = Json.parseToJsonElement(config)
//            if (root !is JsonObject) return config
//            val route = root["route"]
//            if (route !is JsonObject) return config
//            val updatedRoute = JsonObject(route.filterKeys { it != paramName })
//            val updatedRoot = JsonObject(
//                root.toMutableMap().apply {
//                    put("route", updatedRoute)
//                }
//            )
//            return Json.encodeToString(JsonElement.serializer(), updatedRoot)
//        } catch (e: Exception) {
//            return config
//        }
//    }

}