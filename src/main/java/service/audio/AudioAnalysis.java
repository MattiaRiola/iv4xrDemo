package service.audio;

import config.audio.AudioConfig;
import config.audio.ChunkSize;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import entity.audio.ChunkDetail;
import entity.math.Complex;
import utils.math.FFT;

import javax.sound.sampled.AudioFormat;
import java.util.*;
import java.util.stream.Collectors;

import static config.audio.AudioConfig.*;

public class AudioAnalysis {
    private static final Map<Long, Set<ChunkDetail>> fingerprintsDb = new HashMap<>();

    public static void loadAudioFingerprint(List<AudioSignal> audios) {

        for (AudioSignal audio : audios) {
            Map<Long, Set<ChunkDetail>> fingerprintMap = audio.getFingerprint();
            fingerprintMap.forEach((k, chunkDetails) ->
            {
                for (ChunkDetail chunkDetail : chunkDetails) {
                    Set<ChunkDetail> fingerprintsList = fingerprintsDb
                            .getOrDefault(chunkDetail.chunkHash, new TreeSet<>(Comparator.comparing(ChunkDetail::getTime)));
                    fingerprintsList.add(chunkDetail);
                    fingerprintsDb.put(chunkDetail.chunkHash, fingerprintsList);
                }
            });
        }

    }


    public static Map<String, Set<AudioMatch>> searchMatch(AudioSignal input) {
        System.out.println("Searching match for: " + input.getName());
        Map<String, Set<AudioMatch>> matchesPoints = new HashMap<>();
        Map<Long, Set<ChunkDetail>> inputFingerprintMap = input.getFingerprint();

        //for each hash in the fingerprint of inputSound
        for (Map.Entry<Long, Set<ChunkDetail>> inputFingerprintsEntry : inputFingerprintMap.entrySet()) {
            var inputChunkHash = inputFingerprintsEntry.getKey();
            var inputChunks = inputFingerprintsEntry.getValue();
            if (fingerprintsDb.containsKey(inputChunkHash)) {
                //found a match in the db
                Set<ChunkDetail> dbChunks = fingerprintsDb.get(inputFingerprintsEntry.getKey());

                //make match points
                for (ChunkDetail dbChunk : dbChunks) {
                    for (ChunkDetail inputChunk : inputChunks) {
                        Set<AudioMatch> matches = matchesPoints.getOrDefault(dbChunk.name, new TreeSet<>(Comparator.comparing(AudioMatch::getRecordTime)));
                        //add matches
                        AudioMatch match = new AudioMatch(dbChunk.name, inputChunk.time, dbChunk.time, dbChunk.chunkHash);
                        matches.add(match);
                        //System.out.println("Match: "+match);

                        matchesPoints.put(dbChunk.name, matches);
                    }

                }
            }
        }
        printMatches(matchesPoints);
        return matchesPoints;
    }

    public static String getBestMatch(Map<String, Set<AudioMatch>> matches) {
        return matches.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .get().getKey();
    }

