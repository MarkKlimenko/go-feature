package com.go.feature.persistence.repository

import com.go.feature.persistence.entity.Filter
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FilterRepository : CoroutineCrudRepository<Filter, String> {
    suspend fun deleteAllByNamespace(namespaceId: String)
}