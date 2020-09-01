package com.example.reactive.requester;

import com.example.reactive.GreetingRequest;
import com.example.reactive.responder.ReactiveApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.rsocket.RSocketRequester;
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
        stop(client, service);
        System.out.println("exiting..");
    }

    private void stop(ConfigurableApplicationContext client, ConfigurableApplicationContext service) {
        client.getBean(RSocketRequester.class).rsocket().dispose();
        client.stop();
        service.stop();
    }


}
