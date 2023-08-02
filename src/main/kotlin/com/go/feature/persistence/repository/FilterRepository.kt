package com.go.feature.persistence.repository

import com.go.feature.persistence.entity.Filter
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FilterRepository : CoroutineCrudRepository<Filter, String> {
    suspend fun deleteAllByNamespace(namespaceId: String)

    suspend fun findByNamespace(namespaceId: String): Flow<Filter>
    suspend fun findByNameAndNamespace(name: String, namespaceId: String): Filter?

    suspend fun countByNamespace(namespaceId: String): Long
}