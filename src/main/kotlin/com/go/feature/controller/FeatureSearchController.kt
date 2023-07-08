package com.go.feature.controller

import com.go.feature.controller.dto.featuretoggle.FeatureToggleRequest
import com.go.feature.controller.dto.featuretoggle.FeatureToggleResponse
import com.go.feature.service.feature.FeatureSearchService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/features/search")
class FeatureSearchController(
    val featureSearchService: FeatureSearchService
) {
    @PostMapping
    suspend fun findFeatureToggles(
        @RequestBody request: FeatureToggleRequest
    ): FeatureToggleResponse = featureSearchService.findFeatureToggles(request)
}