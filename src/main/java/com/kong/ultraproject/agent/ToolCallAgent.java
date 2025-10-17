package com.kong.ultraproject.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.kong.ultraproject.agent.AgentEnum.AgentStateEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{
    /**
     * 定义的工具
     */
    private final ToolCallback[] availableTools;

    /**
     * 保存工具调用的响应
     */
    private ChatResponse toolCallResponse;

    /**
     * 工具调用管理
     */
    private final ToolCallingManager toolCallingManager;

    /**
     * 用于禁用内置的工具调用机制，自己维护上下文
     */
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护工具调用和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 处理当前状态，并考虑下一步行动
     * @return
     */
    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);

        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    // 调用工具
                    .tools(availableTools)
                    .call()
                    .chatResponse();

            // 记录响应内容,让 AI 判断是否要调用工具，并把输出内容记录到上下文中
            this.toolCallResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            log.info("{}思考内容：{}", getName(), result);
            log.info("{}选择了 {} 个工具", getName(), toolCallList.size());

            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称： %s，参数： %s",
                            toolCall.name(),
                            toolCall.arguments())).collect(Collectors.joining("\n"));

            log.info(toolCallInfo);

            if(toolCallList.isEmpty()){
                // 不调用工具时记录助手信息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 调用工具时，不需要记录信息，调用时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error("{}的思考过程出现问题：{}", getName(), e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到了问题: " + e.getMessage())
            );
            return false;
        }
    }

    /**
     * 执行工具调用操作
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallResponse.hasToolCalls()){
            return "没有工具调用";
        }

        // 工具调用信息
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        // 执行工具调用
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallResponse);
        // 记录消息上下文，conversationHistory 包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 记录当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具 " + toolResponse.name() + "已完成,result: " + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否调用终止工具
        boolean terminateTollCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> "doTerminate".equals(toolResponse.name()));

        if (terminateTollCalled){
            // 假如调用了终止工具，则设置任务状态为 finished
            setState(AgentStateEnum.FINISHED);
        }

        log.info(results);
        return results;
    }
}

