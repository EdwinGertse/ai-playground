package com.tegres.ai.playground.configuration;

import com.tegres.ai.playground.advisors.ContentSafetyAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_CLIENT;

@Configuration
public class ChatClientConfiguration {

    @Value("classpath:/prompts/system-message.st")
    private Resource systemPromptResource;

    @Bean(name = APP_CHAT_CLIENT)
    public ChatClient chatClient(ChatClient.Builder builder,
        ChatMemory chatMemory,
        OpenAiModerationModel moderationModel) {
        return builder
            .defaultSystem(systemPromptResource)
            .defaultAdvisors(
                ContentSafetyAdvisor.builder(moderationModel).build(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                SimpleLoggerAdvisor.builder().build())
            .build();
    }
}
