package com.go.feature.converter

import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespaceStatus
import com.go.feature.persistence.entity.Namespace
import com.go.feature.service.dto.loader.LoadedSettings
import com.go.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class NamespaceConverter {
    fun convert(entity: Namespace): NamespaceResponse {
        return NamespaceResponse(
            id = entity.id,
            name = entity.name,
            status = convertStatus(entity.status),
        )
    }

    fun create(request: NamespaceCreateRequest): Namespace {
        return Namespace(
            id = randomId(),
            name = request.name,
            status = convertStatus(request.status),
        )
    }

    fun create(request: LoadedSettings.Namespace): Namespace {
        return Namespace(
            id = randomId(),
            name = request.name,
            status = convertStatus(request.status),
        )
    }

    fun edit(editedNamespace: Namespace, request: NamespaceEditRequest): Namespace {
        return editedNamespace.copy(
            name = request.name,
            status = convertStatus(request.status)
        )
    }

    fun convertStatus(status: Namespace.Status): NamespaceStatus {
        return when (status) {
            Namespace.Status.ENABLED -> NamespaceStatus.ENABLED
            Namespace.Status.DISABLED -> NamespaceStatus.DISABLED
        }
    }

    fun convertStatus(status: NamespaceStatus): Namespace.Status {
        return when (status) {
            NamespaceStatus.ENABLED -> Namespace.Status.ENABLED
            NamespaceStatus.DISABLED -> Namespace.Status.DISABLED
        }
    }

    fun convertStatus(status: LoadedSettings.Status): Namespace.Status {
        return when (status) {
            LoadedSettings.Status.ENABLED -> Namespace.Status.ENABLED
            LoadedSettings.Status.DISABLED -> Namespace.Status.DISABLED
        }
    }
}