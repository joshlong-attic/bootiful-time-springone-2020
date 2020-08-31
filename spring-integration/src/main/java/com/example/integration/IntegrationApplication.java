package com.example.integration;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.FileInboundChannelAdapterSpec;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;

@SpringBootApplication
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }


}

@Log4j2
@Component
class AudioClient {

    private final AudioService audioService;
    private final File input;

    AudioClient(AudioService audioService, @Value("file://${user.home}/Desktop/interview.wav") File interviewWav) {
        this.audioService = audioService;
        this.input = interviewWav;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void begin() throws Exception {
        log.info("attempting to convert " + this.input.getAbsolutePath() + '.');
        var convertToMp3 = audioService.convertToMp3(this.input);
        log.info("done: " + convertToMp3.getAbsolutePath());
    }
}

@Log4j2
@Configuration
class GatewayConfiguration {

    static final String WAVS = "wavs";
    static final String MP3S = "mp3s";

    private final File mp3Directory;

    GatewayConfiguration(@Value("file://${user.home}/Desktop/mp3s") File mp3Dir) {
        this.mp3Directory = mp3Dir;
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
                .transform(File.class, new GenericTransformer<File, File>() {
                    @Override
                    public File transform(File wav) {
                        return AudioUtils.convert(wav, AudioUtils.deriveMp3FileForWavFile(wav));
                    }
                })
                .channel(mp3s())
                .get();
    }

}


@Log4j2
abstract class AudioUtils {

    public static File deriveMp3FileForWavFile(File wav) {
        return new File(wav.getParentFile(), wav.getName().substring(0, wav.getName().length() - 4) + ".mp3");
    }

    @SneakyThrows
    public static File convert(File wav, File mp3) {
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

@MessagingGateway
interface AudioService {

    @Gateway(requestChannel = GatewayConfiguration.WAVS)
    File convertToMp3(@Payload File wav);
}


@Configuration
class FileToStringFlowConfiguration {


    @Log4j2
    @Component
    static class MyCustomMessageHandler implements MessageHandler {

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            log.info(message.getPayload());
            log.info(message.getHeaders());
        }
    }


    @Bean
    PublishSubscribeChannel output() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean(name = "file-to-string-flow")
    IntegrationFlow integrationFlow(
            MyCustomMessageHandler myCustomMessageHandler,
            @Value("file://${user.home}/Desktop/in") File desktopInDirectory) {

        FileInboundChannelAdapterSpec messageSourceSpec = Files
                .inboundAdapter(desktopInDirectory)
                .autoCreateDirectory(true);

        return IntegrationFlows
                .from(messageSourceSpec, pm -> pm.poller(pc -> pc.fixedRate(100)))
                .transform(new FileToStringTransformer())
                .transform(String.class, String::toUpperCase)
                .channel(this.output())
                .handle(myCustomMessageHandler)
                .get();
    }

}