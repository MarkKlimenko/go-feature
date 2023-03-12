package com.go.feature.persistance.repository

import com.go.feature.persistance.entity.IndexVersion
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface IndexVersionRepository : CoroutineCrudRepository<IndexVersion, String> {
    suspend fun findByNamespace(namespace: String): IndexVersion?
}