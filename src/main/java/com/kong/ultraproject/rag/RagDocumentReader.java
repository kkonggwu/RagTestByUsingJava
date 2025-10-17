package com.kong.ultraproject.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RagDocumentReader {

    private final ResourcePatternResolver resourcePatternResolver;

    RagDocumentReader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * Markdown文档加载
     * @return
     */
    public List<Document> loadMarkdown() {
        ArrayList<Document> documents = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .build();

                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                documents.addAll(markdownDocumentReader.read());
            }
        } catch (IOException e) {
            log.error("markdown read error", e);
        }
        return documents;
    }
}
