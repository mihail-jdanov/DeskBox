package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.tools.JsonFormatter
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object ProfilesManager {

    private const val PROFILES_FILE_NAME = "profiles.json"

    private val profilesFile = File(PROFILES_FILE_NAME)
    private val json = Json { prettyPrint = true }
    private val _profiles = MutableStateFlow(emptyList<Profile>())
    private val _profileToImport: MutableStateFlow<Profile?> = MutableStateFlow(null)

    val profiles = _profiles.asStateFlow()
    val profileToImport = _profileToImport.asStateFlow()

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
        val profile = profile.copy(
            config = JsonFormatter.formatJson(profile.config)
        )
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
                SingBoxManager.stop {
                    SingBoxManager.start(profile)
                }
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

    fun importRemoteProfile(string: String) {
        if (!string.startsWith("sing-box://import-remote-profile")) return
        val uri = URI(string)
        val rawName = uri.fragment ?: ""
        var name = URLDecoder.decode(rawName, StandardCharsets.UTF_8.name()) ?: ""
        val query = uri.rawQuery ?: ""
        val url = query
            .split("&").mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .firstOrNull { (key, _) -> key == "url" }
            ?.second
            ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
            ?: ""
        val host = getHost(url)
        if (name.isEmpty()) name = host ?: ""
        if (url.isNotEmpty()) {
            val profile = Profile(
                name = name,
                isRemote = true,
                remoteURL = url
            )
            val host = host ?: "remote resource"
            AlertsManager.setAlert(
                AlertData(
                    text = "Import remote profile \"$name\"?\n\nYou will connect to $host to download the configuration.",
                    confirmButtonData = AlertButtonData(
                        title = "Import",
                        handler = {
                            _profileToImport.value = profile
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(500)
                                _profileToImport.value = null
                            }
                        }
                    ),
                    cancelButtonData = AlertButtonData.cancel()
                )
            )
        }
    }

    private fun getHost(url: String): String? {
        return try {
            val uri = URI(url)
            uri.host
        } catch (e: Exception) {
            null
        }
    }

}