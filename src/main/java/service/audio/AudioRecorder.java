package service.audio;


import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static config.audio.AudioConfig.getDefaultFormat;

@Deprecated()
//In order to use this class adjust settings to record the correct audio channel
public class AudioRecorder {

    private final AudioFormat format;
    private boolean running = false;


    private ByteArrayOutputStream recordOut = new ByteArrayOutputStream();
    private final byte[] buffer = new byte[1024];
    private Thread recordThread;

    public AudioRecorder() {
        super();
        this.format = getDefaultFormat();
    }

    public void run() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        final TargetDataLine line;
        try {
            //Mixer.Info infoMixer = Arrays.stream(AudioSystem.getMixerInfo()).filter(i-> i.getName().toLowerCase().contains("stereo mix")).findFirst().get();

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        running = true;

        try {
            while (running) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) {
                    recordOut.write(buffer, 0, count);
                }
            }
            recordOut.close();
        } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
        }
    }

    public void startRecording() {
        recordThread = new Thread(this::run);
        recordThread.start();
    }

    public void stopRecording() throws InterruptedException {
        running = false;
        recordThread.join(10000);
    }

    public ByteArrayOutputStream getRecordOut() {
        return recordOut;
    }

}
