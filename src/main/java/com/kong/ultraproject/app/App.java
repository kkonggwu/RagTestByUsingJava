package com.kong.ultraproject.app;

import com.kong.ultraproject.advisor.CustomAdvisor;
import com.kong.ultraproject.chatmemory.FileBasedChatMemory;
import com.kong.ultraproject.utils.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class App {


    private final ChatClient chatClient;

    // todo 写系统提示词，告知AI具体应用背景，根据选题来写
    private static final String SYSTEM_PROMPT = "你是一个非常useful的咨询专家";

    @Resource
    private VectorStore vectorStore;

    @Resource
    private VectorStore pgVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;


    /**
     * AI客户端
     * @param dashscopeChatModel
     */
    public App(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆，对应chatmemory.FileBaseChatMemory
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);

//        // InMemory 基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor
                        new CustomAdvisor()
                )
                .build();
    }

    /**
     * 支持多轮记忆的基础对话
     * @param message
     * @param chatId
     * @return
     */
    public String chat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                .call().chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("chat response: {}", content);
        return content;
    }

    /**
     * SSE 形式调用
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> chatBySSE(String message, String chatId) {
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                .stream().content();
        return content;
    }

    record ChatReport(String title, List<String> suggestions) { }

    /**
     * 支持结构化输出
     *
     * @param message
     * @param chatId
     * @return
     */
    public ChatReport chatWithReport(String message, String chatId) {
        ChatReport chatReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都生成专业性的总结，标题为{用户}的咨询报告，内容为列表形式")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                .call()
                .entity(ChatReport.class);

        log.info("chat response: {}", chatReport);
        return chatReport;
    }

    /**
     * 带RAG功能的查询接口
     * @param message
     * @param chatId
     * @return
     */
    public String chatWithRAG(String message, String chatId){
        // 查询重写
        String rewrittenMessage = queryRewriter.rewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                // 使用RAG增强检索
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                // 基于 PgVector 向量存储
//                .advisors(new QuestionAnswerAdvisor(pgVectorStore))
                // 应用（文档查询器 + 上下文增强器）
//                .advisors(CustomRagAdvisorFactory.buildCustomRagAdvisor(vectorStore,"自定义标签"))
//                .advisors(new CustomAdvisor())
                .call().chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("chat response: {}", content);
        return content;
    }

    /**
     * 带RAG功能的查询接口,SSE形式返回
     * @param message
     * @param chatId
     * @return 返回响应式的文本内容
     */
    public Flux<String> chatWithRAGBySSE(String message, String chatId){
        // 查询重写
        String rewrittenMessage = queryRewriter.rewrite(message);
        // Flux 响应式对象
        return chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                // 使用RAG增强检索
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                // 基于 PgVector 向量存储
//                .advisors(new QuestionAnswerAdvisor(pgVectorStore))
                // 应用（文档查询器 + 上下文增强器）
//                .advisors(CustomRagAdvisorFactory.buildCustomRagAdvisor(vectorStore,"自定义标签"))
//                .advisors(new CustomAdvisor())
                .stream().content();
    }

    public String chatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    public String chatWithMCP(String message, String chatId){
        // 查询重写
        String rewrittenMessage = queryRewriter.rewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                // 使用RAG增强检索
//                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .advisors(new CustomAdvisor())
                .tools(toolCallbackProvider)
                .call().chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("chat response: {}", content);
        return content;
    }
}
