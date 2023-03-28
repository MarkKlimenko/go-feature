package com.go.feature.persistence.repository

import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Feature
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FeatureRepository : CoroutineCrudRepository<Feature, String> {
    suspend fun deleteAllByNamespace(namespaceId: String)
    suspend fun findByNamespaceAndStatus(namespaceId: String, status: Status): Flow<Feature>
}