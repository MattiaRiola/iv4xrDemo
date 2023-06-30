/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.audio;


import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import entity.audio.AudioMatch;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.audio.AudioAnalysis;
import world.LabEntity;
import world.LabWorldModel;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class MonsterAudioTest extends AudioAbstractTest {



	/**
	 * A test to verify that the east closet is reachable.
	 */
	@Test
	public void gameplayAudioMonsterAttackTest() throws InterruptedException {

		var buttonToTest = "button1";
		var doorToTest = "door1";
		LabRecruitsEnvironment environment = null;
		try {
			//when
			environment = createLevelEnvironment();
			Map<Double, String> expectedSound = playLevel(environment);
			labRecruitsTestServer.closeAudioRecorder();

			Set<AudioMatch> matches = getMatchesFromGameRecords();

			//then
			Assertions.assertFalse(matches.isEmpty(), "No matches found in the game records");
			Assertions.assertTrue(AudioAnalysis.getMatchWithScores(matches).containsKey(MONSTERATTACK_WAV), MONSTERATTACK_WAV + " not found in the game records");
			checkExpectedSounds(expectedSound, matches);
		} catch (IOException | UnsupportedAudioFileException e) {
			System.err.println("Error: " + e.getMessage());
		} finally {
			environment.close();
		}

	}

	private LabRecruitsEnvironment createLevelEnvironment() {
		var config = new LabRecruitsConfig("square_withEnemies");

		config.view_distance = 20f;

		LabRecruitsEnvironment environment = new LabRecruitsEnvironment(config);
		return environment;
	}

	private static Map<Double, String> playLevel(LabRecruitsEnvironment environment) throws IOException, InterruptedException {
		TestSettings.youCanRepositionWindow();
		labRecruitsTestServer.startRecording(1, 25);

		// create a test agent

		LabRecruitsTestAgent agent = createAgentWithEventProducer(environment, "agent0");

		// press play in Unity
		if (!environment.startSimulation())
			throw new InterruptedException("Unity refuses to start the Simulation!");


		var testingGoal = SEQ(GoalLib.atBGF("Finish", 1.5f, true),
				SUCCESS(),
				SUCCESS(),
				SUCCESS());

		agent.setGoal(testingGoal);


		agent.update();
		assertTrue(((LabWorldModel) agent.state().worldmodel).gameover == false);

		System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover);

		Map<Double, String> expectedSounds = progressTheAgentAndExtractExpectedSounds(environment, agent, testingGoal, 200);

		//assertTrue(((LabWorldModel) agent.state().worldmodel).gameover == true)  ;

		var wom = (LabWorldModel) agent.state().worldmodel;
		var orc1 = wom.getElement("orc1");

		assertTrue(wom.elements.values().stream().filter(e -> e.type == LabEntity.ENEMY).count() == 2);
		//assertTrue(Vec3.dist(wom.position, orc1.position) <= 1.5f) ;

		System.out.println(">>> orc1 = " + orc1);
		System.out.println(">>> orc1 prev state: " + orc1.getPreviousState());

		//add few updates:
		Thread.sleep(1000);
		wom = agent.state().env().observe("agent0");
		assertTrue(wom.health <= 90);

		if (!environment.close())
			throw new InterruptedException("Unity refuses to close the Simulation!");

		return expectedSounds;
	}
}
