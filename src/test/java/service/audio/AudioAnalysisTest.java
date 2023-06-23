package service.audio;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class AudioAnalysisTest {

    @BeforeAll
    public static void init() {

    }

    @AfterAll
    public static void endingTest() {

    }

    @Test
    public void audioAnalysisTest() throws IOException, UnsupportedAudioFileException, InterruptedException {

        byte[] ding1Sound = AudioAnalysis.readAudio("src/test/resources/audio/ding1.wav", AudioConfig.getFormat());
        int[][] ding1SoundWav = AudioAnalysis.readWavFile("src/test/resources/audio/ding1.wav");
        byte[] fireSizzleSound = AudioAnalysis.readAudio("src/test/resources/audio/firesizzle.wav", AudioConfig.getFormat());
        byte[] wave50HzSound = AudioAnalysis.readAudio("src/test/resources/audio/50HzSound.wav", AudioConfig.getFormat());
        byte[] wave10000HzSound = AudioAnalysis.readAudio("src/test/resources/audio/10000HzSound.wav", AudioConfig.getFormat());
        Complex[][] ding1Spectrum = AudioAnalysis.FFT(ding1Sound);
        Complex[][] fireSizzleSpectrum = AudioAnalysis.FFT(fireSizzleSound);
        Complex[][] wave50HzSpectrum = AudioAnalysis.FFT(wave50HzSound);
        Complex[][] wave10000HzSpectrum = AudioAnalysis.FFT(wave10000HzSound);
        //Plot2D.plot2Array(ding1SoundWav[1],ding1SoundWav[0]);

        Thread.sleep(10000);
        Assertions.fail("TODO: tests on audio analysis");
    }

    @Test
    void graphPlotterTest() throws InterruptedException {

    }


}
