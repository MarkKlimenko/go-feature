package com.go.feature.service

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespaceStatus
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.converter.NamespaceConverter
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.NamespaceRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NamespaceService(
    val namespaceRepository: NamespaceRepository,
    val namespaceConverter: NamespaceConverter,
    val versionService: IndexVersionService,
    val applicationProperties: ApplicationProperties,
) {

    suspend fun getNamespaces(): NamespacesResponse {
        val namespaces: List<NamespaceResponse> = namespaceRepository.findAll()
            .map { namespaceConverter.convert(it) }
            .toList()

        return NamespacesResponse(
            data = namespaces
        )
    }

    //TODO: check Transactional
    @Transactional(rollbackFor = [Exception::class])
    suspend fun createNamespace(request: NamespaceCreateRequest): NamespaceResponse {
        val createdNamespace: Namespace = namespaceRepository.save(namespaceConverter.create(request))

        versionService.updateIndex(createdNamespace.id)

        return namespaceConverter.convert(createdNamespace)
    }

    //TODO: check Transactional
    @Transactional(rollbackFor = [Exception::class])
    suspend fun editNamespace(id: String, request: NamespaceEditRequest): NamespaceResponse {
        val requiredNamespace: Namespace = namespaceRepository.findById(id)
            ?: throw IllegalArgumentException("Namespace not found")

        val editedNamespace: Namespace = namespaceRepository.save(namespaceConverter.edit(requiredNamespace, request))

        versionService.updateIndex(id)

        return namespaceConverter.convert(editedNamespace)
    }

    suspend fun createDefaultNamespace() {
        if (namespaceRepository.count() == 0L) {
            val defaultNamespace: String = applicationProperties.namespace.default

            logger.info("Create default namespace with name=${defaultNamespace}")

            createNamespace(
                NamespaceCreateRequest(
                    name = defaultNamespace,
                    status = NamespaceStatus.ENABLED
                )
            )
        }
    }

    private companion object : KLogging()
}