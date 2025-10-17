package com.kong.ultraproject.utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * prompt 重写器，调用 AI 用于优化用户上传 prompt 优化检索精确度，提升输出质量
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;


    public QueryRewriter(ChatModel dashScopeModel) {
        ChatClient.Builder builder = ChatClient.builder(dashScopeModel);
        // 创建查询重写转换器
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 查询重写操作
     * @param prompt
     * @return
     */
    public String rewrite(String prompt) {
        Query query = new Query(prompt);
        Query transformedQuery = queryTransformer.transform(query);
        return transformedQuery.text();
    }
}
