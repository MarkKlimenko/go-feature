package com.go.feature.service.loader

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.util.exception.ValidationException
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.File

@Service
class SettingsLoaderService(
    val applicationProperties: ApplicationProperties,
    val fileLoaderService: SettingFileLoaderService,
) {
    // TODO: use lock between services for external storage
    suspend fun loadSettings() {
        if (applicationProperties.loader.enabled) {
            val files: Array<File>? = File(applicationProperties.loader.location)
                .listFiles { _: File, name: String -> name.endsWith(SETTINGS_FILE_TYPE) }

            if (files == null) {
                logger.warn(
                    "$LOG_PREFIX Settings location not found; " +
                        "location=${applicationProperties.loader.location}"
                )
                return
            }

            if (files.isEmpty()) {
                logger.warn(
                    "$LOG_PREFIX Settings location is empty; " +
                        "fileType=$SETTINGS_FILE_TYPE, " +
                        "location=${applicationProperties.loader.location}"
                )
                return
            }

            files.forEach {
                try {
                    fileLoaderService.loadSettingFile(it)
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