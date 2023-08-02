package com.markklim.feature.service.loader.settings

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.dto.settings.loader.LoadedSettings
import com.markklim.feature.dto.status.FilterStatus
import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.persistence.entity.IndexVersion
import com.markklim.feature.persistence.entity.Namespace
import com.markklim.feature.service.feature.FeatureService
import com.markklim.feature.service.filter.FilterService
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.service.namespace.NamespaceService
import com.markklim.feature.util.exception.internal.InternalValidationException
import mu.KLogging
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AtomicSettingsLoader(
    val properties: ApplicationProperties,
    val objectMapper: ObjectMapper,
    val indexVersionService: IndexVersionService,
    val namespaceService: NamespaceService,
    val filterService: FilterService,
    val featureService: FeatureService,
) {
    // TODO: check constraints before db loading
    @Transactional(rollbackFor = [Exception::class])
    suspend fun loadSettingFile(fileByteArray: ByteArray) {
        val configHash: String = DigestUtils.md5Hex(fileByteArray)
        val settings: LoadedSettings = parseSettings(fileByteArray)

        val namespace: Namespace = namespaceService.prepareNamespaceForSettings(settings)
        val indexVersion: IndexVersion? = indexVersionService.find(namespace.id)

        logger.info("$LOG_PREFIX Prepare settings for namespace ${namespace.name}")

        if (properties.loader.forceUpdate
            || indexVersion == null
            || !properties.storage.enabled
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
        val filterMaxSize: Int = properties.filter.maxSize
        val featureMaxSize: Int = properties.feature.maxSize

        if (settings.filters.filter { it.status != FilterStatus.DISABLED }.size > filterMaxSize) {
            throw InternalValidationException("Enabled filters size exceeds $filterMaxSize")
        }

        if (settings.features.filter { it.status != Status.DISABLED }.size > featureMaxSize) {
            throw InternalValidationException("Enabled features size exceeds $featureMaxSize")
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "SETTINGS_LOADER:"
    }
}