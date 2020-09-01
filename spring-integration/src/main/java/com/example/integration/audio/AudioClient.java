package com.example.integration.audio;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Log4j2
class AudioClient {

    private final AudioService audioService;
    private final File input;

    AudioClient(AudioService audioService, File interviewWav) {
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
