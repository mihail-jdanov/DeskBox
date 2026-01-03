package org.mikhailzhdanov.deskbox.tools

object OldWindowsChecker {

    val isOldWindows: Boolean

    init {
        isOldWindows = checkForOldWindows()
    }

    private fun checkForOldWindows(): Boolean {
        val osName = System.getProperty("os.name") ?: return true
        val version = osName.split(" ").getOrNull(1) ?: return true
        return version == "10" || version == "8.1"
                || version == "8" || version == "7"
                || version == "Vista" || version == "XP"
    }

}