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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static utils.FileExplorer.DIR_GAME_RECORDS_SAVED;
import static utils.FileExplorer.DIR_GAME_SOUNDS;

public class GameSoundTest {


    static List<AudioSignal> readAudios = new LinkedList<>();


    @BeforeAll
    public static void init() throws UnsupportedAudioFileException, IOException {
        List<AudioSignal> gameSounds = FileExplorer.readAllSoundsInFolder(DIR_GAME_SOUNDS);
        readAudios.addAll(gameSounds);
        AudioAnalysis.loadAudioFingerprint(gameSounds);
        AudioConfig.CHUNK_SIZE = 256;
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
        AudioAnalysis.getAudiosWithOrderedChunksDetails(readAudios);
        AudioAnalysis.getAudiosWithOrderedChunksDetails(List.of(reachabilityTestAudio));

        fail("TODO: check audio matches in time windows");

    }

}
