package com.kong.ultraproject.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct 模式的抽象代理类，用于实现 think - act 的运行模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并决定下一步的操作
     * @return 是否需要执行，true 为执行， false 为不执行
     */
    public abstract boolean think();

    /**
     * 执行
     * @return 返回执行结果
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成，无需下一步行动";
            }
            return act();

        } catch (Exception e) {
            e.printStackTrace();
            return "step 执行失败： "+e.getMessage();
        }
    }
}
