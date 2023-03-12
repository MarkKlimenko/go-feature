package com.go.feature.persistance.repository

import com.go.feature.persistance.entity.Feature
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FeatureRepository : CoroutineCrudRepository<Feature, String>