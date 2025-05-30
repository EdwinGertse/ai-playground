package com.tegres.ai.playground.controller;

import com.tegres.ai.playground.domain.ActorFilms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_CLIENT;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;

    @Value("${app.number-of-highest-rated-films:}")
    private int numberOfHighestRatedFilms;

    public ChatController(@Qualifier(APP_CHAT_CLIENT) ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping
    public String chat() {
        PromptTemplate prompt = new PromptTemplate("""
                Generate the top {numberOfHighestRatedFilms} filmography for the actor or actress {actorFullName}
                """);
        var actorFilmsResponse = this.chatClient
            .prompt(prompt.create(Map.of("numberOfHighestRatedFilms", numberOfHighestRatedFilms,
                "actorFullName", "Sandra Bullock")))
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.DEFAULT_CONVERSATION_ID, "Edwin"))
            .call()
            .entity(ActorFilms.class);

        var response = String.format("%s -> %s",
            actorFilmsResponse.actor(),
            actorFilmsResponse.films()
                .stream()
                .collect(Collectors.toList()));
        log.debug(response);

        return response;
    }
}
