package com.example.integration.audio;

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


@SpringBootApplication
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}

@Log4j2
@Configuration
class GatewayConfiguration {


    static final String WAVS = "wavs";
    static final String MP3_CONVERSION_MESSAGE_HANDLER = "mp3ConversionMessageHandler";

    private final File mp3Dir;

    GatewayConfiguration(@Value("file://${user.home}/Desktop/mp3s") File file) {
        this.mp3Dir = file;
    }

    @Bean
    MessageChannel mp3s() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    MessageChannel wavs() {
        return MessageChannels.publishSubscribe(WAVS).get();
    }

    @Bean
    IntegrationFlow wavToMp3Conversion() {
        return IntegrationFlows
                .from(wavs())
                .handle(Files.outboundGateway(mp3Dir).autoCreateDirectory(true))
                .transform(File.class,
                        AudioUtils::convert,
                        spec -> spec.id(MP3_CONVERSION_MESSAGE_HANDLER))
                .channel(mp3s())
                .get();
    }

    //@Bean
    ApplicationListener<ApplicationReadyEvent> begin(AudioIntegrationClient client) {
        return events -> {
            var file = new File(System.getenv("HOME") + "/Desktop/sound.wav");
            Assert.state(file.exists(), () -> "the file " + file.getAbsolutePath() + " does not exist");
            log.info("start...");
            var convertToMp3 = client.convertToMp3(file);
            log.info("stop...");
            log.info("new mp3 available " + convertToMp3.getAbsolutePath() + '.');
        };
    }


}

@MessagingGateway
interface AudioIntegrationClient {

    @Gateway(requestChannel = GatewayConfiguration.WAVS)
    File convertToMp3(@Payload File wav);
}


@Log4j2
abstract class AudioUtils {

    private static File deriveMp3FileForWavFile(File wav) {
        return new File(wav.getParentFile(), wav.getName().substring(0, wav.getName().length() - 4) + ".mp3");
    }

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

}