package com.markklim.feature.service.feature

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.controller.dto.feature.FeatureCreateRequest
import com.markklim.feature.controller.dto.feature.FeatureEditRequest
import com.markklim.feature.controller.dto.feature.FeatureResponse
import com.markklim.feature.controller.dto.feature.FeaturesResponse
import com.markklim.feature.converter.FeatureConverter
import com.markklim.feature.dto.settings.loader.LoadedSettings
import com.markklim.feature.persistence.entity.Feature
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.persistence.repository.FeatureRepository
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.util.checkStorageForUpdateAction
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.FEATURE_ALREADY_EXISTS_ERROR
import com.markklim.feature.util.message.FEATURE_NOT_FOUND_ERROR
import com.markklim.feature.util.message.FEATURE_SIZE_EXCEEDS_ERROR
import com.markklim.feature.util.message.FILTER_IS_USED_ERROR
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeatureService(
    val featureRepository: FeatureRepository,
    val featureConverter: FeatureConverter,
    val indexVersionService: IndexVersionService,
    val properties: ApplicationProperties,
) {

    suspend fun getFeatures(namespaceId: String): FeaturesResponse {
        val features: List<FeatureResponse> = featureRepository.findByNamespace(namespaceId)
            .map { featureConverter.convert(it) }
            .toList()

        return FeaturesResponse(
            features = features
        )
    }

    suspend fun getFeature(id: String): FeatureResponse =
        featureRepository.findById(id)
            ?.let { featureConverter.convert(it) }
            ?: throw ClientException(FEATURE_NOT_FOUND_ERROR)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun createFeature(request: FeatureCreateRequest): FeatureResponse {
        checkStorageForUpdateAction(properties)
        checkFeatureCount(request.namespace)

        featureRepository.findByNameAndNamespace(request.name, request.namespace)
            ?.let { throw ClientException(FEATURE_ALREADY_EXISTS_ERROR) }

        return featureRepository.save(featureConverter.create(request))
            .let {
                indexVersionService.update(it.namespace)
                featureConverter.convert(it)
            }
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun editFeature(id: String, request: FeatureEditRequest): FeatureResponse {
        checkStorageForUpdateAction(properties)

        val requiredFeature: Feature = featureRepository.findById(id)
            ?: throw ClientException(FEATURE_NOT_FOUND_ERROR)

        return featureRepository.save(featureConverter.edit(requiredFeature, request))
            .let {
                indexVersionService.update(it.namespace)
                featureConverter.convert(it)
            }
    }

    suspend fun deleteAllForNamespace(namespaceId: String) {
        featureRepository.deleteAllByNamespace(namespaceId)
    }

    suspend fun validateFilterNotUsedByFeatures(filter: Filter) {
        // TODO: use pagination
        featureRepository.findByNamespace(filter.namespace)
            .collect { feature ->
                featureConverter.getFeatureFilter(feature).forEach {
                    if (it.id == filter.id) {

                        val valuesMap: Map<String, String> = mapOf(
                            "filterName" to filter.name,
                            "featureName" to feature.name,
                        )

                        throw ClientException(FILTER_IS_USED_ERROR, valuesMap)
                    }
                }
            }
    }

    suspend fun createFeaturesForSettings(namespaceId: String, settings: LoadedSettings, filters: List<Filter>) {
        val nameToFilterMap: Map<String, Filter> = filters.associateBy { it.name }
        val features: List<Feature> = featureConverter.create(namespaceId, settings.features, nameToFilterMap)
        featureRepository.saveAll(features).collect()
    }

    private suspend fun checkFeatureCount(namespaceId: String) {
        val currentCount: Long = featureRepository.countByNamespace(namespaceId)

        if (currentCount >= properties.feature.maxSize) {
            throw ClientException(
                FEATURE_SIZE_EXCEEDS_ERROR,
                mapOf("featureSize" to properties.feature.maxSize.toString())
            )
        }
    }
}