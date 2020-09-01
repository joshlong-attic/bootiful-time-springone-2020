package com.example.integration;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.Assert;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }

}

@Log4j2
@Configuration
class IntegrationConfiguration {


    static final String WAVS = "wavs";
    static final String MP3S = "mp3s";

    static final String WAV_TO_MP3_DIR = "wav-to-mp3-directory";

    @Bean
    MessageChannel wavs() {
        return MessageChannels.publishSubscribe(WAVS).get();
    }

    @Bean
    MessageChannel mp3s() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    IntegrationFlow wavToMp3Conversion(@Value("file://${user.home}/Desktop/mp3s") File mp3sDirectoryToMoveFilesTo) {
        return IntegrationFlows
                .from(this.wavs())
                .handle(Files.outboundGateway(mp3sDirectoryToMoveFilesTo).autoCreateDirectory(true))
                .transform(File.class, AudioUtils::convert, spec -> spec.id(WAV_TO_MP3_DIR))
                .channel(mp3s())
                .get();
    }


//    @Bean
    ApplicationListener<ApplicationReadyEvent> client(AudioClient ac, @Value("file://${user.home}/Desktop/sound.wav") File wav) {
        return event -> {
            try {
                log.info("start...");
                var mp3 = ac.convertToMp3(wav);
                log.info("stop...");
                var result = mp3.get();
                log.info("mp3 " + result.getAbsolutePath());
            } catch (Exception e) {
                log.error(e);
            }
        };

    }
}


@MessagingGateway
interface AudioClient {

    @Gateway(
            requestChannel = IntegrationConfiguration.WAVS,
            replyChannel = IntegrationConfiguration.MP3S
    )
    CompletableFuture<File> convertToMp3(@Payload File file);
}


@Log4j2
abstract class AudioUtils {

    @SneakyThrows
    public static File convert(File wav) {
        var mp3 = deriveMp3FileForWavFile(wav);
        Assert.state(!mp3.exists() || mp3.delete(), () -> "the destination .mp3 file " + mp3.getAbsolutePath() + " must not exist!");
        log.info("converting " + wav.getAbsolutePath() + " to " + mp3.getAbsolutePath() + '.');
        var command = "ffmpeg -i " + wav.getAbsolutePath() + " " + mp3.getAbsolutePath();
        var exec = Runtime.getRuntime().exec(command);
        var statusCode = exec.waitFor();
        if (statusCode == 0) {
            log.info("converted " + wav.getAbsolutePath() + " to " + mp3.getAbsolutePath() + '.');
            return mp3;
        }
        throw new RuntimeException("could not convert '" + wav.getAbsolutePath() + "'!");
    }

    private static File deriveMp3FileForWavFile(File wav) {
        return new File(wav.getParentFile(), wav.getName().substring(0, wav.getName().length() - 4) + ".mp3");
    }

}
