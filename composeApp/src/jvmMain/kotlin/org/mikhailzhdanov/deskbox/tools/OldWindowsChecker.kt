package org.mikhailzhdanov.deskbox.tools

object OldWindowsChecker {

    val isOldWindows: Boolean

    init {
        isOldWindows = checkForOldWindows()
    }

    private fun checkForOldWindows(): Boolean {
        val osName = System.getProperty("os.name") ?: return true
        return osName == "Windows 10" || osName == "Windows 8.1"
                || osName == "Windows 8" || osName == "Windows 7"
                || osName == "Windows Vista"
    }

}