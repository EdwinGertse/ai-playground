package com.tegres.ai.playground.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collection;

@JsonPropertyOrder({ "actor", "films" })
public record ActorFilms(String actor, Collection<Film> films) {
}
