package agents.audio;

import agents.TestSettings;
import config.audio.AudioConfig;
import entity.audio.AudioSignal;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import service.audio.AudioAnalysis;
import utils.FileExplorer;
import world.BeliefState;
import world.LabEntity;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static agents.TestSettings.USE_AUDIO_TESTING;
import static utils.FileExplorer.*;

public abstract class AudioAbstractTest {

    private static final double DETECTIOn_DELAY = 0.2d;
    static List<AudioSignal> readAudios = new LinkedList<>();

    static LabRecruitsTestServer labRecruitsTestServer;

    static final boolean SKIP_GAMEPLAY = false;

    public static final String MONSTERATTACK_WAV = "monsterattack.wav";

    public static final String DING_1_WAV = "ding1.wav";
    public static final String FIRESIZZLE_WAV = "firesizzle.wav";

    public static double delay;

    public static final StopWatch soundStopWatch = new StopWatch();

    @BeforeAll
    static void start() throws UnsupportedAudioFileException, IOException {
        Assumptions.assumeTrue(USE_AUDIO_TESTING, "audio testing disabled");
        AudioConfig.CHUNK_SIZE = 512;
        AudioConfig.FUZ_FACTOR = 2;
        List<AudioSignal> gameSounds = FileExplorer.readAllSoundsInFolder(DIR_GAME_SOUNDS);
        readAudios.addAll(gameSounds);
        AudioAnalysis.loadAudioFingerprint(gameSounds);

        if (!SKIP_GAMEPLAY) {
            String labRecruitesExeRootDir = System.getProperty("user.dir");
            labRecruitsTestServer = TestSettings.start_LabRecruitsTestServerWithAudio(labRecruitesExeRootDir, 10);
            delay = TestSettings.delayBetweenRecorderAndGame;
            soundStopWatch.start();
        }

    }


    @AfterAll
    static void close() throws IOException {
        soundStopWatch.stop();
        if (labRecruitsTestServer != null) labRecruitsTestServer.close();

        if (USE_AUDIO_TESTING && DELETE_AUDIO_ONCE_FINISHED)
            FileExplorer.deleteFilesInFolder(DIR_GAME_RECORDS);
    }


    static void addExpectedSound(Map<Double, String> expectedSounds, WorldEntity change) {
        double changeTime = (change.timestamp / 1000d)
                + -DETECTIOn_DELAY
                //+ TestSettings.delayBetweenRecorderAndGame
                ;

        double audioDuration = 1d;
        soundStopWatch.split();
        double stopWatchTime = (soundStopWatch.getSplitTime() / 1000d) - audioDuration;
        switch (change.type) {
            case LabEntity.DOOR:
                if (change.properties.get("isOpen").equals(true))
                    System.out.println("door is open (no sound associated)");
                //expectedSounds.put(changeTime, fromEntityTypeToSoundName(change.type));
                break;
            case LabEntity.SWITCH:
                if (change.properties.get("isOn").equals(true)) {
                    expectedSounds.put(stopWatchTime, fromEntityTypeToSoundName(change.type));
                }
                break;
            case LabEntity.PLAYER:
                System.out.println("player changes (no sound associated)");
//                if(change.properties.get("HP").equals(0))
                //expectedSounds.put(changeTime, fromEntityTypeToSoundName(change.type));
                break;
            case LabEntity.FIREHAZARD:
                System.out.println("fire hazard changes (no sound associated)");
//                if(change.properties.get("isOnFire").equals(true))
                //expectedSounds.put(changeTime, fromEntityTypeToSoundName(change.type));
                break;
        }

    }

    Map<String, Boolean> getButtonsDoorsState(BeliefState state) {
        Map<String, Boolean> mystate = new HashMap<>();
        for (var e : state.worldmodel.elements.values()) {
            if (e.type.equals(LabEntity.DOOR)) {
                mystate.put(e.id, state.isOpen(e.id));
            }
            if (e.type.equals(LabEntity.SWITCH)) {
                mystate.put(e.id, state.isOn(e.id));
            }
            if (e.type.equals(LabEntity.PLAYER)) {
                Serializable hpProperty = state.val("HP");
                //TODO: set player health
                //mystate.put(e.id,)
            }
            if (e.type.equals(LabEntity.FIREHAZARD)) {
                var fireHazard = e.position;
                //TODO: get info aboud FIREHAZARD
                //mystate.put(e.id,)
            }
        }
        return mystate;
    }

    public static String fromEntityTypeToSoundName(String name) {
        switch (name) {
            case LabEntity.SWITCH:
                return DING_1_WAV;
            case LabEntity.DOOR:
                return "door";
            case LabEntity.FIREHAZARD:
                return FIRESIZZLE_WAV;
            case LabEntity.ENEMY:
                return MONSTERATTACK_WAV;
            default:
                return "unknown";
        }
    }
}
