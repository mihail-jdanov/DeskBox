package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.tools.RemoteConfigsFetcher
import org.mikhailzhdanov.deskbox.tools.TimestampFormatter

object RemoteConfigsManager {

    private const val JOB_INTERVAL: Long = 60

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    fun startConfigUpdates(profile: Profile) {
        stopConfigUpdates()
        job = scope.launch {
            while (isActive) {
                val profile = ProfilesManager.profiles.value.firstOrNull {
                    it.id == profile.id
                }
                if (profile != null) {
                    if (profile.isRemote && profile.autoUpdate) {
                        updateConfigIfNeeded(profile)
                    }
                } else {
                    stopConfigUpdates()
                }
                delay(JOB_INTERVAL * 1000)
            }
        }
    }

    fun stopConfigUpdates() {
        job?.cancel()
        job = null
    }

    private suspend fun updateConfigIfNeeded(profile: Profile) {
        val interval = (profile.autoUpdateInterval ?: 0) * 60
        val currentTimestamp = TimestampFormatter.getCurrentTimestamp()
        val isTimeToUpdate = currentTimestamp - profile.lastUpdateTimestamp >= interval
        if (isTimeToUpdate) {
            try {
                val config = RemoteConfigsFetcher.fetchConfig(profile.remoteURL)
                if (SingBoxManager.isValidConfig(config)) {
                    val currentProfile = ProfilesManager.profiles.value.firstOrNull {
                        it.id == profile.id
                    }
                    if (currentProfile != null
                        && currentProfile.isRemote && currentProfile.autoUpdate
                        && currentProfile.remoteURL == profile.remoteURL) {
                        val currentTimestamp = TimestampFormatter.getCurrentTimestamp()
                        ProfilesManager.saveProfile(
                            currentProfile.copy(
                                config = config,
                                lastUpdateTimestamp = currentTimestamp
                            )
                        )
                    } else {
                        stopConfigUpdates()
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

}