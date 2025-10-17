package com.kong.ultraproject.agent;

import cn.hutool.core.util.StrUtil;
import com.kong.ultraproject.agent.AgentEnum.AgentStateEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 *
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    /**
     * 系统 prompt
      */
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentStateEnum state = AgentStateEnum.IDLE;

    /**
     * 执行控制，最大10步
     */
    private int maxSteps = 10;
    private int currentStep = 0;

    // 大模型
    private ChatClient chatClient;

    // Memory（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentStateEnum.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 更改状态
        state = AgentStateEnum.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentStateEnum.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentStateEnum.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentStateEnum.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    public SseEmitter runBySSE(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L); // 五分钟超时
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentStateEnum.IDLE) {
                    sseEmitter.send("Cannot run agent from state: " + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Cannot run agent with empty user prompt");
                    sseEmitter.complete();
                    return;
                }
            }catch (Exception e) {
                sseEmitter.completeWithError(e);
            }

            // 更改状态
            state = AgentStateEnum.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                for (int i = 0; i < maxSteps && state != AgentStateEnum.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);
                    // 单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    sseEmitter.send(result);
                }
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentStateEnum.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                }
                // 正常结束
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentStateEnum.ERROR;
                log.error("Error executing agent", e);
                try {
                    sseEmitter.send("Error executing agent：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 清理资源
                this.cleanup();
            }
        });
        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentStateEnum.ERROR;
            this.cleanup();
            log.warn("SSE Timeout");
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentStateEnum.RUNNING) {
                this.state = AgentStateEnum.FINISHED;
            }
            this.cleanup();
            log.info("SSE completed");
        });
        return sseEmitter;
    }

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 清理资源
    }
}
