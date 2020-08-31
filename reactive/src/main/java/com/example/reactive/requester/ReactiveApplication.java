package com.example.reactive.requester;

import com.example.reactive.GreetingRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.retrosocket.EnableRSocketClients;
import org.springframework.stereotype.Component;

@EnableRSocketClients
@SpringBootApplication
public class ReactiveApplication {

    @SneakyThrows
    public static void main(String[] args) {

        SpringApplication.run(ReactiveApplication.class, args);
        System.in.read();
    }

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.connectTcp("localhost", 8888).block();
    }

}

@Log4j2
@Profile("!default")
@Component
@RequiredArgsConstructor
class Runner {

    private final GreetingClient client;

    @EventListener(ApplicationReadyEvent.class)
    public void go() {

        this.client
                .greet(new GreetingRequest("Mario"))
                .subscribe(gr -> log.info(gr.toString()));
    }
}

