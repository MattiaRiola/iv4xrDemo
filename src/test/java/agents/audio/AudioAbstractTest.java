package agents.audio;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import config.audio.AudioConfig;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import service.audio.AudioAnalysis;
import utils.FileExplorer;
import world.LabEntity;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.*;

import static agents.TestSettings.ENABLE_VERBOSE_LOGGING;
import static agents.TestSettings.USE_AUDIO_TESTING;
import static utils.FileExplorer.*;

public abstract class AudioAbstractTest {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    static List<AudioSignal> readAudios = new LinkedList<>();

    static LabRecruitsTestServer labRecruitsTestServer;

    static final boolean SKIP_GAMEPLAY = false;

    public static final String MONSTERATTACK_WAV = "monsterattack.wav";

    public static final String DING_1_WAV = "ding1.wav";
    public static final String FIRESIZZLE_WAV = "firesizzle.wav";


    public static final double DEFAULT_TIME_WINDOW_SIZE = 1.0d;
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

    /**
     * makes the agent progress through the environment and check if the expected sounds are played
     *
     * @param environment
     * @param testAgent
     * @param testingTask
     * @return the expected sounds and the time they were played
     * @throws InterruptedException
     */
    static Map<Double, String> progressTheAgentAndExtractExpectedSounds(LabRecruitsEnvironment environment, LabRecruitsTestAgent testAgent, GoalStructure testingTask) throws InterruptedException {
        int i = 0;
        int previousHp = 100;
        Map<Double, String> expectedSounds = new TreeMap<>(Comparator.reverseOrder());
        while (testingTask.getStatus().inProgress()) {
            if (ENABLE_VERBOSE_LOGGING)
                System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position);
            Thread.sleep(50);
            i++;
            testAgent.update();
            List<WorldEntity> changes = testAgent.state().changedEntities;
            if (environment.obs.agent.health < previousHp) {
                addExpectedDamageRelatedSound(expectedSounds, LabEntity.FIREHAZARD);
                previousHp = environment.obs.agent.health;
            }
            if (changes.size() > 0) {
                for (WorldEntity change : changes) {
                    System.out.println("_____Change: " + change);
                    addExpectedSound(expectedSounds, change);
                }
            }
            if (i > 200) {
                break;
            }
        }
        return expectedSounds;
    }

    static void addExpectedDamageRelatedSound(Map<Double, String> expectedSounds, String changeEntity) {
        double stopWatchTime = (soundStopWatch.getSplitTime() / 1000d);
        switch (changeEntity) {
            case LabEntity.ENEMY:
                System.out.println("enemy here not fire!");
                expectedSounds.put(stopWatchTime, fromEntityTypeToSoundName(LabEntity.FIREHAZARD));
                break;
            case LabEntity.FIREHAZARD:
                expectedSounds.put(stopWatchTime, fromEntityTypeToSoundName(LabEntity.FIREHAZARD));
                break;
        }
    }


    static void addExpectedSound(Map<Double, String> expectedSounds, WorldEntity change) {

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
        }

    }

    static void checkExpectedSounds(Map<Double, String> expectedSounds, Set<AudioMatch> matches) {
        System.out.println("checking expected sounds");
        expectedSounds.forEach(
                (time, type) -> {
                    var relatedAudio = readAudios.stream().filter(audioSignal -> audioSignal.getName().equals(type)).findAny();
                    if (relatedAudio.isPresent()) {
                        double audioDuration = relatedAudio.get().getAudioDuration();
                        audioDuration = DEFAULT_TIME_WINDOW_SIZE;
                        System.out.print("sound " + type + " at " + time);
                        String match = AudioAnalysis.getBestMatchAtTime(matches, time, audioDuration);
                        Assertions.assertNotNull(match, "no match found in this time window " + (time - audioDuration) + " - " + (time + audioDuration));
                        Assertions.assertEquals(type, match,
                                (time - audioDuration) + " - " + (time + audioDuration) +
                                        " match found but not the expected one, stat:\n"
                                        + AudioAnalysis.getMatchStat(AudioAnalysis.getMatchesAtTime(matches, time, audioDuration)));
                        System.out.println(ANSI_GREEN + " -> OK" + ANSI_RESET);
                    }
                }
        );
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
