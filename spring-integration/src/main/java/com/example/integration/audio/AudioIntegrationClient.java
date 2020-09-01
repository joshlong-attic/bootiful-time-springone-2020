package com.example.integration.audio;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.File;

@MessagingGateway
interface AudioIntegrationClient {

    @Gateway(requestChannel = GatewayConfiguration.WAVS)
    File convertToMp3(@Payload File wav);
}
