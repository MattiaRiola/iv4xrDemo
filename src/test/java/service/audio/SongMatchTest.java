package service.audio;

import config.audio.AudioConfig;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.FileExplorer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static utils.FileExplorer.DIR_SONG_DB;
import static utils.FileExplorer.DIR_SONG_RECORDS;

public class SongMatchTest {

    static List<AudioSignal> readAudios = new LinkedList<>();


    @BeforeAll
    public static void init() throws UnsupportedAudioFileException, IOException {
        AudioConfig.CHUNK_SIZE = 1024;
        AudioConfig.FUZ_FACTOR = 1;
        List<AudioSignal> songs = FileExplorer.readAllSoundsInFolder(DIR_SONG_DB);
        readAudios.addAll(songs);
        AudioAnalysis.loadAudioFingerprint(songs);
    }

    @Test
    public void songFromStartMatchTest() throws IOException, UnsupportedAudioFileException, InterruptedException {
        String resonance0_20RecFileName = "REC 0 20 - HOME - Resonance.wav";
        AudioSignal resonanceRecordedAudio = FileExplorer.readWavFile(DIR_SONG_RECORDS + resonance0_20RecFileName, resonance0_20RecFileName.toLowerCase());
        Map<String, Set<AudioMatch>> matches = AudioAnalysis.searchMatch(resonanceRecordedAudio);
        assertNotEquals(0, matches.size(), "no matches found");
        assertEquals(1, matches.keySet().stream().filter(k -> k.toLowerCase().contains("resonance")).count(), "no matches with resonance");
        String bestMatch = AudioAnalysis.getBestMatch(matches);
        assertTrue(bestMatch.toLowerCase().contains("resonance"), "best match is not resonance");
    }

    @Test
    public void songAnalysisTest() throws IOException, UnsupportedAudioFileException, InterruptedException {
        String resonance115_123RecFileName = "REC 0 20 - HOME - Resonance.wav";
        AudioSignal resonanceRecordedAudio = FileExplorer.readWavFile(DIR_SONG_RECORDS + resonance115_123RecFileName, resonance115_123RecFileName.toLowerCase());
        Map<String, Set<AudioMatch>> matches = AudioAnalysis.searchMatch(resonanceRecordedAudio);
        assertNotEquals(0, matches.size(), "no matches found");
        assertEquals(1, matches.keySet().stream().filter(k -> k.toLowerCase().contains("resonance")).count(), "no matches with resonance");
        String bestMatch = AudioAnalysis.getBestMatch(matches);
        assertTrue(bestMatch.toLowerCase().contains("resonance"), "best match is not resonance");
    }


    @Test
    public void unknownSongMatchTest() throws IOException, UnsupportedAudioFileException, InterruptedException {
        String haruWoTsugeru0_20RecFileName = "REC - yama - Haru wo tsugeru.wav";
        AudioSignal resonanceRecordedAudio = FileExplorer.readWavFile(DIR_SONG_RECORDS + haruWoTsugeru0_20RecFileName, haruWoTsugeru0_20RecFileName.toLowerCase());
        Map<String, Set<AudioMatch>> matches = AudioAnalysis.searchMatch(resonanceRecordedAudio);
        assertEquals(0, matches.size(), "no matches found");
        assertEquals(1, matches.keySet().stream().filter(k -> k.toLowerCase().contains("resonance")).count(), "no matches with resonance");
        String bestMatch = AudioAnalysis.getBestMatch(matches);
        assertTrue(bestMatch.toLowerCase().contains("resonance"), "best match is not resonance");
    }
}