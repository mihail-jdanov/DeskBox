package org.mikhailzhdanov.deskbox.managers

import io.github.vinceglb.autolaunch.AutoLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.APP_NAME
import org.mikhailzhdanov.deskbox.tools.OSChecker
import org.mikhailzhdanov.deskbox.tools.OSType
import java.io.File
import java.nio.charset.Charset

object AutorunManager {

    val pathToExecutable = ProcessHandle.current()
        .info()
        .command()
        .orElse(null)

    private val scope = CoroutineScope(Dispatchers.IO)
    private val osType = OSChecker.currentOS.type

    private val macAutoLaunch by lazy {
        AutoLaunch(
            appPackageName = "org.mikhailzhdanov.deskbox",
            appPath = pathToExecutable
        )
    }

    private val linuxAutostartDir by lazy {
        File(System.getProperty("user.home"), ".config/autostart")
    }

    private val linuxAutostartFile by lazy {
        File(linuxAutostartDir, "$APP_NAME.desktop")
    }

    fun createTask(completion: (Boolean) -> Unit) {
        scope.launch {
            when (osType) {
                OSType.Windows -> createWindowsTask()
                OSType.MacOS -> macAutoLaunch.enable()
                OSType.Linux -> createLinuxTask()
            }
            completion(isTaskActive())
        }
    }

    fun removeTask(completion: (Boolean) -> Unit) {
        scope.launch {
            when (osType) {
                OSType.Windows -> removeWindowsTask()
                OSType.MacOS -> macAutoLaunch.disable()
                OSType.Linux -> removeLinuxTask()
            }
            completion(!isTaskActive())
        }
    }

    fun isTaskActive(callback: (Boolean) -> Unit) {
        scope.launch {
            callback(isTaskActive())
        }
    }

    private suspend fun isTaskActive(): Boolean {
        return when (osType) {
            OSType.Windows -> isWindowsTaskActive()
            OSType.MacOS -> macAutoLaunch.isEnabled()
            OSType.Linux -> isLinuxTaskActive()
        }
    }

    private suspend fun createWindowsTask() {
        removeWindowsTask()
        val xml = """
            <?xml version="1.0" encoding="UTF-16"?>
            <Task version="1.4" xmlns="http://schemas.microsoft.com/windows/2004/02/mit/task">
              <RegistrationInfo>
                <Author>${System.getProperty("user.name")}</Author>
              </RegistrationInfo>
              <Triggers>
                <LogonTrigger>
                  <Enabled>true</Enabled>
                  <UserId>${getWindowsUserID()}</UserId>
                </LogonTrigger>
              </Triggers>
              <Principals>
                <Principal id="Author">
                  <LogonType>InteractiveToken</LogonType>
                  <RunLevel>HighestAvailable</RunLevel>
                </Principal>
              </Principals>
              <Settings>
                <MultipleInstancesPolicy>IgnoreNew</MultipleInstancesPolicy>
                <DisallowStartIfOnBatteries>false</DisallowStartIfOnBatteries>
                <StopIfGoingOnBatteries>false</StopIfGoingOnBatteries>
                <StartWhenAvailable>true</StartWhenAvailable>
                <RunOnlyIfNetworkAvailable>false</RunOnlyIfNetworkAvailable>
                <IdleSettings>
                  <StopOnIdleEnd>false</StopOnIdleEnd>
                  <RestartOnIdle>false</RestartOnIdle>
                </IdleSettings>
                <Enabled>true</Enabled>
                <Hidden>false</Hidden>
                <RunOnlyIfIdle>false</RunOnlyIfIdle>
              </Settings>
              <Actions Context="Author">
                <Exec>
                  <Command>"$pathToExecutable"</Command>
                  <WorkingDirectory>${getWindowsWorkingDir()}</WorkingDirectory>
                </Exec>
              </Actions>
            </Task>
        """.trimIndent()
        val xmlFile = File.createTempFile("task_", ".xml")
        xmlFile.outputStream().use { stream ->
            stream.write(byteArrayOf(0xFF.toByte(), 0xFE.toByte()))
            stream.write(xml.toByteArray(Charset.forName("UTF-16LE")))
        }
        val process = ProcessBuilder(
            "schtasks",
            "/Create",
            "/TN", "DeskBox_Autostart",
            "/XML", xmlFile.absolutePath,
            "/F"
        ).redirectErrorStream(true).start()
        process.waitFor()
        xmlFile.delete()
    }

    private suspend fun removeWindowsTask() {
        if (isTaskActive()) {
            ProcessBuilder(
                "schtasks",
                "/Delete",
                "/TN", "DeskBox_Autostart",
                "/F"
            ).start()
        }
    }

    private suspend fun isWindowsTaskActive(): Boolean {
        try {
            val process = ProcessBuilder(
                "schtasks",
                "/Query",
                "/TN", "DeskBox_Autostart",
                "/FO", "LIST",
                "/V"
            ).start()
            val output = process.inputStream.bufferedReader(Charset.forName("CP866")).readText()
            process.waitFor()
            if (!output.contains("DeskBox_Autostart")) return false
            if (!output.contains(pathToExecutable, ignoreCase = true)) return false
            return output.contains(getUserName(), ignoreCase = true)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private suspend fun getWindowsUserID(): String {
        val proc = ProcessBuilder("whoami", "/user").start()
        val output = proc.inputStream.bufferedReader(Charset.forName("CP866")).readText()
        proc.waitFor()
        val lastLine = output.lines().last { it.trim().isNotEmpty() }
        return lastLine.split(" ").last()
    }

    private fun getUserName(): String {
        return System.getProperty("user.name")
    }

    private fun getWindowsWorkingDir(): String {
        return File(pathToExecutable).parent
    }

    private fun createLinuxTask() {
        removeLinuxTask()
        val dir = OSType.Linux.getWorkingDir()
        val content = """
            [Desktop Entry]
            Name=$APP_NAME
            Exec=${dir}bin/$APP_NAME %u
            Path=$dir
            Icon=${dir}lib/$APP_NAME.png
            Terminal=false
            Categories=Network;
            Type=Application
            StartupNotify=false
            StartupWMClass=org-mikhailzhdanov-deskbox-MainKt
            MimeType=x-scheme-handler/sing-box;
            X-GNOME-Autostart-enabled=true
        """.trimIndent()
        if (!linuxAutostartDir.exists()) linuxAutostartDir.mkdirs()
        linuxAutostartFile.writeText(content)
    }

    private fun removeLinuxTask() {
        if (isLinuxTaskActive()) {
            linuxAutostartFile.delete()
        }
    }

    private fun isLinuxTaskActive(): Boolean {
        return linuxAutostartFile.exists()
    }

}