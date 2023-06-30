package entity.audio;


import config.audio.AudioConfig;
import entity.math.Complex;
import service.audio.AudioAnalysis;

import javax.sound.sampled.AudioFormat;
import java.util.Map;
import java.util.Set;

public class AudioSignal {
    private String name;
    private short[] samples;
    private Complex[][] spectrogram;
    private AudioFormat format;

    private Map<Long, Set<ChunkDetail>> fingerprint;


    public AudioSignal(String name, short[] samples, AudioFormat format) {
        this.name = name;
        this.samples = samples;
        this.format = format;
        this.spectrogram = AudioAnalysis.evaluateSpectrogram(samples);
        this.fingerprint = AudioAnalysis.analyse(this);
        System.out.println(
                name + " loaded:" + samples.length +
                        "\n\t- samples" + " ( = " + AudioAnalysis.getTimeOfSample(samples.length, format) + " seconds)" +
                        "\n\t- generating " + fingerprint.size() + " fingerprints" +
                        "\n\t- using a " + spectrogram.length + " chunks long spectrum" +
                        "\n\t- with fuz factor of: " + AudioConfig.FUZ_FACTOR +
                        String.format("\n\t- minimum chunk duration: %.3f  seconds", AudioConfig.getChunkDuration(this.format))
        );
    }

    public String getName() {
        return name;
    }

    public Map<Long, Set<ChunkDetail>> getFingerprint() {
        return fingerprint;
    }

    public short[] getSamples() {
        return samples;
    }

    public void setSamples(short[] samples) {
        this.samples = samples;
    }

    public Complex[][] getSpectrogram() {
        return spectrogram;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public void setFormat(AudioFormat format) {
        this.format = format;
    }

    /**
     * Prints the duration of the audio file
     */
    public double getAudioDuration() {
        return (double) samples.length / format.getFrameRate();
    }

    @Override
    public String toString() {
        return "AudioSignal{" +
                "name='" + name + '\'' +
                " ( tot duration: " + getAudioDuration() + " seconds ) " +
                ", format=" + format +
                '}';
    }

}
