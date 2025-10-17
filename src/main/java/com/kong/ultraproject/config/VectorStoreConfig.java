package com.kong.ultraproject.config;

import com.kong.ultraproject.rag.RagDocumentReader;
import com.kong.ultraproject.utils.CustomKeywordEnricher;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量存储器的配置；基于内存存储的向量数据库
 */
@Configuration
public class VectorStoreConfig {

    @Resource
    private RagDocumentReader ragDocumentReader;

    @Resource
    private CustomKeywordEnricher customKeywordEnricher;


    @Bean
    VectorStore doVectorStore(EmbeddingModel embeddingModel) {
        // 构建向量存储器，使用embeddingModel
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 得到切分后的文档
        List<Document> documents = ragDocumentReader.loadMarkdown();
        // 补充元信息
        List<Document> enrichedDocuments = customKeywordEnricher.enrichDocuments(documents);
        // 加入向量数据库
        simpleVectorStore.add(enrichedDocuments);

        return simpleVectorStore;
    }


}
