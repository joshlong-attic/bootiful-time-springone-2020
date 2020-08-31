package com.example.integration;

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

    static final String MESSAGE_SOURCE_ID = "file-to-string-flow.org.springframework.integration.config.ConsumerEndpointFactoryBean#2";

    static final String SOURCE_POLLING_CHANNEL_ADAPTER_ID =
            "file-to-string-flow.org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean#0";

    @Test
    public void fileMessageFlowTest() throws Exception {

        enumerateBeanDefinitionNames(Object.class);
        enumerateBeanDefinitionNames(MessageHandler.class);
        enumerateBeanDefinitionNames(MessageSource.class);

        var firstCountDownLatch = new CountDownLatch(1);
        var secondCountDownLatch = new CountDownLatch(1);
        var testMessage = "test @ " + Instant.now().toString();
        var mockMessageSource = MockIntegration.mockMessageSource(init(testMessage));
        var messageHandler = MockIntegration
                .mockMessageHandler()
                .handleNext(message -> {
                    Assert.assertNotNull(message.getPayload());
                    Assert.assertTrue(message.getPayload() instanceof String);
                    secondCountDownLatch.countDown();
                });
        this.mockIntegrationContext.substituteMessageHandlerFor(MESSAGE_SOURCE_ID, messageHandler);
        this.mockIntegrationContext.substituteMessageSourceFor(SOURCE_POLLING_CHANNEL_ADAPTER_ID, mockMessageSource);
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

    private void enumerateBeanDefinitionNames(Class<?> clazz) {
        if (!DEBUG) return;

        log.info(System.lineSeparator());
        log.info("---------------------------------------------");
        log.info(clazz.getName());
        log.info("---------------------------------------------");
        var beanNamesForType = this.context.getBeanNamesForType(clazz);
        for (var msgHandlerBeanName : beanNamesForType) {
            log.info(msgHandlerBeanName + ':' + this.context.getBean(msgHandlerBeanName).getClass());
        }
    }
}
