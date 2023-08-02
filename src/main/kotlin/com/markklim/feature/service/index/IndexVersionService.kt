package com.markklim.feature.service.index

import com.markklim.feature.persistence.entity.IndexVersion
import com.markklim.feature.persistence.repository.IndexVersionRepository
import com.markklim.feature.util.randomId
import com.markklim.feature.util.randomVersion
import org.springframework.stereotype.Service

@Service
class IndexVersionService(
    val indexVersionRepository: IndexVersionRepository,
) {

    suspend fun find(namespaceId: String): IndexVersion? =
        indexVersionRepository.findByNamespace(namespaceId)

    suspend fun update(namespaceId: String) {
        val version: IndexVersion = indexVersionRepository.findByNamespace(namespaceId)
            ?.copy(indexVersionValue = randomVersion())
            ?: IndexVersion(
                id = randomId(),
                namespace = namespaceId,
                indexVersionValue = randomVersion()
            )

        indexVersionRepository.save(version)
    }

    suspend fun update(indexVersion: IndexVersion?, namespaceId: String, indexVersionValue: String) {
        if (indexVersion != null) {
            indexVersionRepository.save(
                indexVersion.copy(indexVersionValue = indexVersionValue)
            )
        } else {
            indexVersionRepository.save(
                IndexVersion(
                    id = randomId(),
                    namespace = namespaceId,
                    indexVersionValue = indexVersionValue
                )
            )
        }
    }

    suspend fun deleteAllForNamespace(namespaceId: String) =
        indexVersionRepository.deleteAllByNamespace(namespaceId)
}