package agents.audio;

import agents.TestSettings;
import entity.audio.AudioSignal;
import game.LabRecruitsTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import service.audio.AudioAnalysis;
import utils.FileExplorer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static agents.TestSettings.USE_AUDIO_TESTING;
import static utils.FileExplorer.*;

public abstract class AudioAbstractTest {

    static List<AudioSignal> readAudios = new LinkedList<>();

    static LabRecruitsTestServer labRecruitsTestServer;

    static final boolean SKIP_GAMEPLAY = false;

    @BeforeAll
    static void start() throws UnsupportedAudioFileException, IOException {
        Assumptions.assumeTrue(USE_AUDIO_TESTING, "audio testing disabled");

        List<AudioSignal> gameSounds = FileExplorer.readAllSoundsInFolder(DIR_GAME_SOUNDS);
        readAudios.addAll(gameSounds);
        AudioAnalysis.loadAudioFingerprint(gameSounds);

        if (!SKIP_GAMEPLAY) {
            String labRecruitesExeRootDir = System.getProperty("user.dir");
            labRecruitsTestServer = TestSettings.start_LabRecruitsTestServerWithAudio(labRecruitesExeRootDir, 10);
        }

    }


    @AfterAll
    static void close() throws IOException {
        if (labRecruitsTestServer != null) labRecruitsTestServer.close();

        if (USE_AUDIO_TESTING && DELETE_AUDIO_ONCE_FINISHED)
            FileExplorer.deleteFilesInFolder(DIR_GAME_RECORDS);
    }
}
