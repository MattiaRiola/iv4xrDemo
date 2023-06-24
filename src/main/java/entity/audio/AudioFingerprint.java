package entity.audio;

import java.util.Arrays;

import static config.audio.AudioConfig.FUZ_FACTOR;

public class AudioFingerprint {

    double[][] highScores;
    double[][] relatedFrequency;

    public AudioFingerprint(double[][] highScores, double[][] relatedFrequencies) {
        this.highScores = highScores;
        this.relatedFrequency = relatedFrequencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioFingerprint that = (AudioFingerprint) o;
        return Arrays.equals(highScores, that.highScores) && Arrays.equals(relatedFrequency, that.relatedFrequency);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(highScores);
        result = 31 * result + Arrays.hashCode(relatedFrequency);
        return result;
    }

    /**
     * hash evaluation using 4 points
     */
    public static long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }
}
