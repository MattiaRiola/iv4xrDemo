package service.audio;

import javax.sound.sampled.AudioFormat;

public class AudioConfig {

    public static final int LOWER_LIMIT = 10;
    public static final int UPPER_LIMIT = 44100;
    public static final int[] RANGE = new int[]{40, 80, 120, 180, UPPER_LIMIT + 1};
    public static final int FUZ_FACTOR = 2;
    /**
     * with 16-bit samples, at 44,100 Hz, one second of such sound will be 44,100 samples * 2 bytes * 2 channels ≈ 176 kB.
     * If we pick 4 kB for the size of a chunk, we will have 44 chunks of data to analyze in every second of the song.
     */
    public static int CHUNK_SIZE = 1024 * 8 * 2; //2KB (0.5seconds)

    public static AudioFormat getFormat() {
        float sampleRate = UPPER_LIMIT;
        int sampleSizeInBits = 8;
        int channels = 1; //mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
