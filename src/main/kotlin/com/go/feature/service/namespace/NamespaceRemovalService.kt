package com.go.feature.service.namespace

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.feature.FeatureService
import com.go.feature.service.filter.FilterService
import com.go.feature.service.index.IndexVersionService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.ValidationException
import com.go.feature.util.message.NAMESPACE_NOT_FOUND_ERROR
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
            ?: throw ValidationException(NAMESPACE_NOT_FOUND_ERROR)

        filterService.deleteAllForNamespace(id)
        featureService.deleteAllForNamespace(id)
        indexVersionService.deleteAllForNamespace(id)
        namespaceRepository.deleteById(id)
    }
}