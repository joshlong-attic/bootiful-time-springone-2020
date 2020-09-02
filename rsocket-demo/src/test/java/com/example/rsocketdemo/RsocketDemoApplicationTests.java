package com.example.rsocketdemo;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.RSocketServerInitializedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Log4j2/
@SpringBootTest
@Import(ClientConfiguration.class)
class RsocketDemoApplicationTests {
    //@Autowired
    GreetingClient client;

    @Autowired
    RSocketRequester requester;


    @AfterEach
    void tearDown() {
        requester.rsocket().dispose();
    }

    //@Test
    void retroClientRequestRespond() {
        var request = client
                .greet(new RequestResponse("SpringOne"))
                .take(1);

        StepVerifier
                .create(request)
                .assertNext(res -> {
                    Assertions.assertNotNull(res);
                    Assertions.assertTrue(res.getName().contains("SpringOne"));
                })
                .verifyComplete();

    }

    @Test
    void requesterRequestRespond() {
        var request = requester
                .route("greetings")
                .data(Mono.just(new RequestResponse("SpringOne 2020")), RequestResponse.class)
                .retrieveFlux(RequestResponse.class)
                .take(1);

        StepVerifier
                .create(request)
                .assertNext(res -> {
                    Assertions.assertNotNull(res);
                    Assertions.assertTrue(res.getName().contains("SpringOne"));
                })
                .verifyComplete();
    }

}

// I tried sending a requester to the context after server initialized - otherwise it cannot connect.
@Configuration
class ClientConfiguration {

    @Autowired
    GenericApplicationContext ctx;

    @EventListener(RSocketServerInitializedEvent.class)
    void rsocketClients() {
        RSocketRequester req = ctx
                .getBean(RSocketRequester.Builder.class)
                .connectTcp("localhost", 8888)
                .block();

        ctx.registerBean(RSocketRequester.class, () -> req);
    }
}