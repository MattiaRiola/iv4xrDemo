package service.audio;


import entity.audio.AudioSignal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.FileExplorer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static utils.FileExplorer.DIR_GAME_SOUNDS;
import static utils.FileExplorer.DIR_MANUALLY_PLAYED;

public class GameSoundTest {


    static List<AudioSignal> readAudios = new LinkedList<>();


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
    public void sameSoundFingerPrintTest() throws UnsupportedAudioFileException, IOException {
        String ding1FileName = "ding1.wav";
        String ding2FileName = "ding2.wav";
        String ding1CopyFileName = "ding1.wav";
        AudioSignal ding1Audio = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding1FileName, ding1FileName);
        AudioSignal ding1AudioCopy = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding1CopyFileName, ding1CopyFileName);
        AudioSignal ding2Audio = FileExplorer.readWavFile(DIR_GAME_SOUNDS + ding2FileName, ding2FileName);

        assertEquals(ding1Audio.getFingerprint().keySet(), ding1AudioCopy.getFingerprint().keySet());
        assertNotEquals(ding1Audio.getFingerprint().keySet(), ding2Audio.getFingerprint().keySet());
        var matches = AudioAnalysis.searchMatch(ding1Audio);
        assertNotEquals(0, matches.size(), "no matches found");

    }


    @Test
    public void manuallyPlayedSoundsComparisonTest() throws UnsupportedAudioFileException, IOException {
        String manuallyPlayedSoundFile = "academo_record_manually_played_soundRec.wav";

        AudioSignal manuallyPlayedSound = FileExplorer.readWavFile(DIR_MANUALLY_PLAYED + manuallyPlayedSoundFile, "manually_played");
        var matches = AudioAnalysis.searchMatch(manuallyPlayedSound);
        assertNotEquals(0, matches.size(), "no matches found");
        AudioAnalysis.getAudiosWithOrderedChunksDetails(readAudios);
        AudioAnalysis.getAudiosWithOrderedChunksDetails(List.of(manuallyPlayedSound));
    }







}
