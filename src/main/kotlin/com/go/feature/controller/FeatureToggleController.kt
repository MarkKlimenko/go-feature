package com.go.feature.controller

import com.go.feature.controller.dto.featuretoggle.FeatureToggleRequest
import com.go.feature.controller.dto.featuretoggle.FeatureToggleResponse
import com.go.feature.service.FeatureToggleService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/feature-toggle")
class FeatureToggleController(
    val featureToggleService: FeatureToggleService
) {
    @PostMapping("find")
    suspend fun findFeatureToggles(
        @RequestBody request: FeatureToggleRequest
    ): FeatureToggleResponse = featureToggleService.findFeatureToggles(request)
}