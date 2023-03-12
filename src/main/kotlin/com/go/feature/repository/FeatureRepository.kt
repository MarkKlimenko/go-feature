package com.go.feature.repository

import com.go.feature.entity.Feature
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FeatureRepository : CoroutineCrudRepository<Feature, String>