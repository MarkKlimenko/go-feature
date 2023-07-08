package com.go.feature.controller

import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.controller.dto.filter.FilterEditRequest
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.service.filter.FilterRemovalService
import com.go.feature.service.filter.FilterService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/filters")
class FilterController(
    val filterService: FilterService,
    val filterRemovalService: FilterRemovalService,
) {
    @GetMapping
    suspend fun getFilters(
        @RequestParam("ns") namespaceId: String
    ): FiltersResponse = filterService.getFilters(namespaceId)

    @GetMapping("{id}")
    suspend fun getFilter(
        @PathVariable id: String,
    ): FilterResponse = filterService.getFilter(id)

    @PostMapping
    suspend fun createFilter(
        @RequestBody request: FilterCreateRequest
    ): FilterResponse = filterService.createFilter(request)

    @PostMapping("{id}")
    suspend fun editFilter(
        @PathVariable id: String,
        @RequestBody request: FilterEditRequest
    ): FilterResponse = filterService.editFilter(id, request)

    @DeleteMapping("{id}")
    suspend fun deleteFilter(
        @PathVariable id: String
    ) = filterRemovalService.deleteFilter(id)
}