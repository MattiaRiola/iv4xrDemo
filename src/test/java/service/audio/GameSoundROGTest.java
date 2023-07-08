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

public class GameSoundROGTest {


    static List<AudioSignal> readAudios = new LinkedList<>();
    static final int MAX_THRESHOLD = 100;
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
    public void ROGDataExtraction() throws UnsupportedAudioFileException, IOException {


        AudioConfig.CHUNK_SIZE = 256;
        for (int i = 1; i < 5; i++) {
            String fileName = "ROGData_" + "CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + i + ".csv";
            AudioConfig.FUZ_FACTOR = i;
            System.out.println("Audio config:\n\t- Chunk size: " + AudioConfig.CHUNK_SIZE + "\n\t- Fuz factor: " + AudioConfig.FUZ_FACTOR);
            writeToCSV(fileName, evaluateRogPoints().toString());

        }
        AudioConfig.CHUNK_SIZE = 512;
        for (int i = 1; i < 5; i++) {
            String fileName = "ROGData_" + "CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + i + ".csv";
            AudioConfig.FUZ_FACTOR = i;
            System.out.println("Audio config:\n\t- Chunk size: " + AudioConfig.CHUNK_SIZE + "\n\t- Fuz factor: " + AudioConfig.FUZ_FACTOR);
            writeToCSV(fileName, evaluateRogPoints().toString());

        }

        AudioConfig.CHUNK_SIZE = 1024;
        for (int i = 1; i < 5; i++) {
            String fileName = "ROGData_" + "CHUNKSIZE_" + AudioConfig.CHUNK_SIZE + "_FUZ_FACTOR_" + i + ".csv";
            AudioConfig.FUZ_FACTOR = i;
            System.out.println("Audio config:\n\t- Chunk size: " + AudioConfig.CHUNK_SIZE + "\n\t- Fuz factor: " + AudioConfig.FUZ_FACTOR);
            System.out.println(evaluateRogPoints());
            writeToCSV(fileName, evaluateRogPoints().toString());

        }

    }

    private StringBuilder evaluateRogPoints() throws IOException, UnsupportedAudioFileException {

        AudioAnalysis.resetDb();
        initDB();
        StringBuilder resStr = new StringBuilder();
        resStr.append(ROGData.getCSVHeader()).append("\n");
        for (int i = 0; i < MAX_THRESHOLD; i++) {
            ROGData ROGData = evaluateROGDataForThatThreshold(i, TIME_WINDOW_SIZE);
            // add res to the string
            resStr.append(i).append(", ").append(ROGData.toCSVRate());
            if (ROGData.getTruePositiveRate() == 0) {
                break;
            }
        }
        return resStr;
    }

    private ROGData evaluateROGDataForThatThreshold(int threshold, double timeWindowSize) throws IOException, UnsupportedAudioFileException {

        ROGData ROGData = new ROGData();

        String reachabilityTestAudioFile = "RoomReachabilityAudioRecorded.wav";
        String monsterAudioTestRecord = "MonsterAudioTestRecorded.wav";
        AudioSignal reachabilityTestAudio = FileExplorer.readWavFile(DIR_GAME_RECORDS_SAVED + reachabilityTestAudioFile, reachabilityTestAudioFile);
        AudioSignal monsterTestAudio = FileExplorer.readWavFile(DIR_GAME_RECORDS_SAVED + monsterAudioTestRecord, monsterAudioTestRecord);
        var matchesRR = AudioAnalysis.searchMatch(reachabilityTestAudio);
        var matchesM = AudioAnalysis.searchMatch(monsterTestAudio);


        // Actual : sound is present? YES

        evaluateWhenSoundIsPresent(threshold, timeWindowSize, ROGData, matchesRR, matchesM);


        // Actual : sound is present? NO

        evaluateWhenSoundIsAbsent(threshold, timeWindowSize, ROGData, matchesRR, matchesM);


        return ROGData;
    }

    private static void evaluateWhenSoundIsAbsent(int threshold, double timeWindowSize, ROGData rogData, Set<AudioMatch> matchesRR, Set<AudioMatch> matchesM) {

        // silence

        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 16.0d, timeWindowSize),
                allSoundButOne("silence"));

        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 3.5d, timeWindowSize),
                allSoundButOne("silence"));

        // During Fire sound

        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 1.6d, timeWindowSize),
                allSoundButOne(FIRESIZZLE.getFileName()));

        // During Ding sound
        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 5.0d, timeWindowSize),
                allSoundButOne(DING1.getFileName()));

        // During Monster sound

        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesM, 5.5d, timeWindowSize),
                allSoundButOne(MONSTERATTACK.getFileName()));
        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesM, 17.0d, timeWindowSize),
                allSoundButOne(MONSTERATTACK.getFileName()));

        FPandTNevaluation(
                threshold,
                rogData,
                AudioAnalysis.getMatchesAtTime(matchesM, 17.0d, timeWindowSize),
                allSoundButOne(MONSTERATTACK.getFileName()));


    }

    private static void evaluateWhenSoundIsPresent(int threshold, double timeWindowSize, ROGData ROGData, Set<AudioMatch> matchesRR, Set<AudioMatch> matchesM) {
        // fire matchesRR

        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 1.6d, timeWindowSize),
                Set.of(FIRESIZZLE.getFileName()));

        //ding matchesRR

        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 5.0d, timeWindowSize),
                Set.of(DING1.getFileName()));

        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 10.5d, timeWindowSize),
                Set.of(DING1.getFileName()));

        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesRR, 13.25d, timeWindowSize),
                Set.of(DING1.getFileName()));


        // monster matchesM

        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesM, 3.0d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesM, 5.5d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesM, 7.5d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
        TPandFNevaluation(
                threshold,
                ROGData,
                AudioAnalysis.getMatchesAtTime(matchesM, 17.0d, timeWindowSize),
                Set.of(MONSTERATTACK.getFileName()));
    }

    /**
     * @param threshold      min match required to be considered a match
     * @param ROGData        this object will be modified by the method adding false negative or true positive
     * @param match          the set of matches to be evaluated
     * @param expectedSounds the sounds that are expected to be present in the match
     */
    private static void TPandFNevaluation(int threshold, ROGData ROGData, Set<AudioMatch> match, Set<String> expectedSounds) {
        Map<String, Integer> stat = AudioAnalysis.getMatchStat(match);
        for (String expectedSound : expectedSounds) {
            int numOfMatchesFound = stat.getOrDefault(expectedSound, 0);
            if (numOfMatchesFound < threshold)
                ROGData.addFalseNegative();
            else
                ROGData.addTruePositive();
        }
    }

    /**
     * @param threshold        min match required to be considered a match
     * @param ROGData          this object will be modified by the method adding true negative or false positive
     * @param match            the set of matches to be evaluated
     * @param unexpectedSounds the sound that is unexpected to be present in the match
     */
    private static void FPandTNevaluation(int threshold, ROGData ROGData, Set<AudioMatch> match, Set<String> unexpectedSounds) {
        Map<String, Integer> stat = AudioAnalysis.getMatchStat(match);
        for (String unexpectedSound : unexpectedSounds) {
            int matchFound = stat.getOrDefault(unexpectedSound, 0);
            if (matchFound < threshold)
                ROGData.addTrueNegative();
            else
                ROGData.addFalsePositive();
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
