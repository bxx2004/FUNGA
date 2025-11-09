package cn.revoist.lifephoton.module.funga.ai.rag

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.ContentMetadata
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import java.lang.Exception
import java.util.Map
import java.util.function.Function
import java.util.stream.Collectors

/**
 * @author 6hisea
 * @date  2025/7/26 19:12
 * @description: None
 */
class MultiEmbeddingStoreContentRetriever(
    val embeddingModel: EmbeddingModel,
) : ContentRetriever {
    val DEFAULT_MAX_RESULTS: Function<Query, Int> = Function { query: Query -> 3 }
    val DEFAULT_MIN_SCORE: Function<Query, Double> = Function { query: Query -> 0.8 }

    override fun retrieve(query: Query): List<Content> {
        val embeddedQuery = embeddingModel.embed(query.text()).content()

        val searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(embeddedQuery)
            .maxResults(20)
            .minScore(DEFAULT_MIN_SCORE.apply(query))
            .build()

        val res =  EmbeddingStores.getAllDb().map {
            val searchResult = it.search(searchRequest)
            searchResult.matches().stream()
                .map<Content> { embeddingMatch: EmbeddingMatch<TextSegment> ->
                    Content.from(
                        embeddingMatch.embedded(),
                        Map.of<ContentMetadata, Any>(
                            ContentMetadata.SCORE, embeddingMatch.score(),
                            ContentMetadata.EMBEDDING_ID, embeddingMatch.embeddingId()
                        )
                    )
                }
                .toList()
        }.flatten().sortedByDescending {
            it.metadata().getValue(ContentMetadata.SCORE).toString().toDouble()
        }.filter {
            it.metadata().getValue(ContentMetadata.SCORE).toString().toDouble() >= 0.8
        }
        return try {
            res.subList(0,DEFAULT_MAX_RESULTS.apply(query))
        }catch (e: Exception){
            res
        }
    }
}