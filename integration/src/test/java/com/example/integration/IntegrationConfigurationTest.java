package com.example.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.mock.MockIntegration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.io.File;

@SpringIntegrationTest
@SpringBootTest
class IntegrationConfigurationTest {


    @Autowired
    private IntegrationConfiguration configuration;

    @Autowired
    private MockIntegrationContext context;

    @Test
    void contextLoads() {


        var mockMesssageHandler = MockIntegration.mockMessageHandler()
                .handleNext(message -> {

                    Assertions.assertTrue(message.getPayload() instanceof File);
                    var file = (File) message.getPayload();
                    Assertions.assertTrue(file.getAbsolutePath().contains("/mp3s/"));
                });

        this.context.substituteMessageHandlerFor(IntegrationConfiguration.WAV_TO_MP3_DIR,
                mockMesssageHandler);

        var wav = new File(System.getenv("HOME") + "/Desktop/sound.wav");
        var message = MessageBuilder.withPayload(wav).build();
        this.configuration.wavs().send(message);


    }

    @AfterTestMethod
    public void tearDown() {
        this.context.resetBeans();
    }

}