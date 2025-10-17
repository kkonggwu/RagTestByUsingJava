package com.kong.ultraproject.utils;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建自定义的上下文查询增强器，主要用于处理边界条件，进行异常处理
 */
public class CustomContextualQueryAugmenterFactory {
    public static ContextualQueryAugmenter getInstance() {
        PromptTemplate promptTemplate = new PromptTemplate("""
                你应该回答以下内容：
                你说的这些，我没有回答的能力，请你自行搜索，另寻高见吧！
                (顺便保佑我抽卡不歪)
                """);

        return ContextualQueryAugmenter.builder()
                .emptyContextPromptTemplate(promptTemplate)
                .allowEmptyContext(false)
                .build();
    }
}
