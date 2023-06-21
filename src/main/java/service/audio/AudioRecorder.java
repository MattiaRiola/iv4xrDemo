package service.audio;


import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public class AudioRecorder {
    private final AudioFormat format;
    private boolean running = false;
    private byte[] buffer;
    /**
     * with 16-bit samples, at 44,100 Hz, one second of such sound will be 44,100 samples * 2 bytes * 2 channels ≈ 176 kB.
     * If we pick 4 kB for the size of a chunk, we will have 44 chunks of data to analyze in every second of the song.
     */
    private int CHUNK_SIZE = 1024*8*4; //4KB

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    public AudioRecorder(AudioFormat format) {
        this.format = format;
    }
    public AudioRecorder() {
        this.format = getFormat();
    }
    private AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 8;
        int channels = 1; //mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void startRecording() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        // In another thread I start:

        running = true;

        try {
            while (running) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            }
            out.close();
        } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
        }
    }

    void FFT(){
        byte audio[] = out.toByteArray();

        final int totalSize = audio.length;

        int amountPossible = totalSize/CHUNK_SIZE;

//When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

//For all the chunks:
        for(int times = 0;times < amountPossible; times++) {
            Complex[] complex = new Complex[CHUNK_SIZE];
            for(int i = 0;i < CHUNK_SIZE;i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio[(times*CHUNK_SIZE)+i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }
    }
}
