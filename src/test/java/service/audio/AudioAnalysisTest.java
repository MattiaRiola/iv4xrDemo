package service.audio;


import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.FileExplorer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AudioAnalysisTest {


    static List<AudioSignal> readAudios = new LinkedList<>();
    public static final String DIR_BASE_AUDIO_RES = "src/test/resources/audio/";
    public static final String DIR_GAME_SOUNDS = DIR_BASE_AUDIO_RES + "game/sounds/";
    public static final String DIR_SONGS = DIR_BASE_AUDIO_RES + "songs/";
    public static final String DIR_RECORDS = DIR_BASE_AUDIO_RES + "recorded/";
    public static final String DIR_GAME_RECORDS = DIR_BASE_AUDIO_RES + "game/records/";
    public static final boolean DELETE_AUDIO_ONCE_FINISHED = true;


    @BeforeAll
    public static void init() throws UnsupportedAudioFileException, IOException {
        List<AudioSignal> gameSounds = FileExplorer.readAllSoundsInFolder(DIR_GAME_SOUNDS);
        readAudios.addAll(gameSounds);
        AudioAnalysis.loadAudioFingerprint(gameSounds);
    }


    @AfterAll
    public static void endingTest() {

    }

    @Test
    public void simpleFingerPrintTest() throws UnsupportedAudioFileException, IOException {
        String ding1FileName = "ding1.wav";
        String ding2FileName = "ding2.wav";
        String ding1CopyFileName = "ding1Copy.wav";
        AudioSignal ding1Audio = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding1FileName, ding1FileName);
        AudioSignal ding1AudioCopy = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding1CopyFileName, ding1CopyFileName);
        AudioSignal ding2Audio = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding2FileName, ding2FileName);

        assertEquals(ding1Audio.getFingerprint().keySet(), ding1AudioCopy.getFingerprint().keySet());
        assertNotEquals(ding1Audio.getFingerprint().keySet(), ding2Audio.getFingerprint().keySet());

        assertNotEquals(0, AudioAnalysis.searchMatch(ding1Audio).size());

    }

    @Test
    public void songAnalysisTest() throws IOException, UnsupportedAudioFileException, InterruptedException {
        List<AudioSignal> songs = FileExplorer.readAllSoundsInFolder(DIR_SONGS);
        AudioAnalysis.loadAudioFingerprint(songs);
        readAudios.addAll(songs);
        String resonanceRecordFileName = "Recorded-HOME-Resonance.wav";
        AudioSignal resonanceRecordedAudio = FileExplorer.readWavFile(DIR_RECORDS + resonanceRecordFileName, resonanceRecordFileName);
        Map<String, List<AudioMatch>> matches = AudioAnalysis.searchMatch(resonanceRecordedAudio);
        assertNotEquals(0, matches.size(), "no matches found");
        assertEquals(1, matches.keySet().stream().filter(k -> k.toLowerCase().contains("resonance")).count(), "no matches with resonance");
        String bestMatch = matches.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .get().getKey();
        System.out.println(matches);
        assertTrue(bestMatch.toLowerCase().contains("resonance"), "best match is not resonance");
    }







}
