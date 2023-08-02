package com.markklim.feature.controller

import com.markklim.feature.controller.dto.feature.FeatureCreateRequest
import com.markklim.feature.controller.dto.feature.FeatureEditRequest
import com.markklim.feature.controller.dto.feature.FeatureResponse
import com.markklim.feature.controller.dto.feature.FeaturesFindRequest
import com.markklim.feature.controller.dto.feature.FeaturesFindResponse
import com.markklim.feature.controller.dto.feature.FeaturesResponse
import com.markklim.feature.service.feature.FeatureRemovalService
import com.markklim.feature.service.feature.FeatureSearchService
import com.markklim.feature.service.feature.FeatureService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/features")
class FeatureController(
    val featureSearchService: FeatureSearchService,
    val featureService: FeatureService,
    val featureRemovalService: FeatureRemovalService,
) {
    @PostMapping("search")
    suspend fun findFeatures(
        @RequestBody request: FeaturesFindRequest
    ): FeaturesFindResponse = featureSearchService.findFeatures(request)

    @GetMapping
    suspend fun getFeatures(
        @RequestParam("ns") namespaceId: String
    ): FeaturesResponse = featureService.getFeatures(namespaceId)

    @GetMapping("{id}")
    suspend fun getFeature(
        @PathVariable id: String,
    ): FeatureResponse = featureService.getFeature(id)

    @PostMapping
    suspend fun createFeature(
        @RequestBody @Validated request: FeatureCreateRequest
    ): FeatureResponse = featureService.createFeature(request)

    @PostMapping("{id}")
    suspend fun editFeature(
        @PathVariable id: String,
        @RequestBody @Validated request: FeatureEditRequest
    ): FeatureResponse = featureService.editFeature(id, request)

    @DeleteMapping("{id}")
    suspend fun deleteFeature(
        @PathVariable id: String
    ) = featureRemovalService.deleteFeature(id)
}