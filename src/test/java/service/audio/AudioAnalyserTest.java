package service.audio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;

public class AudioAnalyserTest {

    @Test
    void timeStampSampleTest() {
        int sampleRate = 44100;
        AudioFormat defaultAudioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
        double samplePeriod = 1d / sampleRate;
        double actualTime3 = AudioAnalysis.getTimestampOfSample(3, defaultAudioFormat);
        Assertions.assertEquals(3 * samplePeriod, actualTime3);

    }
}
