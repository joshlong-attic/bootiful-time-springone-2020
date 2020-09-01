package com.example.integration.audio;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageChannel;

import java.io.File;

@Log4j2
@Configuration
class GatewayConfiguration {

    static final String WAVS = "wavs";

    static final String MP3S = "mp3s";

    static final String MP3_CONVERSION_MESSAGE_HANDLER = "mp3ConversionMessageHandler";

    @Bean
    MessageChannel mp3s() {
        return MessageChannels.publishSubscribe(MP3S).get();
    }

    @Bean
    MessageChannel wavs() {
        return MessageChannels.publishSubscribe(WAVS).get();
    }

    @Bean
    IntegrationFlow wavToMp3Conversion(@Value("file://${user.home}/Desktop/mp3s") File mp3Dir) {
        return IntegrationFlows
                .from(wavs())
                .handle(Files.outboundGateway(mp3Dir).autoCreateDirectory(true))
                .transform(File.class,
                        wav -> AudioUtils.convert(wav, AudioUtils.deriveMp3FileForWavFile(wav)),
                        spec -> spec.id(MP3_CONVERSION_MESSAGE_HANDLER))
                .channel(mp3s())
                .get();
    }

    @Bean
    @Profile("demo")
    ApplicationListener<ApplicationReadyEvent> begin(AudioIntegrationClient client) {
        return events -> {
            var file = new File(System.getenv("HOME") + "/Desktop/interview.wav");
            log.info("start...");
            var convertToMp3 = client.convertToMp3(file);
            log.info("stop...");
            log.info("new mp3 available " + convertToMp3.getAbsolutePath() + '.');
        };
    }


}