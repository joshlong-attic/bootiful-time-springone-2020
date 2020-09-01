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
import org.springframework.util.Assert;

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
    public CompletableFuture<File> convertToMp3(File wav) {
        log.info("before...");
        var cb = CompletableFuture.completedFuture(AudioUtils.convert(wav, AudioUtils.deriveMp3FileForWavFile(wav)));
        log.info("after...");
        return cb;
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
