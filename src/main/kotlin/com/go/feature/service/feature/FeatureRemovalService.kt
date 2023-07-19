package com.go.feature.service.feature

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.repository.FeatureRepository
import com.go.feature.service.index.IndexVersionService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.localized.ClientException
import com.go.feature.util.message.FEATURE_NOT_FOUND_ERROR
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeatureRemovalService(
    val featureRepository: FeatureRepository,
    val indexVersionService: IndexVersionService,
    val applicationProperties: ApplicationProperties,
) {

    @Transactional(rollbackFor = [Exception::class])
    suspend fun deleteFeature(id: String) {
        checkStorageForUpdateAction(applicationProperties)

        val deletedFeature: Feature = featureRepository.findById(id)
            ?: throw ClientException(FEATURE_NOT_FOUND_ERROR)

        featureRepository.deleteById(id)
        indexVersionService.update(deletedFeature.namespace)
    }
}