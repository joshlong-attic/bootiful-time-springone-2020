package com.example.integration.audio;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
@Profile("audio")
@Configuration
class GatewayConfiguration {

    static final String WAVS = "wavs";
    static final String MP3S = "mp3s";

    private final File mp3Directory;

    GatewayConfiguration(@Value("file://${user.home}/Desktop/mp3s") File mp3Dir) {
        this.mp3Directory = mp3Dir;
    }

    @Bean
    AudioClient audioClient(AudioService service,   @Value("file://${user.home}/Desktop/interview.wav") File f) {
        return new AudioClient(service, f);
    }

    @Bean(MP3S)
    MessageChannel mp3s() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean(WAVS)
    MessageChannel wavs() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    IntegrationFlow wavConversionFlow() {
        return IntegrationFlows
                .from(wavs())
                .handle(Files.outboundGateway(this.mp3Directory).autoCreateDirectory(true))
                .transform(File.class, wav -> AudioUtils.convert(wav, AudioUtils.deriveMp3FileForWavFile(wav)))
                .channel(mp3s())
                .get();
    }

}
