package com.tegres.ai.playground.advisors;

public class AnswerNotRelevantException extends RuntimeException {

    public AnswerNotRelevantException(String question, String answer) {
        super(String.format("The answer `%s` is not relevant to the question `%s`", answer, question));
    }
}
