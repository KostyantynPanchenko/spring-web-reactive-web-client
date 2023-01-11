package com.example.web.reactive.client.controller;

import com.example.web.reactive.client.model.Person;
import com.example.web.reactive.client.proxy.PersonServiceProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PersonController {

  private final PersonServiceProxy proxy;

  @GetMapping(value = "/person", produces = "application/stream+json")
  public Flux<Person> getAll() {
    log.info("Got a new request. Processing...");
    return proxy.getAll()
        .onErrorResume(
            WebClientRequestException.class,
            exception -> Flux.just(new Person(0L, "No", "Name")));
  }
}
