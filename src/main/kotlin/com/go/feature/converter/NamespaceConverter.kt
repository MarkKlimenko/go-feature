package com.go.feature.converter

import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Namespace
import com.go.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class NamespaceConverter {
    fun convert(entity: Namespace): NamespaceResponse =
        NamespaceResponse(
            id = entity.id,
            name = entity.name,
            status = entity.status,
            version = entity.version!!
        )

    fun create(name: String, status: Status): Namespace =
        Namespace(
            id = randomId(),
            name = name,
            status = status,
        )

    fun create(request: NamespaceCreateRequest): Namespace =
        create(request.name, request.status)

    fun create(namespaceSetting: LoadedSettings.Namespace): Namespace =
        Namespace(
            id = randomId(),
            name = namespaceSetting.name,
            status = namespaceSetting.status,
        )

    fun edit(editedNamespace: Namespace, request: NamespaceEditRequest): Namespace =
        editedNamespace.copy(
            name = request.name,
            status = request.status,
            version = request.version
        )
}