package service.audio;


import entity.audio.AudioSignal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioAnalysisTest {

    @BeforeAll
    public static void init() {

    }

    @AfterAll
    public static void endingTest() {

    }

    @Test
    public void bigendtolittleend() {
        byte low = 2;
        byte high = 1;
        ByteBuffer bb = ByteBuffer.allocate(2).put(high).put(low)
                .order(ByteOrder.LITTLE_ENDIAN);
        int newlow = reverseBitsByte(bb.get(0));
        int newhigh = reverseBitsByte(bb.get(1));
        int oldInt = (high << 8) + (low & 0x00ff);
        int newInt = (newhigh << 8) + (newlow & 0x00ff);
    }

    public byte reverseBitsByte(byte x) {
        int intSize = 8;
        byte y = 0;
        for (int position = intSize - 1; position > 0; position--) {
            y += ((x & 1) << position);
            x >>= 1;
        }
        return y;
    }

    @Test
    public void audioAnalysisTest() throws IOException, UnsupportedAudioFileException, InterruptedException {

        AudioSignal ding1Audio = AudioAnalysis.readWavFile("src/test/resources/audio/ding1.wav", "ding1");
        AudioSignal ding2Audio = AudioAnalysis.readWavFile("src/test/resources/audio/ding2.wav", "ding2");
//        AudioSignal fireSizzleAudio = AudioAnalysis.readWavFile("src/test/resources/audio/firesizzle.wav");
//        AudioSignal deathAudio = AudioAnalysis.readWavFile("src/test/resources/audio/death.wav");
//        AudioSignal monsterAudio = AudioAnalysis.readWavFile("src/test/resources/audio/monsterAttack.wav");
//        AudioSignal tensionAudio = AudioAnalysis.readWavFile("src/test/resources/audio/tension.wav");
//        AudioSignal sound50HzAudio = AudioAnalysis.readWavFile("src/test/resources/audio/50HzSound.wav");
//        AudioSignal sound10000HzAudio = AudioAnalysis.readWavFile("src/test/resources/audio/10000HzSound.wav");
        //AudioSignal windowlickerCut = AudioAnalysis.readWavFile("src/test/resources/audio/WindowlickerCut.wav","WindowlickerCut");
        //AudioSignal windowlickerAudio = AudioAnalysis.readWavFile("src/test/resources/audio/Windowlicker.wav","Windowlicker");

        //Plot2D.plotArray(ding1Audio.getSamples()[0], Color.BLUE);
        //Plot2D.plotArray(ding2Audio.getSamples()[0], Color.RED);
        //Plot2D.plotArray(windowlickerAudio.getSamples()[0], Color.RED);
        //PlotSpectrum2D.plotSpectrum(spectrumWindowLilckerByte, "windowlickerByte spectrum");
        //int[][] windowLickerByteChannels = new int[2][windowlickerByte.length];
        //windowLickerByteChannels[0] = fromByteToInt(windowlickerByte);
        //AudioSignal audioWindowLicker = new AudioSignal(windowlickerByte, AudioConfig.getDefaultFormat());
        //System.out.println("windowlicker: " + windowlicker);


        //PlotSpectrum2D.plotSpectrum(windowlickerCut.getSpectrum(), "wav spectrum");

        //Plot2D.plotArray(Arrays.stream(sound50HzAudio[0]).filter(i->i!=0).limit(1000).toArray(), Color.BLUE);       //Plot2D.plotArray(sound50HzAudio[0], Color.BLUE);
        //Plot2D.plotArray(Arrays.stream(sound10000HzAudio[0]).filter(i->i!=0).limit(1000).toArray(), Color.RED);       //Plot2D.plotArray(sound50HzAudio[0], Color.BLUE);


        //Thread.sleep(100000);
        Assertions.assertNotEquals(ding1Audio.getFingerprint(), ding2Audio.getFingerprint());
        Assertions.fail("TODO: tests on audio analysis");
    }

    @Test
    void graphPlotterTest() throws InterruptedException {

    }


}
