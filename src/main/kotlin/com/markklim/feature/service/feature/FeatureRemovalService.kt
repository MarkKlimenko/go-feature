package com.markklim.feature.service.feature

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.persistence.entity.Feature
import com.markklim.feature.persistence.repository.FeatureRepository
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.util.checkStorageForUpdateAction
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.FEATURE_NOT_FOUND_ERROR
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