package com.go.feature.service.loader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.converter.FeatureConverter
import com.go.feature.converter.FilterConverter
import com.go.feature.converter.NamespaceConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.dto.status.FilterStatus
import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.FeatureRepository
import com.go.feature.persistence.repository.FilterRepository
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.IndexVersionService
import com.go.feature.util.exception.ValidationException
import kotlinx.coroutines.flow.collect
import mu.KLogging
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Service
class SettingsLoaderService(
    val applicationProperties: ApplicationProperties,
    val objectMapper: ObjectMapper,
    val indexVersionService: IndexVersionService,
    val namespaceRepository: NamespaceRepository,
    val filterRepository: FilterRepository,
    val featureRepository: FeatureRepository,
    val namespaceConverter: NamespaceConverter,
    val featureConverter: FeatureConverter,
    val filterConverter: FilterConverter,
) {
    //TODO: use lock between services for external storage
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
                    loadSetting(it)
                } catch (e: ValidationException) {
                    logger.error("Error: ${e.message}")
                } catch (e: Exception) {
                    logger.error("Error: ", e)
                }
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun loadSetting(file: File) {
        val fileByteArray: ByteArray = file.readBytes()
        val configHash: String = DigestUtils.md5Hex(fileByteArray)

        val settings: LoadedSettings = objectMapper.readValue(fileByteArray)
        checkSettings(settings)

        val namespace: Namespace = namespaceRepository.findByName(settings.namespace.name)
            ?: namespaceRepository.save(namespaceConverter.create(settings.namespace))

        logger.info("$LOG_PREFIX Prepare settings for namespace ${namespace.name}")

        val indexVersion: IndexVersion? = indexVersionService.find(namespace.id)

        if (applicationProperties.loader.forceUpdate
            || indexVersion == null
            || indexVersion.indexVersionValue != configHash
        ) {
            logger.info("$LOG_PREFIX Start settings loading for namespace ${namespace.name}")

            filterRepository.deleteAllByNamespace(namespace.id)
            featureRepository.deleteAllByNamespace(namespace.id)

            val filters: List<Filter> = filterConverter.convert(namespace.id, settings.filters)
            filterRepository.saveAll(filters).collect()

            val nameToFilterMap: Map<String, Filter> = filters.associateBy { it.name }
            val features: List<Feature> = featureConverter.create(namespace.id, settings.features, nameToFilterMap)
            featureRepository.saveAll(features).collect()

            indexVersionService.update(indexVersion, namespace.id, configHash)

            logger.info("$LOG_PREFIX Finish settings loading for namespace ${namespace.name}")
        } else {
            logger.info("$LOG_PREFIX Settings are up to date for namespace ${namespace.name}")
        }
    }

    private fun checkSettings(settings: LoadedSettings) {
        if (settings.filters.filter { it.status != FilterStatus.DISABLED }.size > 20) {
            throw ValidationException("Enabled filters size exceeds 20")
        }

        if (settings.features.filter { it.status != Status.DISABLED }.size > 100) {
            throw ValidationException("Enabled features size exceeds 100")
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "SETTINGS_LOADER:"

        const val SETTINGS_FILE_TYPE = ".json"
    }
}