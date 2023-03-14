package com.go.feature.persistence.repository

import com.go.feature.persistence.entity.IndexVersion
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface IndexVersionRepository : CoroutineCrudRepository<IndexVersion, String> {
    suspend fun findByNamespace(namespaceId: String): IndexVersion?
}