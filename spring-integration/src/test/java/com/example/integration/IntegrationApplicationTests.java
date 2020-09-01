package com.example.integration;

import com.example.integration.files.FileToStringFlowConfiguration;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.mock.MockIntegration;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

@Log4j2
@SpringBootTest
@SpringIntegrationTest
//@LongRunningTest // <-- only activate if the RUN_LONG_INTEGRATION_TESTS env var == true
class IntegrationApplicationTests {

    private static boolean DEBUG = false;

    @Autowired
    private PublishSubscribeChannel output;

    @Autowired
    private MockIntegrationContext mockIntegrationContext;

    @Autowired
    private ApplicationContext context;



    @Test
    public void fileMessageFlowTest() throws Exception {


        var firstCountDownLatch = new CountDownLatch(1);
        var secondCountDownLatch = new CountDownLatch(1);


        // mock message source (replace file inbound adapter)
        var testMessage = "test @ " + Instant.now().toString();
        var mockMessageSource = MockIntegration.mockMessageSource(init(testMessage));
        this.mockIntegrationContext.substituteMessageSourceFor(
                FileToStringFlowConfiguration.MESSAGE_SOURCE_ID, mockMessageSource);

        // mock messageHandler (replace MyCustomMessageHandler)
        var messageHandler = MockIntegration
                .mockMessageHandler()
                .handleNext(message -> {
                    Assert.assertNotNull(message.getPayload());
                    Assert.assertTrue(message.getPayload() instanceof String);
                    secondCountDownLatch.countDown();
                });

        this.mockIntegrationContext.substituteMessageHandlerFor(FileToStringFlowConfiguration.MESSAGE_HANDLER_ID, messageHandler);
        this.output.subscribe(message -> {
            Assert.assertNotNull(message.getPayload());
            Assert.assertEquals(message.getPayload(), testMessage.toUpperCase());
            Assert.assertTrue(message.getPayload() instanceof String);
            firstCountDownLatch.countDown();
        });
        firstCountDownLatch.await();
        secondCountDownLatch.await();
        log.info("done!");
    }

    @AfterTestMethod
    public void tearDown() {
        this.mockIntegrationContext.resetBeans();
    }

    @SneakyThrows
    private static File init(String message) {
        var temp = Files.createTempFile("temp", ".txt").toFile();
        try (var fw = new FileWriter(temp)) {
            FileCopyUtils.copy(message, fw);
        }
        return temp;
    }

}
