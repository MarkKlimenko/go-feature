package com.go.feature.service.index

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.persistence.entity.Namespace
import mu.KLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import org.springframework.stereotype.Service

@Service
class IndexService(
    val objectMapper: ObjectMapper
) {
    private val namespaceIdToIndexMapper: MutableMap<String, IndexStorage> = mutableMapOf()

    fun createIndex(index: IndexVersion, namespace: Namespace, filters: List<Filter>, features: List<Feature>) {
        if (filters.isEmpty() || features.isEmpty()) {
            // TODO: debug
            logger.info("${LOG_PREFIX} Filters or features are empty for namespace=${namespace.name}")
            return
        }

        val memoryIndex: Directory = ByteBuffersDirectory()
        val writer = IndexWriter(memoryIndex, IndexWriterConfig(StandardAnalyzer()))

        features.forEach { feature: Feature ->
            val document = Document()
            document.add(StringField("ft", feature.name, Field.Store.YES))


            val featureFilterIdToFilterMap: Map<String, Feature.Filter> =
                objectMapper.readValue<List<Feature.Filter>>(feature.filters)
                    .associateBy { it.id }

            filters.forEach { filter: Filter ->
                val value: String? = featureFilterIdToFilterMap[filter.id]?.value

                //TODO: refactor
                if (filter.operator == Filter.Operator.EQ) {
                    if(value != null) {
                        document.add(StringField("${filter.parameter}_${filter.operator}", value, Field.Store.NO))
                    } else {
                        document.add(StringField("${filter.parameter}_${filter.operator}", FILTER_DISABLED_VALUE, Field.Store.NO))
                    }
                } else if (filter.operator == Filter.Operator.LIST_EQ) {
                    if(value != null) {
                        document.add(TextField("${filter.parameter}_${filter.operator}", value, Field.Store.NO))
                    } else {
                        document.add(TextField("${filter.parameter}_${filter.operator}", FILTER_DISABLED_VALUE, Field.Store.NO))
                    }
                } else if (filter.operator == Filter.Operator.MORE) {
                    if(value != null) {
                        val longValue: Long = try {
                            value.toLong()
                        } catch (e: NumberFormatException) {
                            throw IllegalArgumentException("Value is not compatible with filter type; filter=${filter.name}, value=${value}")
                        }

                        document.add(LongField("${filter.parameter}_${filter.operator}", longValue))
                    } else {
                        document.add(LongField("${filter.parameter}_${filter.operator}", FILTER_MORE_DISABLED_VALUE))
                    }
                } else {
                    throw IllegalArgumentException("logic is not implemented for filter.operator=${filter.operator}")
                }
            }

            writer.addDocument(document)
        }

        writer.close()

        namespaceIdToIndexMapper[namespace.id] = IndexStorage(index.indexVersionValue, memoryIndex)
    }

    fun getInternalIndexVersion(namespaceId: String): String? {
        return namespaceIdToIndexMapper[namespaceId]?.internalIndexVersion
    }

    private data class IndexStorage(
        val internalIndexVersion: String,
        val memoryIndex: Directory
    )

    private companion object : KLogging() {
        const val LOG_PREFIX = "INDEX_LOADER:"

        const val FILTER_DISABLED_VALUE = "disfft"
        const val FILTER_MORE_DISABLED_VALUE = Long.MAX_VALUE
    }
}