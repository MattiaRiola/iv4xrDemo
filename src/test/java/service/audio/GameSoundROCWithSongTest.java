package service.audio;


import config.audio.AudioConfig;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.FileExplorer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static utils.FileExplorer.DIR_GAME_RECORDS_SAVED;
import static utils.FileExplorer.DIR_GAME_SOUNDS;
import static utils.SoundFileNames.*;

public class GameSoundROCWithSongTest {


    static List<AudioSignal> readAudios = new LinkedList<>();
    static final int MAX_THRESHOLD = 50;
    static final double TIME_WINDOW_SIZE = 1d;

    @BeforeAll
    public static void init() {
    }

    private static void initDB() throws IOException, UnsupportedAudioFileException {
        List<AudioSignal> gameSounds = FileExplorer.readAllSoundsInFolder(DIR_GAME_SOUNDS);
        readAudios.addAll(gameSounds);
        AudioAnalysis.loadAudioFingerprint(gameSounds);
    }


    @AfterAll
    public static void endingTest() {

    }

    @Test
    public void ROCDataExtraction() throws UnsupportedAudioFileException, IOException {


        AudioConfig.CHUNK_SIZE = 256;
        for (int i = 1; i < 0; i++) {
            String fileName = "ROCData_" + "CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + i + ".csv";
            AudioConfig.FUZ_FACTOR = i;
            System.out.println("Audio config:\n\t- Chunk size: " + AudioConfig.CHUNK_SIZE + "\n\t- Fuz factor: " + AudioConfig.FUZ_FACTOR);
            writeToCSV(fileName, evaluateRocPoints().toString());

        }
        AudioConfig.CHUNK_SIZE = 512;
        for (int i = 4; i < 6; i++) {
            String fileName = "ROCData_" + "CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + i + ".csv";
            AudioConfig.FUZ_FACTOR = i;
            System.out.println("Audio config:\n\t- Chunk size: " + AudioConfig.CHUNK_SIZE + "\n\t- Fuz factor: " + AudioConfig.FUZ_FACTOR);
            writeToCSV(fileName, evaluateRocPoints().toString());

        }

        AudioConfig.CHUNK_SIZE = 1024;
        for (int i = 1; i < 0; i++) {
            String postfix = "CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + i;
            String fileName = "ROCData_" + postfix + ".csv";
            AudioConfig.FUZ_FACTOR = i;
            System.out.println("Audio config:\n\t- Chunk size: " + AudioConfig.CHUNK_SIZE + "\n\t- Fuz factor: " + AudioConfig.FUZ_FACTOR);
            System.out.println(evaluateRocPoints());
            writeToCSV(fileName, evaluateRocPoints().toString());

        }

    }

    private StringBuilder evaluateRocPoints() throws IOException, UnsupportedAudioFileException {

        AudioAnalysis.resetDb();
        initDB();
        StringBuilder resStr = new StringBuilder();
        String postfix = ", CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + AudioConfig.FUZ_FACTOR;
        resStr.append(ROCData.getCSVHeader()).append(postfix).append("\n");
        for (int i = 0; i < MAX_THRESHOLD; i++) {
            ROCData ROCData = evaluateROCDataForThatThreshold(i, TIME_WINDOW_SIZE);
            // add res to the string
            resStr.append(i).append(", ").append(ROCData.toCSVRate());
            if (ROCData.getTruePositiveRate() == 0) {
                break;
            }
        }
        return resStr;
    }

    private ROCData evaluateROCDataForThatThreshold(int threshold, double timeWindowSize) throws IOException, UnsupportedAudioFileException {

        ROCData ROCData = new ROCData();

        String reachabilityTestAudioFile = "RoomReachabilityAudioRecordedWithSong.wav";
        String monsterAudioTestRecord = "MonsterAudioTestRecordedWithSong.wav";
        AudioSignal reachabilityTestAudio = FileExplorer.readWavFile(DIR_GAME_RECORDS_SAVED + reachabilityTestAudioFile, reachabilityTestAudioFile);
        AudioSignal monsterTestAudio = FileExplorer.readWavFile(DIR_GAME_RECORDS_SAVED + monsterAudioTestRecord, monsterAudioTestRecord);
        var matchesRR = AudioAnalysis.searchMatch(reachabilityTestAudio);
        var matchesM = AudioAnalysis.searchMatch(monsterTestAudio);


        // Actual : sound is present? YES

        evaluateWhenSoundIsPresent(threshold, timeWindowSize, ROCData, matchesRR, matchesM);


        // Actual : sound is present? NO

        evaluateWhenSoundIsAbsent(threshold, timeWindowSize, ROCData, matchesRR, matchesM);


        return ROCData;
    }

