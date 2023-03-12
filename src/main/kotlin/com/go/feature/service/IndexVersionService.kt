package com.go.feature.service

import com.go.feature.entity.IndexVersion
import com.go.feature.repository.IndexVersionRepository
import com.go.feature.util.randomId
import com.go.feature.util.randomVersion
import org.springframework.stereotype.Service

@Service
class IndexVersionService(
    val indexVersionRepository: IndexVersionRepository,
) {

    suspend fun updateIndex(namespaceId: String) {
        val version: IndexVersion = indexVersionRepository.findByNamespace(namespaceId)
            ?.copy(indexVersion = randomVersion())
            ?: IndexVersion(
                id = randomId(),
                namespace = namespaceId,
                indexVersion = randomVersion()
            )

        indexVersionRepository.save(version)
    }
}