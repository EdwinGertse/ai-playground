package com.tegres.ai.playground.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_CLIENT;

@Configuration
public class ChatClientConfiguration {

    @Bean(name = APP_CHAT_CLIENT)
    public ChatClient chatClient(ChatClient.Builder builder,
        ChatMemory chatMemory) {
        return builder
            .defaultAdvisors(
                SafeGuardAdvisor.builder().sensitiveWords(List.of("bad")).build(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                SimpleLoggerAdvisor.builder().build())
            .build();
    }
}
