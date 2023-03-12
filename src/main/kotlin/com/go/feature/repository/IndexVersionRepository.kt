package com.go.feature.repository

import com.go.feature.entity.IndexVersion
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface IndexVersionRepository : CoroutineCrudRepository<IndexVersion, String> {
    suspend fun findByNamespace(namespace: String): IndexVersion?
}