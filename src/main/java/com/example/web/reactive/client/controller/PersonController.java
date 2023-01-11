package com.example.web.reactive.client.controller;

import com.example.web.reactive.client.model.Person;
import com.example.web.reactive.client.proxy.PersonServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class PersonController {

  private final PersonServiceProxy proxy;

  @GetMapping(value = "/person", produces = "application/stream+json")
  public Flux<Person> getAll() {
    return proxy.getAll();
  }
}
