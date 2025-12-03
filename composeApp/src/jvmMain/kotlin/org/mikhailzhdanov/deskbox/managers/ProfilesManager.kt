package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import org.mikhailzhdanov.deskbox.Profile
import java.io.File

object ProfilesManager {

    private const val PROFILES_FILE_NAME = "profiles.json"

    private val profilesFile = File(PROFILES_FILE_NAME)
    private val json = Json { prettyPrint = true }
    private val _profiles = MutableStateFlow(emptyList<Profile>())

    val profiles = _profiles.asStateFlow()

    init {
        fetchProfiles()
    }

    fun fetchProfiles() {
        if (!profilesFile.exists()) {
            _profiles.value = emptyList()
            return
        }
        _profiles.value = try {
            json.decodeFromString(
                deserializer = ListSerializer(Profile.serializer()),
                string = profilesFile.readText()
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveProfile(profile: Profile) {
        val oldProfile = _profiles.value.firstOrNull { it.id == profile.id }
        val currentProfiles = _profiles.value.toMutableList()
        val existingIndex = currentProfiles.indexOfFirst { it.id == profile.id }
        if (existingIndex >= 0) {
            currentProfiles[existingIndex] = profile
        } else {
            currentProfiles += profile
        }
        profilesFile.writeText(
            json.encodeToString(
                serializer = ListSerializer(Profile.serializer()),
                value = currentProfiles
            )
        )
        _profiles.value = currentProfiles
        oldProfile?.let { oldProfile ->
            if (SingBoxManager.isRunning.value
                && SingBoxManager.lastStartedProfile?.id == profile.id
                && oldProfile.config != profile.config) {
                SingBoxManager.stop()
                SingBoxManager.start(profile)
            }
        }
    }

    fun deleteProfile(profile: Profile) {
        val currentProfiles = _profiles.value.toMutableList()
        currentProfiles.removeAll { it.id == profile.id }
        profilesFile.writeText(
            json.encodeToString(
                serializer = ListSerializer(Profile.serializer()),
                value = currentProfiles
            )
        )
        _profiles.value = currentProfiles
        if (SettingsManager.selectedProfileID.value == profile.id) {
            SettingsManager.setSelectedProfileID(null)
        }
    }

}