package com.markklim.feature.service.index

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.markklim.feature.component.filter.FilterComponent
import com.markklim.feature.component.filter.util.FT_INDEX
import com.markklim.feature.controller.dto.feature.FeaturesFindRequest
import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.dto.status.FilterStatus
import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Feature
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.persistence.entity.IndexVersion
import com.markklim.feature.persistence.entity.Namespace
import mu.KLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import org.springframework.stereotype.Service

@Service
class IndexService(
    val objectMapper: ObjectMapper,
    val filterComponent: FilterComponent,
) {
    private val namespaceIdToIndexMapper: MutableMap<String, IndexStorage> = mutableMapOf()

    fun getFeaturesFromIndex(namespaceId: String, data: List<FeaturesFindRequest.DataItem>): List<String> {
        val storage: IndexStorage = namespaceIdToIndexMapper[namespaceId]
            ?: return emptyList()

        val parameterToDataMapper: Map<String, FeaturesFindRequest.DataItem> = data.associateBy { it.parameter }
        val query: BooleanQuery.Builder = BooleanQuery.Builder()

        storage.indexFilters.forEach {
            val value: String? = parameterToDataMapper[it.parameter]?.value

            if (it.status == FilterStatus.ENABLED || it.status == FilterStatus.DISABLED_ON_NULL && value != null) {
                val searchClause: BooleanClause = filterComponent.getFilterBuilder(it.operator)
                    .buildClause(it.column, value)
                query.add(searchClause)
            }
        }

        return storage.indexSearcher
            .search(query.build(), FEATURES_COUNT_FOR_SEARCH)
            .scoreDocs
            .map {
                storage.indexSearcher.storedFields()
                    .document(it.doc)
                    .getField(FT_INDEX)
                    .stringValue()
            }
    }

    fun createIndex(index: IndexVersion, namespace: Namespace, filters: List<Filter>, features: List<Feature>) {
        if (filters.isEmpty() || features.isEmpty()) {
            logger.debug("$LOG_PREFIX Filters or features are empty for namespace=${namespace.name}")
            return
        }

        val memoryIndex: Directory = ByteBuffersDirectory()
        val writer = IndexWriter(memoryIndex, IndexWriterConfig(StandardAnalyzer()))

        processIndexData(filters, features, writer)
        writer.close()
        saveIndexStorage(namespace, index, filters, memoryIndex)
    }

    private fun processIndexData(filters: List<Filter>, features: List<Feature>, writer: IndexWriter) {
        features.forEach { feature: Feature ->
            if (feature.status == Status.ENABLED) {
                processIndexForFeature(filters, feature, writer)
            }
        }
    }

    private fun processIndexForFeature(filters: List<Filter>, feature: Feature, writer: IndexWriter) {
        val document = Document()
        document.add(StringField(FT_INDEX, feature.name, Field.Store.YES))

        val featureFilterIdToFilterMap: Map<String, Feature.Filter> =
            objectMapper.readValue<List<Feature.Filter>>(feature.filters)
                .associateBy { it.id }

        filters.forEach { filter: Filter ->
            if (filter.status != FilterStatus.DISABLED) {
                val fieldName = "${filter.parameter}_${filter.operator}"
                val value: String? = featureFilterIdToFilterMap[filter.id]?.value

                val documentField: Field = filterComponent.getFilterBuilder(filter.operator)
                    .buildField(fieldName, value)

                document.add(documentField)
            }
        }

        writer.addDocument(document)
    }

    private fun saveIndexStorage(
        namespace: Namespace,
        index: IndexVersion,
        filters: List<Filter>,
        memoryIndex: Directory
    ) {
        namespaceIdToIndexMapper[namespace.id] = IndexStorage(
            internalIndexVersion = index.indexVersionValue,
            indexFilters = filters.map {
                IndexFilter(
                    column = "${it.parameter}_${it.operator}",
                    parameter = it.parameter,
                    operator = it.operator,
                    status = it.status
                )
            },
            indexSearcher = IndexSearcher(DirectoryReader.open(memoryIndex))
        )
    }

    fun getInternalIndexVersion(namespaceId: String): String? =
        namespaceIdToIndexMapper[namespaceId]?.internalIndexVersion

    private data class IndexStorage(
        val internalIndexVersion: String,
        val indexFilters: List<IndexFilter>,
        val indexSearcher: IndexSearcher
    )

    private data class IndexFilter(
        val column: String,
        val parameter: String,
        val operator: FilterOperator,
        val status: FilterStatus,
    )

    private companion object : KLogging() {
        const val LOG_PREFIX = "INDEX_LOADER:"

        // TODO: return more than 1000 or allow to save just 1000 records to the ns
        const val FEATURES_COUNT_FOR_SEARCH = 1000
    }
}