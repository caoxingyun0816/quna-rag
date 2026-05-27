package com.quna.rag.springai.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiRagConfig {

    @Bean
    public MilvusServiceClient springAiMilvusClient(@Value("${milvus.host}") String host,
                                                    @Value("${milvus.port}") Integer port) {
        return new MilvusServiceClient(ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build());
    }

    @Bean
    public VectorStore springAiVectorStore(@Qualifier("springAiMilvusClient") MilvusServiceClient milvusClient,
                                           EmbeddingModel embeddingModel,
                                           @Value("${milvus.spring-ai-collection:enterprise_kb_spring_ai}") String collection,
                                           @Value("${milvus.dim:1536}") int dimensions) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName(collection)
                .embeddingDimension(dimensions)
                .indexType(IndexType.IVF_FLAT)
                .metricType(MetricType.COSINE)
                .initializeSchema(true)
                .build();
    }

    @Bean
    public ChatClient springAiRagChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public QuestionAnswerAdvisor springAiQuestionAnswerAdvisor(@Qualifier("springAiVectorStore") VectorStore vectorStore,
                                                               @Value("${rag.spring-ai.top-k:3}") int topK,
                                                               @Value("${rag.spring-ai.similarity-threshold:0.3}") double threshold) {
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("""
                        你是趣拿企业知识库助手。

                        用户问题：
                        {query}

                        可参考的知识库内容：
                        ---------------------
                        {question_answer_context}
                        ---------------------

                        回答规则：
                        1. 优先依据知识库内容回答。
                        2. 如果知识库内容无法回答，请直接说明不知道。
                        3. 不要编造不存在的信息。
                        """)
                .build();
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(promptTemplate)
                .searchRequest(SearchRequest.builder()
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .build())
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor springAiRetrievalAugmentationAdvisor(@Qualifier("springAiVectorStore") VectorStore vectorStore,
                                                                             @Value("${rag.spring-ai.top-k:3}") int topK,
                                                                             @Value("${rag.spring-ai.similarity-threshold:0.3}") double threshold,
                                                                             @Value("${rag.spring-ai.allow-empty-context:false}") boolean allowEmptyContext) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(allowEmptyContext)
                        .build())
                .build();
    }
}
