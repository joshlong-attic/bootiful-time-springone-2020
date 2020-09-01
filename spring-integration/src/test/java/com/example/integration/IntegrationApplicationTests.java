package com.example.integration;

import com.example.integration.files.FileToStringFlowConfiguration;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.mock.MockIntegration;
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

    @Autowired
    private MockIntegrationContext mockIntegrationContext;

    @Test
    public void fileMessageFlowTest() throws Exception {


        var cdl = new CountDownLatch(1);

        // mock message source (replace file inbound adapter)
        var testMessage = "test @ " + Instant.now().toString();
        var mockMessageSource = MockIntegration.mockMessageSource(buildTempFile(testMessage));
        this.mockIntegrationContext.substituteMessageSourceFor(
                FileToStringFlowConfiguration.MESSAGE_SOURCE_ID, mockMessageSource);

        // mock messageHandler (replace MyCustomMessageHandler)
        var messageHandler = MockIntegration
                .mockMessageHandler()
                .handleNext(message -> {
                    Assert.assertNotNull(message.getPayload());
                    Assert.assertTrue(message.getPayload() instanceof String);
                    cdl.countDown();
                });
        this.mockIntegrationContext.substituteMessageHandlerFor(
                FileToStringFlowConfiguration.MESSAGE_HANDLER_ID, messageHandler);

        cdl.await();
        log.info("done!");
    }

    @AfterTestMethod
    public void tearDown() {
        this.mockIntegrationContext.resetBeans();
    }

    @SneakyThrows
    private static File buildTempFile(String message) {
        var temp = Files.createTempFile("temp", ".txt").toFile();
        try (var fw = new FileWriter(temp)) {
            FileCopyUtils.copy(message, fw);
        }
        return temp;
    }

}
