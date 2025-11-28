package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mikhailzhdanov.deskbox.Profile
import java.io.File
import java.io.FileNotFoundException

object SingBoxManager {

    private const val CORE_FILE_NAME = "sing-box.exe"
    private const val CONFIG_FILE_NAME = "config.json"
    private const val MAX_LOG_LINES = 1000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val coreFile = File(CORE_FILE_NAME)
    private val configFile = File(CONFIG_FILE_NAME)
    private val _logs = MutableStateFlow("")
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
        configFile.writeText(profile.config)
        process = ProcessBuilder(coreFile.absolutePath, "run", "-c", configFile.absolutePath)
            .redirectErrorStream(true)
            .start()
        _logs.value = ""
        _isRunning.value = true
        lastStartedProfile = profile
        logJob = scope.launch(Dispatchers.IO) {
            process?.inputStream?.bufferedReader()?.useLines { lines ->
                lines.forEach {
                    appendLog(it)
                }
            }
            process?.waitFor()
            withContext(Dispatchers.Main) {
                _isRunning.value = false
            }
        }
    }

    fun stop() {
        if (!_isRunning.value) return
        process?.destroy()
        process = null
        logJob?.cancel()
        _isRunning.value = false
    }

    private fun appendLog(line: String) {
        val currentLines = _logs.value.lines()
        val trimmed = if (currentLines.size >= MAX_LOG_LINES) {
            currentLines.takeLast(MAX_LOG_LINES - 1)
        } else {
            currentLines
        }
        _logs.value = (trimmed + line).joinToString("\n").trim()
    }

    private fun killExistingCore() {
        ProcessHandle.allProcesses()
            .filter { it.info().command().orElse("").endsWith(CORE_FILE_NAME) }
            .forEach { it.destroyForcibly() }
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

}