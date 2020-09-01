package com.example.integration.files;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.dsl.FileInboundChannelAdapterSpec;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.io.File;

@Configuration
public class FileToStringFlowConfiguration {

    public static final String MESSAGE_SOURCE_ID = "mySimpleMessageSource";
    public static final String MESSAGE_HANDLER_ID = "customMessageHandlerId";

    @Bean(name = "file-to-string-flow")
    IntegrationFlow integrationFlow(
            MyCustomMessageHandler myCustomMessageHandler,
            @Value("file://${user.home}/Desktop/in") File desktopInDirectory) {

        FileInboundChannelAdapterSpec messageSourceSpec = Files
                .inboundAdapter(desktopInDirectory)
                .autoCreateDirectory(true);

        return IntegrationFlows
                .from(messageSourceSpec, pm -> pm.poller(pc -> pc.fixedRate(100)).id(MESSAGE_SOURCE_ID))
                .transform(new FileToStringTransformer())
                .transform(String.class, String::toUpperCase)
                .handle(myCustomMessageHandler, spec -> spec.id(MESSAGE_HANDLER_ID))
                .get();
    }

}

@Log4j2
@Component
class MyCustomMessageHandler implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        log.info(message.getPayload());
        log.info(message.getHeaders());
    }
}