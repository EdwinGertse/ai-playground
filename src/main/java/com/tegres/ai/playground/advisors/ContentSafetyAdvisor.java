package com.tegres.ai.playground.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationMessage;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.OpenAiModerationModel;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContentSafetyAdvisor implements CallAdvisor, StreamAdvisor {

    private static final int DEFAULT_ORDER = 0;
    private static final String DEFAULT_VIOLATION_MESSAGE = "No category violations detected";

    private final OpenAiModerationModel openAiModerationModel;
    private final int order;

    public ContentSafetyAdvisor(OpenAiModerationModel openAiModerationModel) {
        this(openAiModerationModel, DEFAULT_ORDER);
    }

    public ContentSafetyAdvisor(OpenAiModerationModel openAiModerationModel, int order) {
        this.openAiModerationModel = openAiModerationModel;
        this.order = order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Moderation response = this.moderate(request.prompt().getContents());
        final Optional<String> moderationResponse = response.getResults().stream()
            .map(this::constructModerationResult)
            .map(ModerationMessage::getText)
            .findFirst();
        if (moderationResponse.isPresent() && moderationResponse.get().equals(DEFAULT_VIOLATION_MESSAGE)) {
            return chain.nextCall(request);
        } else {
            return this.failureResponse(request, moderationResponse.get());
        }
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        Moderation response = this.moderate(request.prompt().getContents());
        final Optional<String> moderationResponse = response.getResults().stream()
            .map(this::constructModerationResult)
            .map(ModerationMessage::getText)
            .findFirst();
        if (moderationResponse.isPresent() && moderationResponse.get().equals(DEFAULT_VIOLATION_MESSAGE)) {
            return chain.nextStream(request);
        } else {
            return Flux.just(this.failureResponse(request, moderationResponse.get()));
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public static ContentSafetyAdvisor.Builder builder(OpenAiModerationModel openAiModerationModel) {
        return new ContentSafetyAdvisor.Builder(openAiModerationModel);
    }

    private Moderation moderate(String prompt) {
        var moderationRequest = new ModerationPrompt(prompt);
        var response = openAiModerationModel.call(moderationRequest);
        return response.getResult().getOutput();
    }

    private ChatClientResponse failureResponse(ChatClientRequest chatClientRequest, String failureResponse) {
        return ChatClientResponse.builder()
            .chatResponse(ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(failureResponse))))
                .build())
            .context(Map.copyOf(chatClientRequest.context()))
            .build();
    }

    private ModerationMessage constructModerationResult(ModerationResult moderationResult) {
        Categories categories = moderationResult.getCategories();
        String violations = Stream.of(
                Map.entry("Sexual", categories.isSexual()),
                Map.entry("Hate", categories.isHate()),
                Map.entry("Harassment", categories.isHarassment()),
                Map.entry("Self-Harm", categories.isSelfHarm()),
                Map.entry("Sexual/Minors", categories.isSexualMinors()),
                Map.entry("Hate/Threatening", categories.isHateThreatening()),
                Map.entry("Violence/Graphic", categories.isViolenceGraphic()),
                Map.entry("Self-Harm/Intent", categories.isSelfHarmIntent()),
                Map.entry("Self-Harm/Instructions", categories.isSelfHarmInstructions()),
                Map.entry("Harassment/Threatening", categories.isHarassmentThreatening()),
                Map.entry("Violence", categories.isViolence()))
            .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(", "));

        return new ModerationMessage(violations.isEmpty()
            ? DEFAULT_VIOLATION_MESSAGE
            : "Violated categories: " + violations);
    }

    public static final class Builder {
        private OpenAiModerationModel openAiModerationModel;
        private int order = DEFAULT_ORDER;

        private Builder(OpenAiModerationModel openAiModerationModel) {
            this.openAiModerationModel = openAiModerationModel;
        }

        public ContentSafetyAdvisor.Builder order(int order) {
            this.order = order;
            return this;
        }

        public ContentSafetyAdvisor build() {
            return new ContentSafetyAdvisor(this.openAiModerationModel, order);
        }
    }

}
