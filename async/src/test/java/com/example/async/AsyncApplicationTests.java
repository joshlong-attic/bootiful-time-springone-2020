package com.example.async;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
class AsyncApplicationTests {

    @Autowired
    AudioService audioService;

    @Test
    void contextLoads() throws Exception {
        var file = new File(System.getenv("HOME") + "/Desktop/sound.wav");
        Assert.state(file.exists(), () -> "the file " + file.getAbsolutePath() + " must exist.");
        CompletableFuture<File> fileCompletableFuture = this.audioService.convertToMp3(file);
        File convertedFile = fileCompletableFuture.get();
        Assert.isTrue(convertedFile.getAbsolutePath().endsWith("sound.mp3"), "the resulting file should be an .mp3");

    }

}
