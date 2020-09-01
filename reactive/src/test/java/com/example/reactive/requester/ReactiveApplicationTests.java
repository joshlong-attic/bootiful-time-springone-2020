package com.example.reactive.requester;

import com.example.reactive.GreetingRequest;
import com.example.reactive.responder.ReactiveApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import reactor.test.StepVerifier;


class ReactiveApplicationTests {

    @Test
    void contextLoads() {

        var service = new SpringApplicationBuilder()
                .properties("spring.rsocket.server.port=8888")
                .sources(ReactiveApplication.class)
                .build()
                .run();

        var client = new SpringApplicationBuilder()
                .sources(com.example.reactive.requester.ReactiveApplication.class)
                .build()
                .run();

        var greetingClient = client.getBean(GreetingClient.class);
        var greetingRequest = new GreetingRequest("SpringOne 2020");
        var greet = greetingClient.greet(greetingRequest).take(1);
        StepVerifier
                .create(greet)
                .expectNextMatches(gr -> gr.getMessage().contains(greetingRequest.getName()))
                .verifyComplete();
        client.stop();
        service.stop();
        System.out.println("exiting..");
    }
}
