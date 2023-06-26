package service.audio;

import config.audio.AudioConfig;
import config.audio.ChunkSize;
import entity.audio.AudioFingerprint;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import entity.math.Complex;
import utils.math.FFT;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static config.audio.AudioConfig.*;

public class AudioAnalysis {
    private static final Map<Long, AudioFingerprint> fingerprintsDb = new HashMap<>();

    public static void loadAudioFingerprint(List<AudioSignal> audios) {

        for (AudioSignal audio : audios) {
            Map<Long, AudioFingerprint> fingerprints = audio.getFingerprint();
            fingerprintsDb.putAll(fingerprints);
        }

    }


    public static Map<String, List<AudioMatch>> searchMatch(AudioSignal input) {
        System.out.println("Searching match for: " + input.getName());
        Map<String, List<AudioMatch>> matchesPoints = new HashMap<>();
        Map<Long, AudioFingerprint> inputFingerprints = input.getFingerprint();
        for (Map.Entry<Long, AudioFingerprint> recordFingerprint : inputFingerprints.entrySet()) {
            if (fingerprintsDb.containsKey(recordFingerprint.getKey())) {
                AudioFingerprint dbFingerprint = fingerprintsDb.get(recordFingerprint.getKey());
                List<AudioMatch> matches = matchesPoints.getOrDefault(dbFingerprint.name, new ArrayList<>());
                //add matches
                AudioMatch match = new AudioMatch(dbFingerprint.name, recordFingerprint.getValue().time, dbFingerprint.time);
                matches.add(match);
                //System.out.println("Match: "+match);
                List<AudioMatch> orderedMatches = matches.stream().sorted(Comparator.comparing(AudioMatch::getRecordTime)).collect(Collectors.toList());
                matchesPoints.put(dbFingerprint.name, orderedMatches);
            }
        }
        return matchesPoints;
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

    public static Map<Long, AudioFingerprint> analyse(AudioSignal audio) {
        Complex[][] audioSpectrum = audio.getSpectrum();
        Map<Long, AudioFingerprint> fingerprintsMap = new HashMap<>();
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
            AudioFingerprint audioFingerprint = new AudioFingerprint(
                    highScores[t],
                    relatedFrequencies[t],
                    getTimestamp(t, audio.getFormat()),
                    t,
                    audio.getName()
            );
            fingerprintsMap.put(audioFingerprint.getHash(), audioFingerprint);
        }
        return fingerprintsMap;
    }

    public static double getTimestamp(long sampleIndex, AudioFormat format) {
        return (((double) sampleIndex / format.getSampleRate()) * 1000);
    }

    public static void changeConfig(Integer fuzFactor, Integer chunkSize) {
        AudioConfig.FUZ_FACTOR = fuzFactor;
        AudioConfig.CHUNK_SIZE = chunkSize;
    }

    public static void changeConfigBySeconds(int fuzFactor, ChunkSize chunkSize) {
        AudioConfig.FUZ_FACTOR = fuzFactor;
        setChunkSize(chunkSize);
    }

    public static void printMatches(Map<String, List<AudioMatch>> matches) {
        matches.forEach((key, value) -> {
            System.out.println("Matches for: " + value.get(0).getName());
            value.forEach(
                    m -> System.out.println("\t" + m)
            );
        });
    }
}
