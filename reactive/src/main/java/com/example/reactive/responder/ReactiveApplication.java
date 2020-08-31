package com.example.reactive.responder;

import com.example.reactive.GreetingRequest;
import com.example.reactive.GreetingResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Controller
@SpringBootApplication
public class ReactiveApplication {

    public static void main(String[] args) {
        System.setProperty("spring.rsocket.server.port", "8888");
        SpringApplication.run(ReactiveApplication.class, args);
    }

    @MessageMapping("greetings")
    Flux<GreetingResponse> greet(GreetingRequest request) {
        return Flux
                .fromStream(Stream.generate(() -> new GreetingResponse("Hello, " + request.getName() + " @ " + Instant.now() + "!")))
                .delayElements(Duration.ofSeconds(1));
    }

}