    /**
     * @param matches        match map for that audio
     * @param time           interested time when the best match is requested
     * @param timeWindowSize the size of time window analysed (time must be between time-timeWindowSize and time+timeWindowSize)
     * @return the audio with higher match rate in that time window
     */
    public static String getBestMatchAtTime(Map<String, Set<AudioMatch>> matches, long time, long timeWindowSize) {
        return matches.entrySet().stream()
                .peek(e ->
                        e.setValue(
                                e.getValue().stream().filter(m -> m.getRecordTime() == time).collect(Collectors.toSet())
                        )
                )
                .filter(e -> e.getValue().size() > 0)
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                //TODO: check if the time is in the window
                .get().getKey();
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


    /**
     * this is only for 16bit audio signals (in fact short array is used)
     *
     * @param timeDomainSignal array of 16bit audio samples
     * @return spectrogram of the audio signal
     */
    public static Complex[][] evaluateSpectrogram(short[] timeDomainSignal) {

        final int totalSamples = timeDomainSignal.length;

        int amountPossibleChunks = totalSamples / CHUNK_SIZE;

        //When turning into frequency domain we'll need complex numbers:
        Complex[][] spectrogram = new Complex[amountPossibleChunks][UPPER_LIMIT];

        //For all the chunks:
        for (int chunkIndex = 0; chunkIndex < amountPossibleChunks; chunkIndex++) {
            Complex[] timeDomainComplex = convertReShortsInComplexes(timeDomainSignal, chunkIndex, CHUNK_SIZE);
            //Perform FFT analysis on the chunk:
            spectrogram[chunkIndex] = FFT.fft(timeDomainComplex);
        }
        return spectrogram;
    }

    /**
     * Real short numbers -> Complex numbers (with Im = 0)
     *
     * @param timeDomainSignal array of 16bit audio samples
     * @param chunkIndex       index of the chunk
     * @param chunkSize        size of the chunk
     * @return complex array of the samples in time domain (with imaginary part as 0) of the size of chunkSize
     */
    private static Complex[] convertReShortsInComplexes(short[] timeDomainSignal, int chunkIndex, int chunkSize) {
        Complex[] timeDomainComplex = new Complex[chunkSize];
        for (int i = 0; i < chunkSize; i++) {
            //Put the time domain data into a complex number with imaginary part as 0:
            timeDomainComplex[i] = new Complex(timeDomainSignal[(chunkIndex * chunkSize) + i], 0);
        }
        return timeDomainComplex;
    }


    //Find out in which range
    public static int getFrequencyRangeIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq) i++;
        return i;
    }

    public static Map<Long, Set<ChunkDetail>> analyse(AudioSignal audio) {
        Complex[][] audioSpectrogram = audio.getSpectrogram();
        Map<Long, Set<ChunkDetail>> fingerprintsMap = new HashMap<>();
        assert audioSpectrogram != null;

        double[][] highScores = new double[audioSpectrogram.length][RANGE.length];
        double[][] relatedFrequencies = new double[audioSpectrogram.length][RANGE.length];

        for (int chunkIndex = 0; chunkIndex < audioSpectrogram.length; chunkIndex++) {
            for (int freq = LOWER_LIMIT; freq < audioSpectrogram[chunkIndex].length; freq++) {
                //Get the magnitude:
                double mag = Math.log(audioSpectrogram[chunkIndex][freq].abs() + 1);

                //Find out which range we are in:
                int frequencyIndex = getFrequencyRangeIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highScores[chunkIndex][frequencyIndex]) {
                    highScores[chunkIndex][frequencyIndex] = mag;
                    relatedFrequencies[chunkIndex][frequencyIndex] = freq;

                }
            }

            ChunkDetail chunkDetail = new ChunkDetail(
                    highScores[chunkIndex],
                    relatedFrequencies[chunkIndex],
                    getTimestampOfChunk(chunkIndex, audio.getFormat()),
                    chunkIndex,
                    audio.getName()
            );
            Set<ChunkDetail> fingerprints = fingerprintsMap.getOrDefault(chunkDetail.getHash(), new TreeSet<>(Comparator.comparing(ChunkDetail::getTime)));
            fingerprints.add(chunkDetail);
            fingerprintsMap.put(chunkDetail.getHash(), fingerprints);
        }
        return fingerprintsMap;
    }

    public static double getTimestampOfSample(long sampleIndex, AudioFormat format) {
        //return (((double) sampleIndex / format.getSampleRate()) * 1000);
        return ((double) sampleIndex / format.getSampleRate());
    }

    public static double getTimestampOfChunk(long chunkIndex, AudioFormat format) {
        return chunkIndex * AudioConfig.getChunkDuration(format);
    }

    public static void changeConfig(Integer fuzFactor, Integer chunkSize) {
        AudioConfig.FUZ_FACTOR = fuzFactor;
        AudioConfig.CHUNK_SIZE = chunkSize;
    }

    public static void changeConfigBySeconds(int fuzFactor, ChunkSize chunkSize) {
        AudioConfig.FUZ_FACTOR = fuzFactor;
        setChunkSize(chunkSize);
    }

    public static void printMatches(Map<String, Set<AudioMatch>> matches) {
        matches.forEach((key, value) -> {

            System.out.println("Matches for: " + value.stream().map(AudioMatch::getName).distinct().collect(Collectors.toList()));
            value.forEach(
                    m -> System.out.println("\t" + m)
            );
        });
    }

    /**
     * debug purpose function that order the chunks
     *
     * @param audios
     * @return
     */
    public static Map<String, List<ChunkDetail>> getAudiosWithOrderedChunksDetails(List<AudioSignal> audios) {
        Map<String, List<ChunkDetail>> audiosWithOrderedChunkDetails = new LinkedHashMap<>();
        for (AudioSignal audio : audios) {
            List<ChunkDetail> chunksDetailList = audio.getFingerprint().values()
                    .stream()
                    .flatMap(Collection::stream)
                    .sorted(Comparator.comparingDouble(ChunkDetail::getTime).reversed())
                    .collect(Collectors.toList());

            audiosWithOrderedChunkDetails.put(audio.getName(), chunksDetailList);

        }
        return audiosWithOrderedChunkDetails;
    }

    /**
     * old code to read audio with specific format using byte array (src stack overflow)
     */
//    @Deprecated
//    public static byte[] readAudio(String filePath, AudioFormat format) throws IOException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
//        int read;
//        byte[] buff = new byte[1024];
//        while ((read = in.read(buff)) > 0) {
//            out.write(buff, 0, read);
//        }
//        out.flush();
//        return out.toByteArray();
//    }

    /**
     * old code imported from the internet
     */
//    @Deprecated(forRemoval = true)
//    public static Complex[][] FFT(byte[] audio) {
//
//
//        final int totalSize = audio.length;
//
//        int amountPossible = totalSize / CHUNK_SIZE;
//
//        //When turning into frequency domain we'll need complex numbers:
//        Complex[][] results = new Complex[amountPossible][UPPER_LIMIT];
//
//        //For all the chunks:
//        for (int times = 0; times < amountPossible; times++) {
//            Complex[] complex = new Complex[CHUNK_SIZE];
//            for (int i = 0; i < CHUNK_SIZE; i++) {
//                //Put the time domain data into a complex number with imaginary part as 0:
//                complex[i] = new Complex(audio[(times * CHUNK_SIZE) + i], 0);
//            }
//            //Perform FFT analysis on the chunk:
//            results[times] = FFT.fft(complex);
//        }
//        return results;
//    }
}
