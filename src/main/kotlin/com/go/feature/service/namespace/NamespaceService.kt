package com.go.feature.service.namespace

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.converter.NamespaceConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.index.IndexVersionService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.ValidationException
import com.go.feature.util.message.NAMESPACE_NOT_FOUND_ERROR
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NamespaceService(
    val applicationProperties: ApplicationProperties,
    val namespaceRepository: NamespaceRepository,
    val namespaceConverter: NamespaceConverter,
    val indexVersionService: IndexVersionService,
) {

    suspend fun getNamespaces(): NamespacesResponse {
        val namespaces: List<NamespaceResponse> = namespaceRepository.findAll()
            .map { namespaceConverter.convert(it) }
            .toList()

        return NamespacesResponse(
            namespaces = namespaces
        )
    }

    suspend fun getNamespace(id: String): NamespaceResponse =
        namespaceRepository.findById(id)
            ?.let { namespaceConverter.convert(it) }
            ?: throw ValidationException(NAMESPACE_NOT_FOUND_ERROR)

    // TODO: check Transactional
    @Transactional(rollbackFor = [Exception::class])
    suspend fun createNamespace(request: NamespaceCreateRequest): NamespaceResponse {
        checkStorageForUpdateAction(applicationProperties)

        namespaceRepository.findByName(request.name)
            ?.let { throw ValidationException("Namespace already exists") }

        val createdNamespace: Namespace = namespaceRepository.save(namespaceConverter.create(request))

        indexVersionService.update(createdNamespace.id)

        return namespaceConverter.convert(createdNamespace)
    }

    // TODO: check Transactional
    @Transactional(rollbackFor = [Exception::class])
    suspend fun editNamespace(id: String, request: NamespaceEditRequest): NamespaceResponse {
        checkStorageForUpdateAction(applicationProperties)

        val requiredNamespace: Namespace = namespaceRepository.findById(id)
            ?: throw ValidationException(NAMESPACE_NOT_FOUND_ERROR)

        val editedNamespace: Namespace = namespaceRepository.save(namespaceConverter.edit(requiredNamespace, request))

        indexVersionService.update(id)

        return namespaceConverter.convert(editedNamespace)
    }

    suspend fun createDefaultNamespace() {
        val namespaceName: String = applicationProperties.namespace.default

        if (namespaceRepository.findByName(namespaceName) == null) {
            logger.info("Create default namespace with name=$namespaceName")

            val createdNamespace: Namespace = namespaceRepository.save(
                namespaceConverter.create(namespaceName, Status.ENABLED)
            )

            indexVersionService.update(createdNamespace.id)
        } else {
            logger.debug("Default namespace with name=$namespaceName already created")
        }
    }

    suspend fun prepareNamespaceForSettings(settings: LoadedSettings): Namespace {
        return namespaceRepository.findByName(settings.namespace.name)
            ?: namespaceRepository.save(namespaceConverter.create(settings.namespace))
    }

    private companion object : KLogging()
}