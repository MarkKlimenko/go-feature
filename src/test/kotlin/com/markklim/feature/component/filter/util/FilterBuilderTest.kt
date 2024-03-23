package com.markklim.feature.component.filter.util

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery
import org.junit.jupiter.api.Test

class FilterBuilderTest {

    @Test
    fun typeOrFilterBuilderTest() {
        val query: BooleanQuery.Builder = BooleanQuery.Builder()

        query.add(
            BooleanClause(
                BooleanQuery.Builder()
                    .add(TermQuery(Term("field1", "value")), BooleanClause.Occur.SHOULD)
                    .add(TermQuery(Term("field1", FILTER_DISABLED_VALUE)), BooleanClause.Occur.SHOULD)
                    .build(),
                BooleanClause.Occur.MUST)
        )

        query.add(
            BooleanClause(
                BooleanQuery.Builder()
                    .add(TermQuery(Term("field2", "value")), BooleanClause.Occur.SHOULD)
                    .add(TermQuery(Term("field2", FILTER_DISABLED_VALUE)), BooleanClause.Occur.SHOULD)
                    .build(),
                BooleanClause.Occur.MUST)
        )

        query.build()
    }
}