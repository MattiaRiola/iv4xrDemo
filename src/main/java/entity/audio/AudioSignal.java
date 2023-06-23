package entity.audio;


import entity.math.Complex;

import javax.sound.sampled.AudioFormat;

public class AudioSignal {
    private int[][] samples;
    private Complex[][] spectrum;
    private AudioFormat format;

    public AudioSignal(int[][] samples, AudioFormat format) {
        this.samples = samples;
        this.format = format;
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
}
