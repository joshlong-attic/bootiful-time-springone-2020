package com.example.rsocketdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.rsocket.context.RSocketServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.retrosocket.EnableRSocketClients;
import org.springframework.retrosocket.RSocketClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
//@EnableRSocketClients
public class RsocketDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(RsocketDemoApplication.class, args);
	}
}

@Controller
class ExampleController {

	@MessageMapping("greetings")
	Flux<RequestResponse> greet(RequestResponse request) {
		return
				Flux.fromStream(Stream.generate(() -> new RequestResponse(request.getName())))
				.take(5)
				.delayElements(Duration.ofSeconds(1));
	}
}

@RSocketClient
interface GreetingClient {

	@MessageMapping("greetings")
	Flux<RequestResponse> greet(RequestResponse request);
}
