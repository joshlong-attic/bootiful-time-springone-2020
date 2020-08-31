package com.example.reactive.requester;

import com.example.reactive.GreetingRequest;
import com.example.reactive.GreetingResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.retrosocket.RSocketClient;
import reactor.core.publisher.Flux;

@RSocketClient
public interface GreetingClient {

    @MessageMapping("greetings")
    Flux<GreetingResponse> greet(GreetingRequest request);
}
