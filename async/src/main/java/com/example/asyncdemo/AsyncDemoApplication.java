package com.example.asyncdemo;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SpringBootApplication
public class AsyncDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncDemoApplication.class, args);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> eventExec(AudioService service, @Value("file://${HOME}/Desktop/sound.wav") File wavFile) {
        return x -> {
            try {
                var fileFuture = service.convert(wavFile);
                fileFuture.get();
            } catch (Exception e) {
                log.error(e);
            }
        };
    }
}

@Service
@Log4j2
class SpringAudioService implements AudioService {

    @Override
    @Async
    public CompletableFuture<File> convert(File input) {
        log.info("Start...");
        var outFile = AudioUtils.convert(input);
        log.info("Complete...");
        return CompletableFuture.completedFuture(outFile);
     }
}

@Profile("manual")
@Service
class ManualAudioService implements AudioService {
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public CompletableFuture<File> convert(File wavFile) {
        var cf = new CompletableFuture<File>();
        this.executorService.execute(() ->
        {
            var convertedFile = AudioUtils.convert(wavFile);
            cf.complete(convertedFile);
        });
        return cf;
    }

}


interface AudioService {
    CompletableFuture<File> convert(File input);
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