package com.go.feature.service.loader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.dto.status.FilterStatus
import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.persistence.entity.Namespace
import com.go.feature.service.FeatureService
import com.go.feature.service.FilterService
import com.go.feature.service.IndexVersionService
import com.go.feature.service.NamespaceService
import com.go.feature.util.exception.ValidationException
import mu.KLogging
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Service
class SettingFileLoaderService(
    val applicationProperties: ApplicationProperties,
    val objectMapper: ObjectMapper,
    val indexVersionService: IndexVersionService,
    val namespaceService: NamespaceService,
    val filterService: FilterService,
    val featureService: FeatureService,
) {
    // TODO: check constraints before db loading
    @Transactional(rollbackFor = [Exception::class])
    suspend fun loadSettingFile(file: File) {
        loadSettingFile(file.readBytes())
    }

    suspend fun loadSettingFile(fileByteArray: ByteArray) {
        val configHash: String = DigestUtils.md5Hex(fileByteArray)
        val settings: LoadedSettings = parseSettings(fileByteArray)

        val namespace: Namespace = namespaceService.getNamespaceForSettings(settings)
        val indexVersion: IndexVersion? = indexVersionService.find(namespace.id)

        logger.info("$LOG_PREFIX Prepare settings for namespace ${namespace.name}")

        if (applicationProperties.loader.forceUpdate
            || indexVersion == null
            || indexVersion.indexVersionValue != configHash
        ) {
            logger.info("$LOG_PREFIX Start settings loading for namespace ${namespace.name}")

            filterService.deleteAllForNamespace(namespace.id)
            featureService.deleteAllForNamespace(namespace.id)

            val filters: List<Filter> = filterService.createFiltersForSettings(namespace.id, settings)
            featureService.createFeaturesForSettings(namespace.id, settings, filters)
            indexVersionService.update(indexVersion, namespace.id, configHash)

            logger.info("$LOG_PREFIX Finish settings loading for namespace ${namespace.name}")
        } else {
            logger.info("$LOG_PREFIX Settings are up to date for namespace ${namespace.name}")
        }
    }

    private fun parseSettings(fileByteArray: ByteArray): LoadedSettings =
        objectMapper.readValue<LoadedSettings>(fileByteArray)
            .also { checkSettings(it) }

    private fun checkSettings(settings: LoadedSettings) {
        if (settings.filters.filter { it.status != FilterStatus.DISABLED }.size > FILTERS_MAX_SIZE) {
            throw ValidationException("Enabled filters size exceeds $FILTERS_MAX_SIZE")
        }

        if (settings.features.filter { it.status != Status.DISABLED }.size > FEATURES_MAX_SIZE) {
            throw ValidationException("Enabled features size exceeds $FEATURES_MAX_SIZE")
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "SETTINGS_LOADER:"

        const val FILTERS_MAX_SIZE = 20
        const val FEATURES_MAX_SIZE = 100
    }
}