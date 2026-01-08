package org.mikhailzhdanov.deskbox.tools

import org.mikhailzhdanov.deskbox.APP_NAME
import java.nio.file.Paths

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
            else -> OS(OSType.MacOS, false)
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
    MacOS;

    fun getWorkingDir(): String {
        when (this) {
            Windows -> return ""
            MacOS -> {
                val home = System.getProperty("user.home")
                val path = Paths.get(home, "Library", "Application Support", APP_NAME)
                return path.toString()
            }
        }
    }

    fun getCoreFileName(): String {
        return when (this) {
            Windows -> "sing-box.exe"
            MacOS -> "sing-box"
        }
    }

    fun getWindowCornerRadius(): Int {
        return when (this) {
            Windows -> 8
            MacOS -> 0
        }
    }

    fun needsCustomTitleBar(): Boolean {
        return when (this) {
            Windows -> true
            MacOS -> false
        }
    }
}