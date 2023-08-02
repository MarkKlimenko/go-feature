package com.markklim.feature.service.namespace

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.persistence.repository.NamespaceRepository
import com.markklim.feature.service.feature.FeatureService
import com.markklim.feature.service.filter.FilterService
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.util.checkStorageForUpdateAction
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.NAMESPACE_NOT_FOUND_ERROR
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NamespaceRemovalService(
    val applicationProperties: ApplicationProperties,
    val namespaceRepository: NamespaceRepository,
    val indexVersionService: IndexVersionService,
    val filterService: FilterService,
    val featureService: FeatureService,
) {

    @Transactional(rollbackFor = [Exception::class])
    suspend fun deleteNamespace(id: String) {
        checkStorageForUpdateAction(applicationProperties)

        namespaceRepository.findById(id)
            ?: throw ClientException(NAMESPACE_NOT_FOUND_ERROR)

        filterService.deleteAllForNamespace(id)
        featureService.deleteAllForNamespace(id)
        indexVersionService.deleteAllForNamespace(id)
        namespaceRepository.deleteById(id)
    }
}