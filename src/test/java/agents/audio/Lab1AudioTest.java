/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.audio;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import entity.audio.AudioMatch;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A test to demonstrate using iv4xr agents to test the Lab Recruits game. The
 * testing task is to verify that a level called "lab1" can be "finished". This
 * is defined by reaching the goal-flag in the room with a fire extinguisher.
 */
public class Lab1AudioTest extends AudioAbstractTest {

    GoalStructure open_and_check_doors(LabRecruitsTestAgent testAgent, String activatingButton, String... doors) {

        GoalStructure[] subgoals = new GoalStructure[1 + 2 * doors.length];
        subgoals[0] = GoalLib.entityInteracted(activatingButton);
        for (int k = 0; k < doors.length; k++) {
            int i = 2 * k + 1;
            String doorId = doors[k];
            subgoals[i] = GoalLib.entityStateRefreshed(doorId);
            subgoals[i + 1] = GoalLib.entityInvariantChecked(testAgent, doorId, "" + doorId + " should be open",
                    (WorldEntity e) -> e.getBooleanProperty("isOpen"));
        }
        return SEQ(subgoals);
    }

    /**
     * A test to verify that fire-extinguisher room s reachable. The level has
     * however several tight corners that the agent sometimes get stuck. So, I am
     * commenting out this test until we get a better un-stuck tactic. You can still
     * uncomment it to try it out.
     */
    @Test
    public void test_the_lab_is_finishable() throws InterruptedException, UnsupportedAudioFileException, IOException {

        // Create an environment
        var config = new LabRecruitsConfig("lab1");
        config.light_intensity = 0.3f;
        var environment = new LabRecruitsEnvironment(config);

        try {
            //TestSettings.youCanRepositionWindow() ;
            labRecruitsTestServer.startRecording(1, 25);

            // create a test agent
            var testAgent = new LabRecruitsTestAgent("agent0") // matches the ID in the CSV file
                    .attachState(new BeliefState()).attachEnvironment(environment);

            // define the testing-task:
            var testingTask = SEQ(open_and_check_doors(testAgent, "b_hall_1", "d_store_e"),
                    open_and_check_doors(testAgent, "b_store", "d_store_n", "d_sidehall"),
                    GoalLib.entityInteracted("b_secret_1"),
                    open_and_check_doors(testAgent, "b_side", "d_sidehall", "d_lab_w", "d_bcroom"),
                    open_and_check_doors(testAgent, "b_secret_2", "d_closet"),
                    open_and_check_doors(testAgent, "b_closet", "d_theater_s", "d_theater_e"),
                    open_and_check_doors(testAgent, "b_lab_e", "d_tofinish"),
                    open_and_check_doors(testAgent, "b_finish", "d_finish"), GoalLib.entityInCloseRange("finish")

            );
            // attaching the goal and testdata-collector
            var dataCollector = new TestDataCollector();
            testAgent.setTestDataCollector(dataCollector).setGoal(testingTask);

            environment.startSimulation(); // this will press the "Play" button in the game for you
            // goal not achieved yet
            assertFalse(testAgent.success());

            Map<Double, String> expectedSounds = progressTheAgentAndExtractExpectedSounds(environment, testAgent, testingTask, 800);

            labRecruitsTestServer.closeAudioRecorder();
            Set<AudioMatch> matches = getMatchesFromGameRecords();
            checkExpectedOverlappedSounds(expectedSounds, matches, 3);


            // testingTask.printGoalStructureStatus();

            // check that we have passed both tests above:
            // assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4);
            // goal status should be success
            // assertTrue(testAgent.success());
            // close
            testAgent.printStatus();
        } finally {
            environment.close();
        }
    }
}
