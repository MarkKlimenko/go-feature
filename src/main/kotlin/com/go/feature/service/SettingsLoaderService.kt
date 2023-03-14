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
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

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

    //TODO: load all directory
    suspend fun loadSettings() {
        // TODO: do for each file
        // TODO: transactional for each file
        if (applicationProperties.loader.enabled) {
            // TODO: read bytes nonblocking
            val settings: LoadedSettings =
                objectMapper.readValue(Files.readAllBytes(Paths.get(applicationProperties.loader.location)))

            val configHash: String = "TODO1" // TODO: implement

            val namespace: Namespace = namespaceRepository.findByName(settings.namespace.name)
                ?: namespaceRepository.save(namespaceConverter.create(settings.namespace))

            val indexVersion: IndexVersion? = indexVersionRepository.findByNamespace(namespace.id)

            // update configs
            if (indexVersion == null || indexVersion.indexVersion != configHash) {
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
                                ?: throw IllegalArgumentException("No filter with name=${filter.name}"),
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
            } else {
                // do nothing, configs are already updated
            }
        }
    }
}