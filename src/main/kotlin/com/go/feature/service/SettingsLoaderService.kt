package com.go.feature.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.converter.FeatureConverter
import com.go.feature.converter.NamespaceConverter
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.FeatureRepository
import com.go.feature.persistence.repository.FilterRepository
import com.go.feature.persistence.repository.IndexVersionRepository
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.dto.loader.LoadedSettings
import com.go.feature.util.randomId
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
    val namespaceRepository: NamespaceRepository,
    val namespaceConverter: NamespaceConverter,
    val indexVersionRepository: IndexVersionRepository,
    val filterRepository: FilterRepository,
    val featureRepository: FeatureRepository,
    val featureConverter: FeatureConverter,
) {

    suspend fun loadSettings() {
        if (applicationProperties.loader.enabled) {
            val files: Array<File>? = File(applicationProperties.loader.location)
                .listFiles { _: File, name: String -> name.endsWith(SETTINGS_FILE_TYPE) }

            if (files == null) {
                logger.warn(
                    "${LOG_PREFIX} Settings location not found; " +
                            "location=${applicationProperties.loader.location}"
                )
                return
            }

            if (files.isEmpty()) {
                logger.warn(
                    "${LOG_PREFIX} Settings location is empty; " +
                            "fileType=${SETTINGS_FILE_TYPE}, " +
                            "location=${applicationProperties.loader.location}"
                )
                return
            }

            files.forEach {
                loadSetting(it)
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun loadSetting(file: File) {
        val fileByteArray: ByteArray = file.readBytes()
        val configHash: String = DigestUtils.md5Hex(fileByteArray)

        val settings: LoadedSettings = objectMapper.readValue(fileByteArray)

        val namespace: Namespace = namespaceRepository.findByName(settings.namespace.name)
            ?: namespaceRepository.save(namespaceConverter.create(settings.namespace))

        logger.info("${LOG_PREFIX} Prepare settings for namespace ${namespace.name}")

        val indexVersion: IndexVersion? = indexVersionRepository.findByNamespace(namespace.id)

        // update configs
        if (indexVersion == null || indexVersion.indexVersion != configHash) {
            logger.info("${LOG_PREFIX} Start settings loading for namespace ${namespace.name}")

            // clean old configs
            filterRepository.deleteAllByNamespace(namespace.id)
            featureRepository.deleteAllByNamespace(namespace.id)

            // add new configs
            val filters: List<Filter> = settings.filters.map {
                Filter(
                    id = randomId(),
                    name = it.name,
                    namespace = namespace.id,
                    parameter = it.parameter,
                    operator = it.operator,
                    description = it.description
                )
            }
            filterRepository.saveAll(filters).collect()

            val nameToFilterMap: Map<String, Filter> = filters.associateBy { it.name }

            val features: List<Feature> = settings.features.map {
                val featureFilters: List<Feature.Filter> = it.filters.map { filter ->
                    Feature.Filter(
                        id = nameToFilterMap[filter.name]?.id
                            ?: throw IllegalArgumentException("${LOG_PREFIX} No filter with name=${filter.name}"),
                        value = filter.value
                    )
                }

                Feature(
                    id = randomId(),
                    name = it.name,
                    namespace = namespace.id,
                    filters = objectMapper.writeValueAsString(featureFilters),
                    status = featureConverter.convertStatus(it.status),
                    description = it.description
                )
            }
            featureRepository.saveAll(features).collect()

            // update index version
            if (indexVersion != null) {
                indexVersionRepository.save(
                    indexVersion.copy(indexVersion = configHash)
                )
            } else {
                indexVersionRepository.save(
                    IndexVersion(
                        id = randomId(),
                        namespace = namespace.id,
                        indexVersion = configHash
                    )
                )
            }
            logger.info("${LOG_PREFIX} Finish settings loading for namespace ${namespace.name}")
        } else {
            logger.info("${LOG_PREFIX} Settings are up to date for namespace ${namespace.name}")
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "SETTINGS_LOADER:"

        const val SETTINGS_FILE_TYPE = ".json"
    }
}