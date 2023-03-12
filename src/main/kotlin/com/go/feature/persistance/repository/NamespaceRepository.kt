package com.go.feature.persistance.repository

import com.go.feature.persistance.entity.Namespace
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NamespaceRepository : CoroutineCrudRepository<Namespace, String>