package com.tegres.ai.playground.controller;

import com.tegres.ai.playground.advisors.AnswerNotRelevantException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.tegres.ai.playground.common.ComponentConstants.APP_CHAT_CLIENT;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient.Builder chatClient;

    @Value("classpath:/prompts/default.st")
    private Resource actorSearchPromptResource;

    @Value("${app.number-of-highest-rated-films:}")
    private int numberOfHighestRatedFilms;

    public ChatController(@Qualifier(APP_CHAT_CLIENT) ChatClient.Builder chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping
    public String chat() throws IOException {
        log.info("Request: {}",
            actorSearchPromptResource.getContentAsString(Charset.defaultCharset()));
        PromptTemplate prompt = new PromptTemplate(actorSearchPromptResource);
        var actorFilmsResponse = this.chatClient.build().prompt(prompt.create(
                Map.of("numberOfHighestRatedFilms", numberOfHighestRatedFilms, "actorFullName",
                    "Sandra Bullock")))
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.DEFAULT_CONVERSATION_ID, "Edwin"))
            .call().chatResponse();

        EvaluationRequest evaluationRequest = new EvaluationRequest(prompt.getTemplate(),
            actorFilmsResponse.getResult().getOutput().getText());

        RelevancyEvaluator evaluator = new RelevancyEvaluator(chatClient);

        EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);
        if (!evaluationResponse.isPass()) {
            throw new AnswerNotRelevantException(prompt.getTemplate(),
                actorFilmsResponse.getResult().getOutput().getText());
        }
        return actorFilmsResponse.getResult().getOutput().getText();
    }
}
