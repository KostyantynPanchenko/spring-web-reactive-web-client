package com.example.web.reactive.client.sandbox;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

public class ReactiveStreams {

  public static void main(String[] args) throws InterruptedException {
//    System.out.println("--- #1");
//    getStringFlux().subscribe(value -> System.out.println("RECEIVED " + value),
//        error -> System.err.println("CAUGHT " + error)
//    );
//    System.out.println();

    // following code is equivalent to try/catch block which returns hardcoded value
//    case2TryCatchEquivalent();

    // https://projectreactor.io/docs/core/release/reference/#which.errors
    // just return what already emitted
//    case3JustReturnWhatAlreadyEmitted();

    // just return what already emitted
//    case4ErrorResume();

    // wrap to business exception and rethrow
//    case5ErrorResume();

    // the same as in #5 but more straightforward
//    case6ErrorMapping();

//    case7();

//    case8HotReactiveStream();

//    case9ConnectTwoSubscribers();

//    case10AutoconnectableSubs();

//    case11ParallelFlux();

    case12FlatMap();

    case13UseFunctionsForTransformation();
  }

  private static void case2TryCatchEquivalent() {
    System.out.println("--- #2 try/catch equivalent");
    getStringFluxWithError()
        .onErrorReturn("RECOVERED VALUE")
        .subscribe(value -> System.out.println("#2 RECEIVED " + value));
    System.out.println();
  }

  private static void case3JustReturnWhatAlreadyEmitted() {
    System.out.println("--- #3 just return what already emitted");
    getStringFluxWithError()
        .onErrorComplete()
        .subscribe(value -> System.out.println("#3 RECEIVED " + value));
    System.out.println();
  }

  private static void case4ErrorResume() {
    System.out.println("--- #4 return what emitted, on error return resumed value");
    getStringFluxWithError()
        .onErrorResume(exc -> Flux.just("resumed value"))
        .subscribe(value -> System.out.println("#4 RECEIVED " + value));
    System.out.println();
  }

  private static void case5ErrorResume() {
    System.out.println("--- #5 on error return exception message");
    getStringFluxWithError()
        .onErrorResume(exc -> Flux.error(new RuntimeException(exc)))
        .subscribe(
            value -> System.out.println("#5 RECEIVED " + value),
            err -> System.out.println(err.getMessage()));
    System.out.println();
  }

  private static void case6ErrorMapping() {
    System.out.println("--- #6 the same as #5 but more straightforward");
    getStringFluxWithError()
        .onErrorMap(RuntimeException::new)
        .subscribe(
            value -> System.out.println("#6 RECEIVED " + value),
            err -> System.out.println(err.getMessage()));
    System.out.println();
  }

  private static void case7() throws InterruptedException {
    Flux<String> flux =
        Flux.interval(Duration.ofMillis(250))
            .name("Clock tick with delay of 250 ms")
            .map(input -> {
              if (input < 3) return "tick " + input;
              throw new RuntimeException("boom");
            })
            .onErrorReturn("Uh oh");

    flux.subscribe(System.out::println);
    System.out.println();
    Thread.sleep(2100);
  }

  private static void case8HotReactiveStream() {
    System.out.println("-- Hot reactive stream --");
    Sinks.Many<String> hotSource = Sinks.unsafe().many().multicast().directBestEffort();

    Flux<String> hotFlux = hotSource.asFlux().map(String::toUpperCase);

    hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: "+d));

    hotSource.emitNext("blue", FAIL_FAST);
    hotSource.tryEmitNext("green").orThrow();

    hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: "+d));

    hotSource.emitNext("orange", FAIL_FAST);
    hotSource.emitNext("purple", FAIL_FAST);
    hotSource.emitComplete(FAIL_FAST);
    System.out.println();
  }

  private static void case9ConnectTwoSubscribers() throws InterruptedException {
    System.out.println("--- #9 connect two subscribers to deferred stream");
    Flux<Integer> source = Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("All subs subscribed to source"));

    ConnectableFlux<Integer> connectableFlux = source.publish();

    connectableFlux.subscribe(System.out::println, e -> {}, () -> {});
    System.out.println("Subscribed first");
    connectableFlux.subscribe(System.out::println, e -> {}, () -> {});
    System.out.println("Subscribed second");

    Thread.sleep(800);
    System.out.println("will now connect both");

    connectableFlux.connect();
    System.out.println();
  }

  private static void case10AutoconnectableSubs() throws InterruptedException {
    System.out.println("--- #10 autoconnect two subscribers to deferred stream");
    Flux<Integer> source = Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("All subs subscribed to source"));

    Flux<Integer> autoCo = source.publish().autoConnect(2);

    autoCo.subscribe(System.out::println, e -> {}, () -> {});
    System.out.println("subscribed first");
    Thread.sleep(800);
    System.out.println("subscribing second");
    autoCo.subscribe(System.out::println, e -> {}, () -> {});
    System.out.println("Autoconnected");
    System.out.println();
  }

  private static void case11ParallelFlux() throws InterruptedException {
    System.out.println("--- #11 parallelism");
    System.out.println("Invalid parallelism:");
    Flux.range(1, 6)
        .parallel(2)
        .subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));

    System.out.println("Valid parallelism:");
    Flux.range(1, 6)
        .parallel(2)
        .runOn(Schedulers.parallel())
        .subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));

    Thread.sleep(500);

    System.out.println();
  }

  private static void case12FlatMap() throws InterruptedException {
    System.out.println("-- #12 print 'Hello World!' letter by letter");
    Flux.fromIterable(List.of("Hello", " ", "World", "!"))
        .concatWithValues("\n")
        .flatMap(word -> Flux.fromStream(word.chars().boxed()))
        .map(Character::toString)
        .delayElements(Duration.ofMillis(60))
        .subscribe(System.out::print);
    Thread.sleep(1000);
  }

  private static void case13UseFunctionsForTransformation() {
    Flux.fromStream(Stream.of("this", "is", "hello", "world"))
        .startWith("\n")
        .concatWithValues("\n")
        .transform(myFunc())
        .map(item -> {
          if (item.equals("WORLD ")) {
            return "WORLD";
          }
          if (item.equals("\n ")) {
            return "\n";
          }
          return item;
        })
        .doOnComplete(() -> System.out.println("Done processing strings"))
        .subscribe(System.out::print);
  }

  private static Function<Flux<String>, Flux<String>> myFunc() {
    return inputFlux -> inputFlux.map(item -> item + " ").map(String::toUpperCase);
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
