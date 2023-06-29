package com.go.feature.service.loader.settings

import com.go.feature.component.content.provider.ContentProvider
import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.util.exception.ValidationException
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class SettingsLoaderService(
    val applicationProperties: ApplicationProperties,
    val atomicSettingsLoader: AtomicSettingsLoader,
    val settingsLocation: String,
    val settingsContentProvider: ContentProvider,
) {
    // TODO: use lock between services for external storage
    suspend fun loadSettings() {
        if (applicationProperties.loader.enabled) {
            val content: List<ByteArray> = settingsContentProvider.getContent(settingsLocation, SETTINGS_FILE_TYPE)

            if (content.isEmpty()) {
                logger.warn(
                    "$LOG_PREFIX Settings location is empty; " +
                        "fileType=$SETTINGS_FILE_TYPE, " +
                        "location=$settingsLocation"
                )
                return
            }

            content.forEach {
                try {
                    atomicSettingsLoader.loadSettingFile(it)
                } catch (e: ValidationException) {
                    logger.error("Error: ${e.message}")
                } catch (e: Exception) {
                    logger.error("Error: ", e)
                }
            }
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "SETTINGS_LOADER:"
        const val SETTINGS_FILE_TYPE = ".json"
    }
}