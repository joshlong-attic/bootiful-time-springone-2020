package com.example.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.FileInboundChannelAdapterSpec;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.io.File;

@SpringBootApplication
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }

    @Bean
    PublishSubscribeChannel output () {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean(name = "file-to-string-flow")
    IntegrationFlow integrationFlow(@Value("file://${user.home}/Desktop/in") File desktopInDirectory) {

        FileInboundChannelAdapterSpec messageSourceSpec = Files
                .inboundAdapter(desktopInDirectory)
                .autoCreateDirectory(true);

        return IntegrationFlows
                .from(messageSourceSpec,  pm -> pm. poller(pc -> pc.fixedRate(100)))
                .transform(new FileToStringTransformer())
                .channel(this.output())
                .handle((GenericHandler<String>) (s, messageHeaders) -> null)
                .get();
    }

    private <T> T debug(T message, MessageHeaders headers) {
        System.out.println("new message: " + message);
        headers.keySet().forEach((k) -> System.out.println(k + ":" + headers.get(k)));
        return message;
    }



}
