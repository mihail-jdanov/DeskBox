package org.mikhailzhdanov.deskbox.tools

import org.mikhailzhdanov.deskbox.APP_NAME
import oshi.SystemInfo
import java.io.File
import java.nio.file.Paths
import java.util.UUID

object OSChecker {

    val currentOS: OS

    private val osName = System.getProperty("os.name") ?: ""

    init {
        currentOS = fetchCurrentOS()
    }

    private fun fetchCurrentOS(): OS {
        val name = osName.lowercase()
        return when {
            name.contains("win") -> OS(OSType.Windows, checkForOldWindows())
            name.contains("mac") -> OS(OSType.MacOS, false)
            else -> OS(OSType.Linux, false)
        }
    }

    private fun checkForOldWindows(): Boolean {
        val version = osName.split(" ").getOrNull(1) ?: return true
        return version == "10" || version == "8.1"
                || version == "8" || version == "7"
                || version == "Vista" || version == "XP"
    }

}

data class OS(
    val type: OSType,
    val isOldOS: Boolean
)

enum class OSType {
    Windows,
    MacOS,
    Linux;

    fun getWorkingDir(): String {
        when (this) {
            Windows -> {
                val name = getCoreFileName()
                return File(name).absolutePath.removeSuffix(name)
            }
            MacOS -> {
                val home = System.getProperty("user.home")
                val path = Paths.get(home, "Library", "Application Support", APP_NAME)
                return path.toString()
            }
            Linux -> {
                val name = getCoreFileName()
                return File(name).absolutePath.removeSuffix(name).removeSuffix("bin/")
            }
        }
    }

    fun getCoreFileName(): String {
        return when (this) {
            Windows -> "sing-box.exe"
            MacOS, Linux -> "sing-box"
        }
    }

    fun getWindowCornerRadius(): Int {
        return when (this) {
            Windows, Linux -> 8
            MacOS -> 0
        }
    }

    fun needsCustomTitleBar(): Boolean {
        return when (this) {
            Windows, Linux -> true
            MacOS -> false
        }
    }

    fun isLocalDNSOverrideRequired(): Boolean {
        return when (this) {
            Windows -> false
            MacOS, Linux -> true
        }
    }

    fun getHWID(): String {
        val systemInfo = SystemInfo()
        val hardware = systemInfo.hardware
        val processor = hardware.processor
        val vendor = systemInfo.operatingSystem.manufacturer
        val baseboard = hardware.computerSystem.baseboard.toString()
        val identifier = processor.processorIdentifier.identifier
        val hardwareID = when (this) {
            Windows, Linux -> {
                processor.processorIdentifier.processorID
            }
            MacOS -> {
                hardware.computerSystem.hardwareUUID
            }
        }
        val result = vendor + baseboard + identifier + hardwareID + APP_NAME
        return UUID.nameUUIDFromBytes(
            result.toByteArray(Charsets.UTF_8)
        ).toString().uppercase()
    }

}