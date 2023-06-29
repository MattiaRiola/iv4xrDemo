package service.audio;

import entity.audio.AudioMatch;
import entity.audio.ChunkDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.util.Set;

public class AudioAnalyserTest {

    @Test
    void bestMatchesInTimeTest() {
        double[] fakeHighScore = new double[]{0.1d, 0.2d, 0.3d, 0.4d, 0.5d};
        double[] fakeRelatedFreq = new double[]{5, 15, 100, 400};
        Set<AudioMatch> matches = Set.of(
                new AudioMatch(
                        new ChunkDetail(fakeHighScore, fakeRelatedFreq, 10d, 2, "input"),
                        Set.of(
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 1.5d, 3, "Sound1"),
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 1.5d, 3, "Sound2"),
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 2.5d, 5, "Sound1")
                        )
                ),
                new AudioMatch(
                        new ChunkDetail(fakeHighScore, fakeRelatedFreq, 10.5d, 2, "input"),
                        Set.of(
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 2.5d, 5, "Sound1")
                        )
                ),
                new AudioMatch(
                        new ChunkDetail(fakeHighScore, fakeRelatedFreq, 12.5d, 2, "input"),
                        Set.of(
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 2.5d, 5, "Sound2"),
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 1.5d, 3, "Sound2"),
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 1.0d, 2, "Sound2"),
                                new ChunkDetail(fakeHighScore, fakeRelatedFreq, 0.5d, 1, "Sound2")
                        )
                )
        );
        Set<AudioMatch> filteredMatches = AudioAnalysis.getMatchesAtTime(matches, 10.5d, 1d);
        Assertions.assertEquals(2, filteredMatches.size());

        String res = AudioAnalysis.getBestMatchAtTime(matches, 10.5d, 1d);
        Assertions.assertTrue(res.contains("Sound1"));

    }

    @Test
    void timeStampSampleTest() {
        int sampleRate = 44100;
        AudioFormat defaultAudioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
        double samplePeriod = 1d / sampleRate;
        double actualTime3 = AudioAnalysis.getTimeOfSample(3, defaultAudioFormat);
        Assertions.assertEquals(3 * samplePeriod, actualTime3);

    }
}
