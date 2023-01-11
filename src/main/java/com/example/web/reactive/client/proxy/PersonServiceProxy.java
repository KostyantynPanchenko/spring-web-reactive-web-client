package com.example.web.reactive.client.proxy;

import com.example.web.reactive.client.model.Person;
import java.time.Duration;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonServiceProxy {

  private final WebClient webClient;

  public Flux<Person> getAll() {
    return webClient.get().uri("/person")
        .header(HttpHeaders.AUTHORIZATION, "Basic RnJvZG86QmFnZ2lucw==")
        .exchangeToFlux(personsHandler())
        .onErrorReturn(new Person(0L, "No", "Name"))
        .delayElements(Duration.ofMillis(400));
  }

  private static Function<ClientResponse, Flux<Person>> personsHandler() {
    return (ClientResponse response) -> response.bodyToFlux(Person.class);
  }
}
