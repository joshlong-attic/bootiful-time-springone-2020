package com.example.integration;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.FileInboundChannelAdapterSpec;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.File;

@SpringBootApplication
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }


}


@Configuration
class GatewayConfiguration {


}

@MessagingGateway
interface FileService {


    void write (@Payload String payload)  ;
}


@Configuration
class FileToStringFlowConfiguration {


    @Log4j2
    @Component
    static class MyCustomMessageHandler implements MessageHandler {

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            log.info(message.getPayload());
            log.info(message.getHeaders());
        }
    }


    @Bean
    PublishSubscribeChannel output() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean(name = "file-to-string-flow")
    IntegrationFlow integrationFlow(
            MyCustomMessageHandler myCustomMessageHandler,
            @Value("file://${user.home}/Desktop/in") File desktopInDirectory) {

        FileInboundChannelAdapterSpec messageSourceSpec = Files
                .inboundAdapter(desktopInDirectory)
                .autoCreateDirectory(true);

        return IntegrationFlows
                .from(messageSourceSpec, pm -> pm.poller(pc -> pc.fixedRate(100)))
                .transform(new FileToStringTransformer())
                .transform(String.class, String::toUpperCase)
                .channel(this.output())
                .handle(myCustomMessageHandler)
                .get();
    }

}