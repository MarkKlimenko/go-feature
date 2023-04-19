package com.go.feature.controller

import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.service.NamespaceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/namespaces")
class NamespaceController(
    val namespaceService: NamespaceService
) {
    @GetMapping
    suspend fun getNamespaces(): NamespacesResponse = namespaceService.getNamespaces()

    @PostMapping
    suspend fun createNamespace(
        request: NamespaceCreateRequest
    ): NamespaceResponse = namespaceService.createNamespace(request)

    @PostMapping("{id}")
    suspend fun editNamespace(
        @PathVariable id: String,
        request: NamespaceEditRequest
    ): NamespaceResponse = namespaceService.editNamespace(id, request)
}