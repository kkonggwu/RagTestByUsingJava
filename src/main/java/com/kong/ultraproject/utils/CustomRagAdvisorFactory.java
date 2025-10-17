package com.kong.ultraproject.utils;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问的工厂
 */
public class CustomRagAdvisorFactory {

    /**
     *
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor buildCustomRagAdvisor(VectorStore vectorStore, String status) {
        // 过滤特定状态文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) // 过滤条件
                .similarityThreshold(0.5) // 相似度阈值
                .topK(3) // 返回 TOP 3 的文档
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(CustomContextualQueryAugmenterFactory.getInstance())
                .build();
    }
}
