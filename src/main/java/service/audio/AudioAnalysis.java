package service.audio;

import entity.audio.AudioSignal;
import entity.math.Complex;
import utils.math.FFT;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

import static config.audio.AudioConfig.*;

public class AudioAnalysis {
    private Complex[][] results;

    public static AudioSignal readWavFile(String filePath) throws IOException, UnsupportedAudioFileException {
        File file = new File(filePath);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        int frameLength = (int) ais.getFrameLength();
        int frameSize = (int) ais.getFormat().getFrameSize();
        byte[] eightBitByteArray = new byte[frameLength * frameSize];

        int result = ais.read(eightBitByteArray);

        int channels = ais.getFormat().getChannels();
        int[][] samples = new int[channels][frameLength];

        int sampleIndex = 0;
        try {

            for (int t = 0; t < eightBitByteArray.length; ) {
                for (int channel = 0; channel < channels; channel++) {
                    int low = (int) eightBitByteArray[t];
                    t++;
                    int high = (int) eightBitByteArray[t];
                    t++;
                    int sample = getSixteenBitSample(high, low);
                    samples[channel][sampleIndex] = sample;
                }
                sampleIndex++;
            }

        } catch (Exception exp) {

            exp.printStackTrace();

        }
        return new AudioSignal(samples, ais.getFormat());

    }

    protected static int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }

    public static byte[] readAudio(String filePath, AudioFormat format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
        int read;
        byte[] buff = new byte[1024];
        while ((read = in.read(buff)) > 0) {
            out.write(buff, 0, read);
        }
        out.flush();
        return out.toByteArray();
    }

    public static Complex[][] FFT(byte[] audio) {


        final int totalSize = audio.length;

        int amountPossible = totalSize / CHUNK_SIZE;

        //When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][UPPER_LIMIT];

        //For all the chunks:
        for (int times = 0; times < amountPossible; times++) {
            Complex[] complex = new Complex[CHUNK_SIZE];
            for (int i = 0; i < CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio[(times * CHUNK_SIZE) + i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }
        return results;
    }

    public static Complex[][] FFT32bit(int[] timeDomainSignal) {
        final int totalSize = timeDomainSignal.length;

        int amountPossible = totalSize / CHUNK_SIZE;

        //When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][UPPER_LIMIT];

        //For all the chunks:
        for (int times = 0; times < amountPossible; times++) {
            Complex[] complex = new Complex[CHUNK_SIZE];
            for (int i = 0; i < CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(timeDomainSignal[(times * CHUNK_SIZE) + i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }
        return results;
    }

    /**
     * hash evaluation using 4 points
     */
    private long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }

    //Find out in which range
    public static int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq) i++;
        return i;
    }

    public static void analyse(Complex[][] audioSpectrum) throws IOException {
        FileWriter fw = new FileWriter("./points.txt");
        double[][] highScores = new double[audioSpectrum.length][UPPER_LIMIT];
        double[][] recordPoints = new double[audioSpectrum.length][UPPER_LIMIT];
        for (int t = 0; t < audioSpectrum.length; t++) {
            for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {
                //Get the magnitude:
                double mag = Math.log(audioSpectrum[t][freq].abs() + 1);

                //Find out which range we are in:
                int index = getIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highScores[t][index]) {
                    highScores[t][index] = mag;
                    recordPoints[t][index] = freq;
                }
            }
        }
    }
}
