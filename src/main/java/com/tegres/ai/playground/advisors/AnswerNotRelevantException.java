package com.tegres.ai.playground.advisors;

public class AnswerNotRelevantException extends RuntimeException {

    public AnswerNotRelevantException(String question, String answer) {
        super("The answer '" + answer + "' is not relevant to the question '" + question + "'.");
    }
}
