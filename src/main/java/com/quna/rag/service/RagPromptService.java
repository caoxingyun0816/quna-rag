package com.quna.rag.service;

import com.quna.rag.dto.response.RagSearchHitResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG Prompt 构造服务。
 */
@Service
public class RagPromptService {
    public String build(String question, List<RagSearchHitResponse> hits) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            RagSearchHitResponse hit = hits.get(i);
            context.append("资料").append(i + 1).append('\n')
                    .append("文档：").append(hit.getDocName()).append('\n')
                    .append("标题：").append(hit.getTitle()).append('\n')
                    .append(hit.getContent()).append("\n\n");
        }
        return """
                你是企业内部研发助手，请基于给定知识库内容回答问题。

                要求：
                1. 只基于上下文回答，不要编造。
                2. 如果上下文不足，明确说“知识库中未找到足够信息”。
                3. 回答要面向 Java 后端研发人员。
                4. 涉及接口时，输出路由、参数、响应、注意事项。
                5. 涉及代码时，输出类名、方法名、调用链、表、Redis、MQ。
                6. 最后列出引用来源。

                用户问题：
                %s

                知识库上下文：
                %s
                """.formatted(question, context);
    }
}
