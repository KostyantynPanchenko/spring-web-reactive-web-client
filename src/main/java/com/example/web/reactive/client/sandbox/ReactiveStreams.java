package com.example.web.reactive.client.sandbox;

import java.time.Duration;
import reactor.core.publisher.Flux;

public class ReactiveStreams {

  public static void main(String[] args) throws InterruptedException {
    System.out.println("--- #1");
    Flux<String> stringFlux = getStringFlux();
    stringFlux.subscribe(value -> System.out.println("RECEIVED " + value),
        error -> System.err.println("CAUGHT " + error)
    );

    // following code is equivalent to try/catch block which returns hardcoded value
    System.out.println("--- #2");
    stringFlux = getStringFluxWithError();
    stringFlux
        .onErrorReturn("RECOVERED VALUE")
        .subscribe(value -> System.out.println("#2 RECEIVED " + value));

    // just return what already emitted
    System.out.println("--- #3");
    stringFlux = getStringFluxWithError();
    stringFlux.onErrorComplete()
        .subscribe(value -> System.out.println("#3 RECEIVED " + value));;

    // just return what already emitted
    System.out.println("--- #4");
    stringFlux = getStringFluxWithError();
    stringFlux.onErrorResume(exc -> Flux.just("resumed value"))
        .subscribe(value -> System.out.println("#4 RECEIVED " + value));

    // wrap to business exception and rethrow
    System.out.println("--- #5");
    stringFlux = getStringFluxWithError();
    stringFlux.onErrorResume(exc -> Flux.error(new RuntimeException(exc)))
        .subscribe(value -> System.out.println("#5 RECEIVED " + value), err -> System.out.println(err.getMessage()));

    // the same as in #5 but more straightforward
    System.out.println("--- #6");
    stringFlux = getStringFluxWithError();
    stringFlux.onErrorMap(RuntimeException::new)
        .subscribe(value -> System.out.println("#6 RECEIVED " + value), err -> System.out.println(err.getMessage()));



    Flux<String> flux =
        Flux.interval(Duration.ofMillis(250))
            .map(input -> {
              if (input < 3) return "tick " + input;
              throw new RuntimeException("boom");
            })
            .onErrorReturn("Uh oh");

    flux.subscribe(System.out::println);
    Thread.sleep(2100);
  }

  private static Flux<String> getStringFlux() {
    return Flux.range(1, 5)
        .map(v -> {
          System.out.println("Doin' smth dangerous");
          return v * 2;
        })
        .map(v -> {
          System.out.println("Doin' smth dangerous 2");
          return v.toString();
        });
  }

  private static Flux<String> getStringFluxWithError() {
    return Flux.range(1, 5)
        .map(v -> {
          System.out.println("Doin' smth deangerous");
          return v * 2;
        })
        .map(v -> {
          if (v == 6) {
            throw new IllegalArgumentException("Got three!");
          }
          System.out.println("Doin' smth deangerous 2");
          return v.toString();
        });
  }

}
