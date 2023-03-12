package com.go.feature.repository

import com.go.feature.entity.Filter
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FilterRepository : CoroutineCrudRepository<Filter, String>