    private static void evaluateWhenSoundIsAbsent(int threshold, double timeWindowSize, ROCData ROCData, Set<AudioMatch> matchesRR, Set<AudioMatch> matchesM) {

        // silence

        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 16.0d, timeWindowSize),
                allSoundButOne("silence"));

        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 3.5d, timeWindowSize),
                allSoundButOne("silence"));

        // During Fire sound

        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 1.6d, timeWindowSize),
                allSoundButOne(FIRESIZZLE.getFileName()));

        // During Ding sound
        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 5.0d, timeWindowSize),
                allSoundButOne(DING1.getFileName()));

        // During Monster sound

        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 5.5d, timeWindowSize),
                allSoundButOne(MONSTERATTACK.getFileName()));
        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 17.0d, timeWindowSize),
                allSoundButOne(MONSTERATTACK.getFileName()));

        FPandTNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 17.0d, timeWindowSize),
                allSoundButOne(MONSTERATTACK.getFileName()));


    }

    private static void evaluateWhenSoundIsPresent(int threshold, double timeWindowSize, ROCData ROCData, Set<AudioMatch> matchesRR, Set<AudioMatch> matchesM) {
        // fire matchesRR

        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 1.6d, timeWindowSize),
                Set.of(FIRESIZZLE.getFileName()));

        //ding matchesRR

        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 5.0d, timeWindowSize),
                Set.of(DING1.getFileName()));

        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 10.5d, timeWindowSize),
                Set.of(DING1.getFileName()));

        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 13.25d, timeWindowSize),
                Set.of(DING1.getFileName()));


        // monster matchesM

        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 3.0d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 5.5d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 7.5d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
        TPandFNevaluation(
                threshold,
                ROCData,
                AudioAnalysis.getMatchesAtTime(matchesM, 17.0d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
    }

    /**
     * @param threshold      min match required to be considered a match
     * @param ROCData        this object will be modified by the method adding false negative or true positive
     * @param match          the set of matches to be evaluated
     * @param expectedSounds the sounds that are expected to be present in the match
     */
    private static void TPandFNevaluation(int threshold, ROCData ROCData, Set<AudioMatch> match, Set<String> expectedSounds) {
        Map<String, Integer> stat = AudioAnalysis.getMatchStat(match);
        for (String expectedSound : expectedSounds) {
            int numOfMatchesFound = stat.getOrDefault(expectedSound, 0);
            if (numOfMatchesFound < threshold)
                ROCData.addFalseNegative();
            else
                ROCData.addTruePositive();
        }
    }

    /**
     * @param threshold        min match required to be considered a match
     * @param ROCData          this object will be modified by the method adding true negative or false positive
     * @param match            the set of matches to be evaluated
     * @param unexpectedSounds the sound that is unexpected to be present in the match
     */
    private static void FPandTNevaluation(int threshold, ROCData ROCData, Set<AudioMatch> match, Set<String> unexpectedSounds) {
        Map<String, Integer> stat = AudioAnalysis.getMatchStat(match);
        for (String unexpectedSound : unexpectedSounds) {
            int matchFound = stat.getOrDefault(unexpectedSound, 0);
            if (matchFound < threshold)
                ROCData.addTrueNegative();
            else
                ROCData.addFalsePositive();
        }

    }

    private static Set<String> allSoundButOne(String soundToBeIgnored) {
        return readAudios.stream()
                .map(AudioSignal::getName)
                .filter(s -> !s.equals(soundToBeIgnored)).collect(Collectors.toSet());
    }


    public void writeToCSV(String filename, String data) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
