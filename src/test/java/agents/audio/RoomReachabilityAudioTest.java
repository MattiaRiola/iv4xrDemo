/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.audio;


import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import config.audio.ChunkSize;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import org.junit.jupiter.api.*;
import service.audio.AudioAnalysis;
import utils.FileExplorer;
import world.BeliefState;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.FileExplorer.*;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class RoomReachabilityAudioTest {
	static List<AudioSignal> readAudios = new LinkedList<>();
	private static LabRecruitsTestServer labRecruitsTestServer;

	private static final boolean SKIP_GAMEPLAY = false;

	@BeforeAll
	static void start() throws InterruptedException, UnsupportedAudioFileException, IOException {
		Assumptions.assumeTrue(USE_AUDIO_TESTING, "audio testing disabled");
		AudioAnalysis.changeConfigBySeconds(3, ChunkSize.SMALL);

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

	void instrument(Environment env) {
		env.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();
	}

	/**
	 * A test to verify that the east closet is reachable.
	 */
	@Test
	public void gameplayAudioTestDingAndFiresizzleTest() throws InterruptedException {

		var buttonToTest = "button1";
		var doorToTest = "door1";
		LabRecruitsEnvironment environment = null;
		try {
			//when
			if (!SKIP_GAMEPLAY) {// Create an environment
				environment = createLevelEnvironment();
				playLevel(environment);
				labRecruitsTestServer.closeAudioRecorder();
			}

			Map<String, Set<AudioMatch>> matches = getMatchesFromGameRecords();

			//then
			List<String> soundsFound = new LinkedList<>(matches.keySet());
			System.out.println("Matches:");
			Assertions.assertFalse(soundsFound.isEmpty(), "No matches found in the game records");
			Assertions.assertTrue(soundsFound.contains("ding1.wav"), "ding1.wav not found in the game records, found: " + soundsFound);
			Assertions.assertTrue(soundsFound.contains("ding1.wav"), "firesizzle.wav not found in the game records, found: " + soundsFound);

		} catch (IOException | UnsupportedAudioFileException e) {
			System.err.println("Error: " + e.getMessage());
		} finally {
			if (!SKIP_GAMEPLAY)
				environment.close();
		}

	}

	private LabRecruitsEnvironment createLevelEnvironment() {
		var config = new LabRecruitsConfig("buttons_doors_1");
		config.light_intensity = 0.3f;
		var environment = new LabRecruitsEnvironment(config);
		if (USE_INSTRUMENT) instrument(environment);
		return environment;
	}

	private static void playLevel(LabRecruitsEnvironment environment) throws IOException, InterruptedException {
		TestSettings.youCanRepositionWindow();
		labRecruitsTestServer.startRecording(5, 4);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		// define the testing-task:
		var testingTask = SEQ(
				GoalLib.entityInteracted("button1"),
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInvariantChecked(testAgent,
						"door1",
						"door1 should be open",
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),

				GoalLib.entityInteracted("button3"),
				GoalLib.entityStateRefreshed("door2"),
				GoalLib.entityInvariantChecked(testAgent,
						"door2",
						"door2 should be open",
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				GoalLib.entityInteracted("button4"),
				//GoalLib.entityIsInRange("button3").lift(),
				//GoalLib.entityIsInRange("door1").lift(),
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInvariantChecked(testAgent,
						"door1",
						"door1 should be open",
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				//GoalLib.entityIsInRange("button1").lift(),
				GoalLib.entityStateRefreshed("door3"),
				GoalLib.entityInvariantChecked(testAgent,
						"door3",
						"door3 should be open",
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				GoalLib.entityInCloseRange("door3")
		);
		// attaching the goal and testdata-collector
		var dataCollector = new TestDataCollector();
		testAgent.setTestDataCollector(dataCollector).setGoal(testingTask);


		environment.startSimulation(); // this will press the "Play" button in the game for you
		//goal not achieved yet
		assertFalse(testAgent.success());

		int i = 0;
		// keep updating the agent
		while (testingTask.getStatus().inProgress()) {
			if (ENABLE_VERBOSE_LOGGING)
				System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position);
			Thread.sleep(50);
			i++;
			testAgent.update();
			if (i > 200) {
				break;
			}
		}
		testingTask.printGoalStructureStatus();

		// check that we have passed both tests above:
		assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4);
		// goal status should be success
		assertTrue(testAgent.success());
		// close
		testAgent.printStatus();
	}

	private static Map<String, Set<AudioMatch>> getMatchesFromGameRecords() throws IOException, UnsupportedAudioFileException {

		List<AudioSignal> gameRecords = FileExplorer.readAllSoundsInFolder(DIR_GAME_RECORDS);
		Assertions.assertEquals(1, gameRecords.size(), "too many records in the folder, only one record is analysed");
		AudioSignal gameRecord = gameRecords.get(0);

		System.out.println("Analysing: " + gameRecord.getName());

		return AudioAnalysis.searchMatch(gameRecord);
	}
}