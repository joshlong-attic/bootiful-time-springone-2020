package com.example.integration.audio;


import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.mock.MockIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.io.File;


@Log4j2
@SpringBootTest
@SpringIntegrationTest
//@LongRunningTest // <-- only activate if the RUN_LONG_INTEGRATION_TESTS env var == true
class GatewayIntegrationTest {

    @Autowired
    @Qualifier(GatewayConfiguration.WAVS)
    private MessageChannel wavs;

    @Autowired
    private MockIntegrationContext mockIntegrationContext;

    @Test
    public void fileMessageFlowTest() throws Exception {
        var sampleWavFile = new File(System.getenv("HOME"), "/Desktop/sound.wav");
        var messageHandler = MockIntegration
                .mockMessageHandler()
                .handleNext(message -> {
                    Assertions.assertTrue(message.getPayload() instanceof File);
                    File wavInMp3Directory = (File) message.getPayload();
                    Assertions.assertTrue(wavInMp3Directory.getAbsolutePath().contains("/mp3s/"));
                });
        this.mockIntegrationContext.substituteMessageHandlerFor(GatewayConfiguration.MP3_CONVERSION_MESSAGE_HANDLER, messageHandler);
        this.wavs.send(MessageBuilder.withPayload(sampleWavFile).build());
    }

    @AfterTestMethod
    public void tearDown() {
        this.mockIntegrationContext.resetBeans();
    }


}
