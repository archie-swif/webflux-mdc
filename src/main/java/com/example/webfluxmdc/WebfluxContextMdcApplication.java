package com.example.webfluxmdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class WebfluxContextMdcApplication {
    private static Logger log = LoggerFactory.getLogger(WebfluxContextMdcApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebfluxContextMdcApplication.class, args);
    }


    @Bean
    public RouterFunction<ServerResponse> router() {

        return RouterFunctions
                .route(GET("/test"), serverRequest -> {
                    log.info("start");

                    Flux<String> flux = Flux.just("A", "B", "C", "D")

                            //Most common cases where logging can be done
                            .doOnNext(s -> log.info("doOnNext for {}", s))

                            .map(s -> {
                                log.info("map for {}", s);
                                return s;
                            })

                            .flatMap(s ->
                            {
                                log.info("flatMap for {}", s);
                                return Mono.subscriberContext().map(c -> {
                                    log.info("subscriberContext for {}", s);
                                    return s + c.getOrDefault("context", "no_data");
                                });
                            })

                            // Few ways to generate an error
                            .doOnNext(s -> {
                                log.info("doOnNext for {}", s);
                                if (s.startsWith("B")) {
                                    log.info("throwing error for {}", s);
                                    throw new RuntimeException("ERROR ON B");
                                }
                            })

//                            .flatMap(s -> {
//                                log.info("flatMap for {}", s);
//                                if (s.startsWith("B")) {
//                                    return Mono.error(new RuntimeException("ERROR ON B"));
//                                }
//
//                                return Mono.just(s);
//                            })


                            // Different ways to handle error
                            .doOnError(e -> log.error("do on error"))

//                            .onErrorResume(err -> Flux.just("F", "G"))
                            .onErrorContinue((throwable, o) -> log.error("ERR on {}", o))
//                            .onErrorReturn("E")

                            // Defining the reactive context
                            .subscriberContext(Context.of("context", "" + System.currentTimeMillis()));

                    log.info("end");

                    return ServerResponse
                            .ok()
                            .body(flux, String.class);
                });
    }
}

