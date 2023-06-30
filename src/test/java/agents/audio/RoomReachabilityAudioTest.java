/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.audio;


import agents.EventsProducer;
import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import entity.audio.AudioMatch;
import entity.audio.AudioSignal;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.audio.AudioAnalysis;
import utils.FileExplorer;
import world.BeliefState;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static agents.TestSettings.USE_INSTRUMENT;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.FileExplorer.DIR_GAME_RECORDS;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class RoomReachabilityAudioTest extends AudioAbstractTest {


	void instrument(Environment env) {
		env.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();
	}

	/**
	 * A test to verify that the east closet is reachable.
	 */
	@Test
	public void gameplayAudioTestDingAndFiresizzleTest() throws InterruptedException {

		LabRecruitsEnvironment environment = null;
		try {
			//when
			environment = createLevelEnvironment();
			Map<Double, String> expectedSounds = playLevel(environment);
			labRecruitsTestServer.closeAudioRecorder();

			Set<AudioMatch> matches = getMatchesFromGameRecords();

			//then
			List<String> soundsFound = getSoundsFound(matches);
			Assertions.assertFalse(soundsFound.isEmpty(), "No matches found in the game records");
			Assertions.assertTrue(soundsFound.contains(DING_1_WAV), DING_1_WAV + " not found in the game records, found: " + soundsFound);
			Assertions.assertTrue(soundsFound.contains(FIRESIZZLE_WAV), FIRESIZZLE_WAV + " not found in the game records, found: " + soundsFound);


			Assertions.assertFalse(expectedSounds.isEmpty());

			checkExpectedSounds(expectedSounds, matches);
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

	private static Map<Double, String> playLevel(LabRecruitsEnvironment environment) throws IOException, InterruptedException {
		TestSettings.youCanRepositionWindow();
		labRecruitsTestServer.startRecording(5, 4);

		// create a test agent
		String agentId = "agent1";
		LabRecruitsTestAgent testAgent = createAgentWithEventProducer(environment, agentId);

		//init states

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
		// keep updating the agent
		var previousHp = 100;
		Map<Double, String> expectedSounds =
				progressTheAgentAndExtractExpectedSounds(environment, testAgent, testingTask);
		testingTask.printGoalStructureStatus();

		// check that we have passed both tests above:
		assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4);
		// goal status should be success
		assertTrue(testAgent.success());
		// close
		testAgent.printStatus();
		return expectedSounds;
	}


	private static LabRecruitsTestAgent createAgentWithEventProducer(LabRecruitsEnvironment environment, String agentId) {
		return new LabRecruitsTestAgent(agentId) // matches the ID in the CSV file
				.attachState(new BeliefState())
				.attachEnvironment(environment)
				.setTestDataCollector(new TestDataCollector())
				.attachSyntheticEventsProducer(new EventsProducer());
	}


	private static void updateStatesFromWorld(LabRecruitsTestAgent testAgent, Map<String, Set<WorldEntity>> objectStates) {
		var state = testAgent.state();
		//Map<String, Set<AudioEvent>> events = new TreeMap<>();
		for (var e : state.worldmodel.elements.values()) {
			var entityStates = objectStates.getOrDefault(e.id,
					new TreeSet<>(Comparator.comparing(o -> o.timestamp))
			);
			entityStates.add(e);
			objectStates.put(e.id, entityStates);
		}
	}


	private static Set<AudioMatch> getMatchesFromGameRecords() throws IOException, UnsupportedAudioFileException {

		List<AudioSignal> gameRecords = FileExplorer.readAllSoundsInFolder(DIR_GAME_RECORDS);
		Assertions.assertEquals(1, gameRecords.size(), "too many records in the folder, only one record is analysed");
		AudioSignal gameRecord = gameRecords.get(0);

		System.out.println("Analysing: " + gameRecord.getName());

		return AudioAnalysis.searchMatch(gameRecord);
	}

	/**
	 * @param matches
	 * @return return distinct sounds found in the matches
	 */
	public static List<String> getSoundsFound(Set<AudioMatch> matches) {
		return matches.stream()
				.flatMap(m -> m.getDbChunks().stream())
				.map(c -> c.name)
				.distinct()
				.collect(Collectors.toList());
	}

	public static Map<String, Integer> getSoundsFoundWithNumOfOccurencies(Set<AudioMatch> matches) {
		return matches.stream()
				.flatMap(m -> m.getDbChunks().stream())
				.map(c -> c.name)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

	}
}
