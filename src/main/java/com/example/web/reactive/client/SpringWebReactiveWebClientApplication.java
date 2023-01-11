package com.example.web.reactive.client;

import com.example.web.reactive.client.model.Person;
import com.example.web.reactive.client.proxy.PersonServiceProxy;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class SpringWebReactiveWebClientApplication {

  public static void main(final String[] args) {
    SpringApplication.run(SpringWebReactiveWebClientApplication.class, args);
  }

  @Bean
  public WebClient webClient(final WebClient.Builder builder) {
    return builder
        //for Docker
        //.baseUrl("http://host.docker.internal:8080")
        .baseUrl("http://localhost:8080")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean
  public CommandLineRunner commandLineRunner(
      final WebClient webClient, final PersonServiceProxy proxy) {
    return (String[] args) -> {
      proxy.getAll().subscribe(System.out::println);

      waitForSomeTime();

      log.info("Updating Jordan...");
      updateJordan(webClient);
      log.info("Jordan updated.");

      waitForSomeTime();

      proxy.getAll().subscribe(System.out::println);
    };
  }

  private static void waitForSomeTime() {
    try {
      Thread.sleep(1500);
    } catch (final InterruptedException exc) {
      log.error("Shit happens...");
    }
  }

  private void updateJordan(final WebClient webClient) {
    final var updated = webClient.put()
        .uri("/person")
        .header(HttpHeaders.AUTHORIZATION, "Basic RnJvZG86QmFnZ2lucw==")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(new Person(1L, "Michael Jeffrey", "Jordan"))
        .exchangeToMono(personHandler())
        .subscribe();

    log.info("Updated {}", updated);
  }

  private static Function<ClientResponse, Flux<Person>> personsHandler() {
    return (ClientResponse response) -> response.bodyToFlux(Person.class);
  }

  private static Function<ClientResponse, Mono<Person>> personHandler() {
    return (ClientResponse response) -> {
        if (response.statusCode().equals(HttpStatus.OK)) {
          return response.bodyToMono(Person.class);
        } else if (response.statusCode().is4xxClientError()) {
          return Mono.just(new Person(0L, "not found", "not found"));
        }
        return response.createException().flatMap(Mono::error);
    };
  }
}
