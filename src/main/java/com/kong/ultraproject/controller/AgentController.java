package com.kong.ultraproject.controller;

import com.kong.ultraproject.agent.CustomManus;
import com.kong.ultraproject.app.App;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/hyper_project")
public class AgentController {
    @Resource
    private App app;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 普通的对话接口（SSE流式调用）
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/chat/sync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatBySSE(String message, String chatId) {
        return app.chatBySSE(message,chatId);
    }

    /**
     * 同步调用对话接口，（RAG模式接口）
     * @param message 用户消息
     * @param chatId
     * @return
     */
    @GetMapping("/chat/rag/sync")
    public String chatWithRagBySync(String message, String chatId) {
        return app.chatWithRAG(message,chatId);
    }

    /**
     * 以SSE形式流式传输内容
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/chat/rag/sse")
    public SseEmitter chatWithRagBySSE(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L); // 设置三分钟过期
        // 获取 Flux 响应式对象，并且通过 subscribe 推送给 SseEmitter
        app.chatWithRAGBySSE(message,chatId)
                .subscribe(chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }, emitter::completeWithError,emitter::complete);
        return emitter;
    }

    /**
     * 调用 agent
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter chatWithManus(String message) {
        CustomManus manus = new CustomManus(allTools, dashscopeChatModel);
        return manus.runBySSE(message);
    }
}
