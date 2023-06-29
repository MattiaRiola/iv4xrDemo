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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileExplorer {

    public static final String DIR_BASE_AUDIO_RES = "src/test/resources/audio/";
    public static final String DIR_GAME_SOUNDS = DIR_BASE_AUDIO_RES + "game/sounds/";
    public static final String DIR_SONGS = DIR_BASE_AUDIO_RES + "songs/";
    public static final String DIR_SONG_RECORDS = DIR_SONGS + "records/";
    public static final String DIR_SONG_DB = DIR_SONGS + "db/";

    public static final String DIR_MANUALLY_PLAYED = DIR_BASE_AUDIO_RES + "ManuallyPlayed/";
    public static final String DIR_GAME_RECORDS_SAVED = DIR_BASE_AUDIO_RES + "game/saved/";
    public static final String DIR_GAME_RECORDS = DIR_BASE_AUDIO_RES + "game/records/";
    public static final boolean DELETE_AUDIO_ONCE_FINISHED = false;


    public static List<String> listFilesUsingFilesList(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    public static void deleteFilesInFolder(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            int i = 0;
            stream
                    .filter(file -> !Files.isDirectory(file))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            System.out.println("Deleted " + p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        }
    }

    public static List<AudioSignal> readAllSoundsInFolder(String dir) throws IOException, UnsupportedAudioFileException {
        System.out.println("#######################################");
        System.out.println("READING FILE IN " + dir);
        List<String> soundFileNames = FileExplorer.listFilesUsingFilesList(dir);
        List<AudioSignal> res = new LinkedList<>();
        for (String soundFileName : soundFileNames) {

            AudioSignal audio = readWavFile(dir + soundFileName, soundFileName);

            res.add(audio);
        }
        System.out.println("#######################################");
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
        if (audioFormat.getSampleSizeInBits() != 16 || audioFormat.getSampleRate() != 44100)
            throw new UnsupportedAudioFileException("Audio " + file + " file must be 16bit and 44100Hz" + " but it is " + audioFormat);

        byte[] eightBitByteArray = new byte[frameLength * frameSize];
        //int byteRead = ais.read(eightBitByteArray);

        byte[] byteRead = ais.readAllBytes();
        System.out.println("byte read: " + byteRead.length);
        int channels = ais.getFormat().getChannels();
        if (channels != 1)
            throw new UnsupportedAudioFileException("Audio " + file + " file must be mono" + " but it is " + ais.getFormat());
        //read only one channel
        boolean isBigEndian = ais.getFormat().isBigEndian();
        short[] samples = getShortsFromByteArray(byteRead, isBigEndian);

        //from java array to Stream


        AudioSignal res = new AudioSignal(name, samples, ais.getFormat());
        stopWatch.stop();
        long ms = stopWatch.getTime();
        System.out.println("finished in " + ms + " ms : " + res);
        return res;

    }

    protected static short[] getShortsFromByteArray(byte[] eightBitByteArray, boolean isBigEndian) {
        if (eightBitByteArray.length % 2 != 0)
            throw new IllegalArgumentException("byte array must be even, otherwise it can't be split in shorts array");

        short[] samples = new short[eightBitByteArray.length / 2];
        int sampleIndex = 0;
        try {
            for (int t = 0; t < eightBitByteArray.length; ) {
                if (isBigEndian) {
                    byte high = eightBitByteArray[t];
                    t++;
                    byte low = eightBitByteArray[t];
                    t++;

                    short sample = getShortFromTwoBytes(high, low);
                    samples[sampleIndex] = sample;
                } else {
                    byte low = eightBitByteArray[t];
                    //byte low =  reverseBitsByte(eightBitByteArray[t]);

                    t++;
                    byte high = eightBitByteArray[t];
                    //byte high = reverseBitsByte(eightBitByteArray[t]);
                    t++;

                    short sample = getShortFromTwoBytes(high, low);
                    samples[sampleIndex] = sample;
                }
                sampleIndex++;
            }


        } catch (Exception exp) {

            exp.printStackTrace();

        }
        return samples;
    }


    protected static short getShortFromTwoBytes(byte high, byte low) {
        return (short) ((high << 8) + (low & 0x0f));
    }
}
