package com.go.feature.repository

import com.go.feature.entity.Namespace
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NamespaceRepository : CoroutineCrudRepository<Namespace, String> {
}