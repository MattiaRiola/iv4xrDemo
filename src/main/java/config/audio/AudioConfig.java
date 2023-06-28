package config.audio;

import javax.sound.sampled.AudioFormat;

public class AudioConfig {

    public static final int LOWER_LIMIT = 10;
    public static final int UPPER_LIMIT = 44100;
    public static final int[] RANGE = new int[]{40, 80, 120, 180, UPPER_LIMIT + 1};
    public static int FUZ_FACTOR = 2;
    /**
     * with 16-bit samples, at 44,100 Hz, one second of such sound will be 44,100 samples * 2 bytes * 2 channels ≈ 176 kB.
     * If we pick 4 kB for the size of a chunk, we will have 44 chunks of data to analyze in every second of the song.
     * 2 bytes * 44100 samples = 88200 [bytes] -> 1sec I've got 88200 bytes per channel -> total 176400 bytes
     * 1s : 0.2s = 88200 : x -> x = 88200 * 0.2 / 1 = 17640
     * <p>
     * 1KB: 1s : x = 88200 : 1024 -> x = 1 * 1024 / 88200 = 0.0116 -> 86 chunks al secondo
     * 2KB: 1s : x = 88200 : 1024 -> x = 1 * 2048 / 88200 = 0.0232 -> 43 chunks al secondo
     * 4KB: 1s : x = 88200 : 1024 -> x = 1 * 2048 / 88200 = 0.0232 -> 22 chunks al secondo
     */
    public static int CHUNK_SIZE = 1024 * 8;


    private static int BYTES_IN_ONE_SECONDS = 88200; //per channel

    public static AudioFormat getDefaultFormat() {
        float sampleRate = UPPER_LIMIT;
        int sampleSizeInBits = 16;
        int channels = 1; //mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public static void setChunkSize(ChunkSize size) {
        switch (size) {
            case SMALL:
                CHUNK_SIZE = 1024 * 8; //1KB ->
                break;
            case MEDIUM:
                CHUNK_SIZE = 1024 * 8 * 2;
                break;
            case LARGE:
                CHUNK_SIZE = 1024 * 8 * 4;
                break;
        }
    }

    public static double getChunkDuration(AudioFormat format) {
        return CHUNK_SIZE / (format.getSampleRate() * format.getSampleSizeInBits());
    }
}
