package utils;

import entity.audio.AudioSignal;
import org.apache.commons.lang3.time.StopWatch;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileExplorer {

    public static Set<String> listFilesUsingFilesList(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toSet());
        }
    }

    public static List<AudioSignal> readAllSoundsInFolder(String dir) throws IOException, UnsupportedAudioFileException {
        Set<String> soundFileNames = FileExplorer.listFilesUsingFilesList(dir);
        List<AudioSignal> res = new LinkedList<>();
        for (String soundFileName : soundFileNames) {

            AudioSignal audio = readWavFile(dir + soundFileName, soundFileName);

            res.add(audio);
        }
        return res;
    }

    public static AudioSignal readWavFile(String filePath, String name) throws IOException, UnsupportedAudioFileException {
        File file = new File(filePath);
        System.out.println("Reading " + name + " ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.reset();
        stopWatch.start();

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

        AudioSignal res = new AudioSignal(name, samples, ais.getFormat());
        stopWatch.stop();
        long ms = stopWatch.getTime();
        System.out.println("finished in " + ms + " ms : " + res);
        return res;

    }

    protected static int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }
}
