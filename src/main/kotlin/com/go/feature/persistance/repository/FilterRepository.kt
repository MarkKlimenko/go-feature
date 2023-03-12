package com.go.feature.persistance.repository

import com.go.feature.persistance.entity.Filter
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FilterRepository : CoroutineCrudRepository<Filter, String>