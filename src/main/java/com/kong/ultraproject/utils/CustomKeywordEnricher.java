package com.kong.ultraproject.utils;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 元信息增强器（调用AI来实现）
 */
@Component
public class CustomKeywordEnricher {
    @Resource
    private ChatModel dashscopeChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.dashscopeChatModel, 5);
        return enricher.apply(documents);
    }
}



