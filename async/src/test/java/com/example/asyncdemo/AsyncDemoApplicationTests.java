package com.example.asyncdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class AsyncTests  {

    @Autowired
    AudioService audioService;

    @Value("file://${HOME}/Desktop/sound.wav")
    File wavFile;

    @Test
    void execIt() throws Exception {
        var latch = new CountDownLatch(1);
        audioService
                .convert(wavFile)
                .thenAccept(mp3 -> {
                    latch.countDown();
                    Assertions.assertTrue(mp3.exists());
                });

        latch.await();
    }
}

@SpringBootTest
class AsyncDemoApplicationTests {

    @Autowired
    AudioService audioService;

    @Value("file://${HOME}/Desktop/sound.wav")
    File wavFile;

    @Test
    void executesAudioService() throws Exception {
        var mp3File = audioService
                .convert(wavFile)
                .get();

		Assertions.assertTrue(mp3File.exists());
		Assertions.assertTrue(mp3File.getName().endsWith(".mp3"));
    }

}
