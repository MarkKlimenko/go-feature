package com.markklim.feature.persistence.repository

import com.markklim.feature.persistence.entity.IndexVersion
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface IndexVersionRepository : CoroutineCrudRepository<IndexVersion, String> {
    suspend fun findByNamespace(namespaceId: String): IndexVersion?
    suspend fun deleteAllByNamespace(namespaceId: String)
}