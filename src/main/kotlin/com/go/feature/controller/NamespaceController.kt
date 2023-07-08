package com.go.feature.controller

import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.service.namespace.NamespaceRemovalService
import com.go.feature.service.namespace.NamespaceService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/namespaces")
class NamespaceController(
    val namespaceService: NamespaceService,
    val namespaceRemovalService: NamespaceRemovalService,
) {
    @GetMapping
    suspend fun getNamespaces(): NamespacesResponse = namespaceService.getNamespaces()

    @GetMapping("{id}")
    suspend fun getNamespace(
        @PathVariable id: String,
    ): NamespaceResponse = namespaceService.getNamespace(id)

    @PostMapping
    suspend fun createNamespace(
        @RequestBody @Validated request: NamespaceCreateRequest
    ): NamespaceResponse = namespaceService.createNamespace(request)

    @PostMapping("{id}")
    suspend fun editNamespace(
        @PathVariable id: String,
        @RequestBody @Validated request: NamespaceEditRequest
    ): NamespaceResponse = namespaceService.editNamespace(id, request)

    @DeleteMapping("{id}")
    suspend fun deleteNamespace(
        @PathVariable id: String
    ) = namespaceRemovalService.deleteNamespace(id)
}