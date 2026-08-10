package com.tegres.ai.playground.configuration;

import com.tegres.ai.playground.advisors.ContentSafetyAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_CLIENT;
import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_MEMORY;
import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_MEMORY_REPO;

@Configuration
public class ChatClientConfiguration {

    @Value("classpath:/prompts/system-message.st")
    private Resource systemPromptResource;

    @Bean(name = APP_CHAT_CLIENT)
    public ChatClient chatClient(ChatClient.Builder builder,
        @Qualifier(APP_CHAT_MEMORY) ChatMemory chatMemory,
        OpenAiModerationModel moderationModel) {
        return builder
            .defaultSystem(systemPromptResource)
            .defaultAdvisors(
                ContentSafetyAdvisor.builder(moderationModel).build(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                SimpleLoggerAdvisor.builder().build())
            .build();
    }

    @Bean(name = APP_CHAT_MEMORY)
    public ChatMemory chatMemory(@Qualifier(APP_CHAT_MEMORY_REPO) ChatMemoryRepository chatMemoryRepository,
        @Value("${chat-memory.repo.max-messages:20}") int maxMessages) {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(maxMessages)
            .build();
    }

    @ConditionalOnClass(name = "javax.sql.DataSource")
    @Bean(name = APP_CHAT_MEMORY_REPO)
    public ChatMemoryRepository chatMemoryRepository(DataSource dataSource) {
        return JdbcChatMemoryRepository.builder()
            .dataSource(dataSource)
            .dialect(JdbcChatMemoryRepositoryDialect.from(dataSource))
            .build();
    }

    @ConditionalOnMissingClass("javax.sql.DataSource")
    @Bean(name = APP_CHAT_MEMORY_REPO)
    public ChatMemoryRepository inMemoryChatRepository() {
        return new InMemoryChatMemoryRepository();
    }
}
