package com.go.feature.service.feature

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.controller.dto.feature.FeatureCreateRequest
import com.go.feature.controller.dto.feature.FeatureEditRequest
import com.go.feature.controller.dto.feature.FeatureResponse
import com.go.feature.controller.dto.feature.FeaturesResponse
import com.go.feature.converter.FeatureConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FeatureRepository
import com.go.feature.service.index.IndexVersionService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.ValidationException
import com.go.feature.util.message.FEATURE_ALREADY_EXISTS_ERROR
import com.go.feature.util.message.FEATURE_NOT_FOUND_ERROR
import com.go.feature.util.message.filterIsUsedError
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
    val applicationProperties: ApplicationProperties,
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
            ?: throw ValidationException(FEATURE_NOT_FOUND_ERROR)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun createFeature(request: FeatureCreateRequest): FeatureResponse {
        checkStorageForUpdateAction(applicationProperties)

        featureRepository.findByNameAndNamespace(request.name, request.namespace)
            ?.let { throw ValidationException(FEATURE_ALREADY_EXISTS_ERROR) }

        return featureRepository.save(featureConverter.create(request))
            .let {
                indexVersionService.update(it.namespace)
                featureConverter.convert(it)
            }
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun editFeature(id: String, request: FeatureEditRequest): FeatureResponse {
        checkStorageForUpdateAction(applicationProperties)

        val requiredFeature: Feature = featureRepository.findById(id)
            ?: throw ValidationException(FEATURE_NOT_FOUND_ERROR)

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
                        throw ValidationException(filterIsUsedError(filter, feature))
                    }
                }
            }
    }

    suspend fun createFeaturesForSettings(namespaceId: String, settings: LoadedSettings, filters: List<Filter>) {
        val nameToFilterMap: Map<String, Filter> = filters.associateBy { it.name }
        val features: List<Feature> = featureConverter.create(namespaceId, settings.features, nameToFilterMap)
        featureRepository.saveAll(features).collect()
    }
}