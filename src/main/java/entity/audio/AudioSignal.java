package entity.audio;


import entity.math.Complex;
import service.audio.AudioAnalysis;

import javax.sound.sampled.AudioFormat;

public class AudioSignal {
    private String name;
    private int[][] samples;
    private Complex[][] spectrum;
    private AudioFormat format;

    private AudioFingerprint fingerprint;


    public AudioSignal(String name, int[][] samples, AudioFormat format) {
        this.name = name;
        this.samples = samples;
        this.format = format;
        this.spectrum = AudioAnalysis.FFT32bit(samples[0]);
        this.fingerprint = AudioAnalysis.analyse(this);
    }

    public String getName() {
        return name;
    }

    public AudioFingerprint getFingerprint() {
        return fingerprint;
    }

    public int[][] getSamples() {
        return samples;
    }

    public void setSamples(int[][] samples) {
        this.samples = samples;
    }

    public Complex[][] getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Complex[][] spectrum) {
        this.spectrum = spectrum;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public void setFormat(AudioFormat format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "AudioSignal{" +
                ", format=" + format +
                '}';
    }

    public String getSamplesString() {
        String str = "(";
        for (int i = 0; i < samples.length; i++) {
            str = str.concat("channel " + i + ": # samples: " + samples[i].length + ",");
        }
        str = str.concat(")");
        return str;
    }
}
