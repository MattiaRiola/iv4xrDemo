package service.audio;


import config.audio.AudioConfig;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.FileExplorer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static config.audio.AudioConfig.FALSE_POSITIVE_THRESHOLD;
import static org.junit.jupiter.api.Assertions.*;
import static utils.FileExplorer.*;

public class GameSoundTest {


    static List<AudioSignal> readAudios = new LinkedList<>();


    @BeforeAll
    public static void init() throws UnsupportedAudioFileException, IOException {
        AudioConfig.CHUNK_SIZE = 512;
        AudioConfig.FUZ_FACTOR = 2;
        List<AudioSignal> gameSounds = FileExplorer.readAllSoundsInFolder(DIR_GAME_SOUNDS);
        readAudios.addAll(gameSounds);
        AudioAnalysis.loadAudioFingerprint(gameSounds);
    }


    @AfterAll
    public static void endingTest() {

    }

    @Test
    public void sameSoundFingerPrintTest() throws UnsupportedAudioFileException, IOException {
        String ding1FileName = "ding1.wav";
        String ding2FileName = "ding2.wav";
        String ding1CopyFileName = "ding1.wav";
        AudioSignal ding1Audio = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding1FileName, ding1FileName);
        AudioSignal ding1AudioCopy = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding1CopyFileName, ding1CopyFileName);
        AudioSignal ding2Audio = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding2FileName, ding2FileName);

        assertEquals(ding1Audio.getFingerprint().keySet(), ding1AudioCopy.getFingerprint().keySet());
        assertNotEquals(ding1Audio.getFingerprint().keySet(), ding2Audio.getFingerprint().keySet());
        Set<AudioMatch> matches = AudioAnalysis.searchMatch(ding1Audio);
        assertEquals(ding1Audio.getSpectrogram().length, matches.size(), "Some chunk aren't matched with same sound");
        assertTrue(AudioAnalysis.getBestMatch(matches).contains("ding1"), "best match is not ding1");
    }

    @Test
    public void inGameRecordedSoundsComparisonTest() throws UnsupportedAudioFileException, IOException {
        String reachabilityTestAudioFile = "RoomReachabilityAudioRecorded.wav";

        AudioSignal reachabilityTestAudio = FileExplorer.readWavFile(DIR_GAME_RECORDS_SAVED + reachabilityTestAudioFile, reachabilityTestAudioFile);
        var matches = AudioAnalysis.searchMatch(reachabilityTestAudio);
        assertNotEquals(0, matches.size(), "no matches found");

        System.out.println("Match statistics for the whole audio: " + AudioAnalysis.getMatchStat(matches));
        //firesizzle matches
        String firesizzle1 = AudioAnalysis.getBestMatchAtTime(matches, 2.0d, 1.5d);
        Set<AudioMatch> match0 = AudioAnalysis.getMatchesAtTime(matches, 2.0d, 1.5d);

        Assertions.assertEquals("firesizzle.wav", firesizzle1,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(match0));

        //ding matches

        String ding1_1 = AudioAnalysis.getBestMatchAtTime(matches, 5.0d, 0.7d);
        Set<AudioMatch> matches1 = AudioAnalysis.getMatchesAtTime(matches, 5.0d, 0.7d);
        Assertions.assertFalse(matches1.isEmpty());

        String ding1_2 = AudioAnalysis.getBestMatchAtTime(matches, 10.7d, 0.7d);
        Set<AudioMatch> matches2 = AudioAnalysis.getMatchesAtTime(matches, 10.7d, 0.7d);
        Assertions.assertFalse(matches2.isEmpty());

        String ding1_3 = AudioAnalysis.getBestMatchAtTime(matches, 13.25d, 0.7d);
        Set<AudioMatch> matches3 = AudioAnalysis.getMatchesAtTime(matches, 13.25d, 0.7d);
        Assertions.assertFalse(matches3.isEmpty());

        Assertions.assertEquals("ding1.wav", ding1_1,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches1));
        Assertions.assertEquals("ding1.wav", ding1_2,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches2));
        Assertions.assertEquals("ding1.wav", ding1_3,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches3));
    }

    @Test
    public void inGameRecordWithMonsterSoundsComparisonTest() throws UnsupportedAudioFileException, IOException {
        String monsterAudioTestRecord = "MonsterAudioTestRecorded.wav";

        AudioSignal reachabilityTestAudio = FileExplorer.readWavFile(DIR_GAME_RECORDS_SAVED + monsterAudioTestRecord, monsterAudioTestRecord);
        var matches = AudioAnalysis.searchMatch(reachabilityTestAudio);
        assertNotEquals(0, matches.size(), "no matches found");

        System.out.println("Match statistics for the whole audio: " + AudioAnalysis.getMatchStat(matches));
        //monster matches
        String monster_1 = AudioAnalysis.getBestMatchAtTime(matches, 3.0d, 1.0d);
        Set<AudioMatch> matches1 = AudioAnalysis.getMatchesAtTime(matches, 3.0d, 1.0d);

        Assertions.assertEquals("monsterattack.wav", monster_1,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches1));

        //ding matches

        String monster_2 = AudioAnalysis.getBestMatchAtTime(matches, 5.5d, 1.0d);
        Set<AudioMatch> matches2 = AudioAnalysis.getMatchesAtTime(matches, 5.5d, 1.0d);
        Assertions.assertFalse(matches2.isEmpty());

        String monster_3 = AudioAnalysis.getBestMatchAtTime(matches, 7.5d, 1.0d);
        Set<AudioMatch> matches3 = AudioAnalysis.getMatchesAtTime(matches, 7.5d, 1.0d);
        Assertions.assertFalse(matches3.isEmpty());

        String monster_4 = AudioAnalysis.getBestMatchAtTime(matches, 17.0, 1.0d);
        Set<AudioMatch> matches4 = AudioAnalysis.getMatchesAtTime(matches, 17.0d, 1.0d);
        Assertions.assertFalse(matches4.isEmpty());

        Assertions.assertEquals("monsterattack.wav", monster_3,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches4));
        Assertions.assertEquals("monsterattack.wav", monster_2,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches4));
        Assertions.assertEquals("monsterattack.wav", monster_4,
                "filtered matches in that time window: " + AudioAnalysis.getMatchStat(matches4));
    }

    @Test
    public void testAudioAbsence() throws UnsupportedAudioFileException, IOException {
        String reachabilityTestAudioFile = "RoomReachabilityAudioRecorded.wav";
        fail("test used to implement ROC graph");
        AudioSignal unkownSong = FileExplorer.readWavFile(DIR_SONG_RECORDS + reachabilityTestAudioFile, reachabilityTestAudioFile.toLowerCase());
        Set<AudioMatch> matches = AudioAnalysis.searchMatch(unkownSong);
        assertTrue(matches.size() < unkownSong.getSpectrogram().length * FALSE_POSITIVE_THRESHOLD, "matches found");

    }


}
