/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.demo;


import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import static agents.TestSettings.USE_AUDIO_TESTING;
import static agents.TestSettings.USE_INSTRUMENT;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class RoomReachabilityAudioTest {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
		Assumptions.assumeTrue(USE_AUDIO_TESTING,"audio testing disabled");

    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServerWithAudio(labRecruitesExeRootDir,10) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }

    void instrument(Environment env) {
    	env.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();
    }

    /**
     * A test to verify that the east closet is reachable.
     */
    @Test
    public void closetReachableTest() throws InterruptedException {

    	var buttonToTest = "button1" ;
    	var doorToTest = "door1" ;

        // Create an environment
    	var config = new LabRecruitsConfig("buttons_doors_1") ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT) instrument(environment) ;

        try {
        	TestSettings.youCanRepositionWindow() ;

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

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
	        testAgent . setTestDataCollector(dataCollector) . setGoal(testingTask) ;


	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());

	        int i = 0 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>200) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();

	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        testAgent.printStatus();
        }
        finally { environment.close(); }
    }
}
