package org.mikhailzhdanov.deskbox.managers

import java.io.File
import java.nio.charset.Charset

object AutorunManager {

    val pathToExecutable = ProcessHandle.current()
        .info()
        .command()
        .orElse(null)

    fun createTask() {
        removeTask()
        val xml = """
        <?xml version="1.0" encoding="UTF-16"?>
        <Task version="1.4" xmlns="http://schemas.microsoft.com/windows/2004/02/mit/task">
          <RegistrationInfo>
            <Author>${System.getProperty("user.name")}</Author>
          </RegistrationInfo>
          <Triggers>
            <LogonTrigger>
              <Enabled>true</Enabled>
              <UserId>${getUserID()}</UserId>
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
              <WorkingDirectory>${getWorkingDir()}</WorkingDirectory>
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

    fun removeTask() {
        if (isTaskActive()) {
            ProcessBuilder(
                "schtasks",
                "/Delete",
                "/TN", "DeskBox_Autostart",
                "/F"
            ).start()
        }
    }

    fun isTaskActive(): Boolean {
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

    private fun getUserID(): String {
        val proc = ProcessBuilder("whoami", "/user").start()
        val output = proc.inputStream.bufferedReader(Charset.forName("CP866")).readText()
        proc.waitFor()
        val lastLine = output.lines().last { it.trim().isNotEmpty() }
        return lastLine.split(" ").last()
    }

    private fun getUserName(): String {
        return System.getProperty("user.name")
    }

    private fun getWorkingDir(): String {
        return File(pathToExecutable).parent
    }

}