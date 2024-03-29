package com.markklim.feature.service.namespace

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.controller.dto.namespace.NamespaceCreateRequest
import com.markklim.feature.controller.dto.namespace.NamespaceEditRequest
import com.markklim.feature.controller.dto.namespace.NamespaceResponse
import com.markklim.feature.controller.dto.namespace.NamespacesResponse
import com.markklim.feature.converter.NamespaceConverter
import com.markklim.feature.dto.settings.loader.LoadedSettings
import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Namespace
import com.markklim.feature.persistence.repository.NamespaceRepository
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.util.checkStorageForUpdateAction
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.NAMESPACE_NOT_FOUND_ERROR
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
            ?: throw ClientException(NAMESPACE_NOT_FOUND_ERROR)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun createNamespace(request: NamespaceCreateRequest): NamespaceResponse {
        checkStorageForUpdateAction(applicationProperties)

        namespaceRepository.findByName(request.name)
            ?.let { throw ClientException("Namespace already exists") }

        val createdNamespace: Namespace = namespaceRepository.save(namespaceConverter.create(request))

        indexVersionService.update(createdNamespace.id)

        return namespaceConverter.convert(createdNamespace)
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun editNamespace(id: String, request: NamespaceEditRequest): NamespaceResponse {
        checkStorageForUpdateAction(applicationProperties)

        val requiredNamespace: Namespace = namespaceRepository.findById(id)
            ?: throw ClientException(NAMESPACE_NOT_FOUND_ERROR)

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