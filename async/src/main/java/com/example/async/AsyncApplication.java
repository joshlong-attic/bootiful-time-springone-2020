package com.example.async;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@EnableAsync
@SpringBootApplication
public class AsyncApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(AsyncApplication.class, args);
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
        var fileCompletableFuture = audioService.convertToMp3(this.input);
        log.info("got the CompletableFuture<File>");
        fileCompletableFuture.thenAccept(convertedFile -> log.info("new file lives at " + convertedFile.getAbsolutePath() + '.')).get();
    }
}


interface AudioService {

    CompletableFuture<File> convertToMp3(File input);
}

@Log4j2
@Service
@RequiredArgsConstructor
class FfmpegDelegatingAudioService implements AudioService {

    @Async
    @Override
    public CompletableFuture<File> convertToMp3(File input) {
        log.info("start...");
        var newFileFor = new File(input.getParentFile(), input.getName().substring(0, input.getName().length() - 4) + ".mp3");
        var cb = CompletableFuture.completedFuture(convert(input, newFileFor));
        log.info("stop...");
        return cb;
    }

    @SneakyThrows
    private static File convert(File wav, File mp3) {
        log.info("converting " + wav.getAbsolutePath() + " to " + mp3.getAbsolutePath() + '.');
        var command = "ffmpeg -i " + wav.getAbsolutePath() + " " + mp3.getAbsolutePath();
        log.info("the command is " + command);
        var exec = Runtime.getRuntime().exec(command);
        var statusCode = exec.waitFor();
        if (statusCode == 0) {
            return mp3;
        }
        throw new RuntimeException("could not convert '" + wav.getAbsolutePath() + "'!");
    }


}
