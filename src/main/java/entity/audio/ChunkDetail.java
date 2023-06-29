package entity.audio;

import java.util.Arrays;
import java.util.Objects;

import static config.audio.AudioConfig.FUZ_FACTOR;

public class ChunkDetail {


    public final String name;
    public final double time;
    public final long index;
    public final long chunkHash;
    public final double[] highScores;
    public final double[] relatedFrequency;

    public ChunkDetail(double[] highScores, double[] relatedFrequencies, double time, long index, String name) {
        this.highScores = highScores;
        this.relatedFrequency = relatedFrequencies;
        this.time = time;
        this.index = index;
        this.name = name;
        this.chunkHash = getHash();
    }

    public double getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkDetail that = (ChunkDetail) o;
        return Double.compare(that.time, time) == 0 && index == that.index && chunkHash == that.chunkHash && Objects.equals(name, that.name) && Arrays.equals(highScores, that.highScores) && Arrays.equals(relatedFrequency, that.relatedFrequency);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, time, index, chunkHash);
        result = 31 * result + Arrays.hashCode(highScores);
        result = 31 * result + Arrays.hashCode(relatedFrequency);
        return result;
    }

    public boolean isInTimeTimeWindow(double time, double timeWindow) {
        return Math.abs(this.time - time) < timeWindow;
    }

    /**
     * hash evaluation using 4 points
     */
    public static long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }

    public Long getHash() {
        long p1 = (long) relatedFrequency[0];
        long p2 = (long) relatedFrequency[1];
        long p3 = (long) relatedFrequency[2];
        long p4 = (long) relatedFrequency[3];
        return hash(p1, p2, p3, p4);
    }


    @Override
    public String toString() {
        return '\'' + name + '\'' +
                String.format("(%.3f s)", time);
    }

}
