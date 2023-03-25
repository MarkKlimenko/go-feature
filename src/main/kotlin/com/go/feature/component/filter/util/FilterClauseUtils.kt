package com.go.feature.component.filter.util

import org.apache.lucene.document.LongField
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery

fun composeStringStrictEqFilter(field: String, value: String): BooleanClause {
    return BooleanClause(
        TermQuery(Term(field, value)),
        BooleanClause.Occur.MUST
    )
}

fun composeStringEqFilter(field: String, value: String?): BooleanClause {
    return if (value == null) {
        BooleanClause(
            TermQuery(Term(field, FILTER_DISABLED_VALUE)),
            BooleanClause.Occur.MUST
        )
    } else {
        BooleanClause(
            BooleanQuery.Builder()
                .add(TermQuery(Term(field, value)), BooleanClause.Occur.SHOULD)
                .add(TermQuery(Term(field, FILTER_DISABLED_VALUE)), BooleanClause.Occur.SHOULD)
                .build(),
            BooleanClause.Occur.MUST
        )
    }
}

fun composeNumberMoreFilter(field: String, value: Long?): BooleanClause {
    return if (value == null) {
        BooleanClause(
            LongField.newExactQuery(field, Long.MAX_VALUE),
            BooleanClause.Occur.MUST
        )
    } else {
        BooleanClause(
            BooleanQuery.Builder()
                .add(LongField.newRangeQuery(field, Long.MIN_VALUE, value), BooleanClause.Occur.SHOULD)
                .add(LongField.newExactQuery(field, Long.MAX_VALUE), BooleanClause.Occur.SHOULD)
                .build(),
            BooleanClause.Occur.MUST
        )
    }
}

fun composeNumberLessFilter(field: String, value: Long?): BooleanClause {
    return if (value == null) {
        BooleanClause(
            LongField.newExactQuery(field, Long.MIN_VALUE),
            BooleanClause.Occur.MUST
        )
    } else {
        BooleanClause(
            BooleanQuery.Builder()
                .add(LongField.newRangeQuery(field, value, Long.MAX_VALUE), BooleanClause.Occur.SHOULD)
                .add(LongField.newExactQuery(field, Long.MIN_VALUE), BooleanClause.Occur.SHOULD)
                .build(),
            BooleanClause.Occur.MUST
        )
    }
}