package com.markklim.feature.persistence.repository

import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Feature
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FeatureRepository : CoroutineCrudRepository<Feature, String> {
    suspend fun deleteAllByNamespace(namespaceId: String)

    suspend fun findByNameAndNamespace(name: String, namespaceId: String): Feature?
    suspend fun findByNamespace(namespaceId: String): Flow<Feature>
    suspend fun findByNamespaceAndStatus(namespaceId: String, status: Status): Flow<Feature>

    suspend fun countByNamespace(namespaceId: String): Long
}