package com.go.feature.persistence.repository

import com.go.feature.persistence.entity.Feature
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FeatureRepository : CoroutineCrudRepository<Feature, String>