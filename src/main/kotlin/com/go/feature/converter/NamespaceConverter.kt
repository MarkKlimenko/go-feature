package com.go.feature.converter

import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Namespace
import com.go.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class NamespaceConverter {
    fun convert(entity: Namespace): NamespaceResponse {
        return NamespaceResponse(
            id = entity.id,
            name = entity.name,
            status = entity.status,
        )
    }

    fun create(request: NamespaceCreateRequest): Namespace {
        return Namespace(
            id = randomId(),
            name = request.name,
            status = request.status,
        )
    }

    fun create(namespaceSetting: LoadedSettings.Namespace): Namespace {
        return Namespace(
            id = randomId(),
            name = namespaceSetting.name,
            status = namespaceSetting.status,
        )
    }

    fun edit(editedNamespace: Namespace, request: NamespaceEditRequest): Namespace {
        return editedNamespace.copy(
            name = request.name,
            status = request.status
        )
    }
}