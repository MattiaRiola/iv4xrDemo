package service.audio;


import config.audio.AudioConfig;
import entity.math.Complex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.view.PlotSpectrum2D;

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

        byte[] ding1Sound = AudioAnalysis.readAudio("src/test/resources/audio/ding1.wav", AudioConfig.getDefaultFormat());
        int[][] ding1SoundWav = AudioAnalysis.readWavFile("src/test/resources/audio/ding1.wav");
        int[][] sound50HzWav = AudioAnalysis.readWavFile("src/test/resources/audio/50HzSound.wav");
        int[][] sound10000HzWav = AudioAnalysis.readWavFile("src/test/resources/audio/10000HzSound.wav");
        byte[] fireSizzleSound = AudioAnalysis.readAudio("src/test/resources/audio/firesizzle.wav", AudioConfig.getDefaultFormat());
        byte[] wave50HzSound = AudioAnalysis.readAudio("src/test/resources/audio/50HzSound.wav", AudioConfig.getDefaultFormat());
        byte[] wave10000HzSound = AudioAnalysis.readAudio("src/test/resources/audio/10000HzSound.wav", AudioConfig.getDefaultFormat());
        Complex[][] ding1Spectrum = AudioAnalysis.FFT(ding1Sound);
        Complex[][] ding1WavSpectrum = AudioAnalysis.FFT32bit(ding1SoundWav[0]);
        PlotSpectrum2D.plotSpectrum(ding1WavSpectrum, "wav spectrum");
        PlotSpectrum2D.plotSpectrum(ding1Spectrum, "ding1 spectrum");

        //Plot2D.plotArray(Arrays.stream(sound50HzWav[0]).filter(i->i!=0).limit(1000).toArray(), Color.BLUE);       //Plot2D.plotArray(sound50HzWav[0], Color.BLUE);
        //Plot2D.plotArray(Arrays.stream(sound10000HzWav[0]).filter(i->i!=0).limit(1000).toArray(), Color.RED);       //Plot2D.plotArray(sound50HzWav[0], Color.BLUE);


        Thread.sleep(10000);
        Assertions.fail("TODO: tests on audio analysis");
    }

    @Test
    void graphPlotterTest() throws InterruptedException {

    }


}
