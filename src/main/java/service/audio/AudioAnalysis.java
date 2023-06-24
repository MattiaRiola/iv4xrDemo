package service.audio;

import entity.audio.AudioFingerprint;
import entity.audio.AudioSignal;
import entity.math.Complex;
import utils.math.FFT;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static config.audio.AudioConfig.*;

public class AudioAnalysis {
    private Complex[][] results;

    public static AudioSignal readWavFile(String filePath, String name) throws IOException, UnsupportedAudioFileException {
        File file = new File(filePath);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat audioFormat = ais.getFormat();
        int frameLength = (int) ais.getFrameLength();
        int frameSize = (int) audioFormat.getFrameSize();
        byte[] eightBitByteArray = new byte[frameLength * frameSize];
        if (audioFormat.getSampleSizeInBits() != 16 || audioFormat.getSampleRate() != 44100)
            throw new UnsupportedAudioFileException("Audio " + file + " file must be 16bit and 44100Hz" + " but it is " + audioFormat);


        int result = ais.read(eightBitByteArray);

        int channels = ais.getFormat().getChannels();
        int[][] samples = new int[Math.max(channels, 2)][frameLength];

        int sampleIndex = 0;
        try {

            for (int t = 0; t < eightBitByteArray.length; ) {
                for (int channel = 0; channel < channels; channel++) {
                    if (ais.getFormat().isBigEndian()) {
                        int low = eightBitByteArray[t];
                        t++;
                        int high = eightBitByteArray[t];
                        t++;
                        int sample = getSixteenBitSample(high, low);
                        samples[channel][sampleIndex] = sample;
                    } else {
                        int low = eightBitByteArray[t];
                        //byte low =  reverseBitsByte(eightBitByteArray[t]);

                        t++;
                        int high = eightBitByteArray[t];
                        //byte high = reverseBitsByte(eightBitByteArray[t]);
                        t++;

                        int sample = getSixteenBitSample(high, low);
                        samples[channel][sampleIndex] = sample;
                    }

                }
                sampleIndex++;
            }
            if (audioFormat.getChannels() == 1)
                samples[1] = samples[0].clone();


        } catch (Exception exp) {

            exp.printStackTrace();

        }
        return new AudioSignal(name, samples, ais.getFormat());

    }

    protected static int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }

    public static byte reverseBitsByte(byte x) {
        int intSize = 8;
        byte y = 0;
        for (int position = intSize - 1; position > 0; position--) {
            y += ((x & 1) << position);
            x >>= 1;
        }
        return y;
    }

    @Deprecated
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

    public static Complex[][] FFT32bit(int[] timeDomainSignalInt) {
        byte[] timeDomainSignal = fromIntArrayToByteArray(timeDomainSignalInt);

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

    private static byte[] fromIntArrayToByteArray(int[] timeDomainSignalInt) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(timeDomainSignalInt.length * 4);
        IntBuffer intBuffer = byteBuffer.order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        intBuffer.put(timeDomainSignalInt);

        return byteBuffer.array();
    }


    //Find out in which range
    public static int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq) i++;
        return i;
    }

    public static AudioFingerprint analyse(AudioSignal audio) {
        Complex[][] audioSpectrum = audio.getSpectrum();

        if (audioSpectrum == null) {
            audioSpectrum = FFT32bit(audio.getSamples()[0]);
            audio.setSpectrum(audioSpectrum);
        }
        double[][] highScores = new double[audioSpectrum.length][RANGE.length];
        double[][] relatedFrequencies = new double[audioSpectrum.length][RANGE.length];
        for (int t = 0; t < audioSpectrum.length; t++) {
            for (int freq = LOWER_LIMIT; freq < audioSpectrum[t].length; freq++) {
                //Get the magnitude:
                double mag = Math.log(audioSpectrum[t][freq].abs() + 1);

                //Find out which range we are in:
                int index = getIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highScores[t][index]) {
                    highScores[t][index] = mag;
                    relatedFrequencies[t][index] = freq;
                }
            }

        }
        return new AudioFingerprint(highScores, relatedFrequencies);
    }
}
