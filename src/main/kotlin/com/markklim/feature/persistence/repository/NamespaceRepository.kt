package com.markklim.feature.persistence.repository

import com.markklim.feature.persistence.entity.Namespace
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NamespaceRepository : CoroutineCrudRepository<Namespace, String> {
    suspend fun findByName(name: String): Namespace?
}