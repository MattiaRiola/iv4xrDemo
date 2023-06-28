package utils;

import entity.audio.AudioSignal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.audio.AudioAnalysis;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class FileFormatTest {

    @Test
    public void testAudioFormatChunk() throws UnsupportedAudioFileException, IOException {
        AudioSignal audio = FileExplorer.readWavFile(FileExplorer.DIR_GAME_SOUNDS + "ding1.wav", "ding1.wav");
        Assertions.assertEquals(1, audio.getFormat().getChannels());
        Assertions.assertEquals(1d, audio.getAudioDuration());
        Assertions.assertTrue(0 < AudioAnalysis.getTimestampOfChunk(2, audio.getFormat()));

    }
}
