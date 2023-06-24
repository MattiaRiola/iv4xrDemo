package agents;

import static agents.TestSettings.USE_GRAPHICS;
import static agents.TestSettings.USE_SERVER_FOR_TEST;

import java.util.Scanner;

import game.LabRecruitsTestServer;
import game.Platform;

/**
 * This class provides a utility method to launch a Lab Recruits game from
 * Java. This is usually used by tests (and hence the name of this class),
 * but it can be useful when launching LR from Java is needed. There are
 * several other variables which we can set to control whether the launched
 * LR instance will have its graphics visible (or not), and whether
 * instrumentation is enabled or not.
 *
 * When used in a test, the test is expected to start with a call to the method
 * start_LabRecruitsTestServer(..). Depending on the setup of some variables
 * in this class, this will either launch a fresh instance of the Lab Recruits
 * game, or it does nothing (so, the test will assume there is already an active
 * Lab Recruits instance).
 */
public class TestSettings {

	/**
	 * When set to true, the method start_LabRecruitsTestServer() will launch
	 * a fresh instance of the Lab Recruits game. Else it assumes that there is
	 * already an instance of Lab Recruits active. Using auto-launch is convenient
	 * for testing, but if high turn around is desired we might not want to do that.
	 *
	 * The default is true.
	 */
    public static boolean USE_SERVER_FOR_TEST = true ;

    /**
     * If set to true, and USE_SERVER_FOR_TEST is also true, then invoking
     * start_LabRecruitsTestServer() will launch a fresh instance of the Lab Recruits
     * game with graphics on/visible. Else the graphics is off (preferred if faster
     * tests are desired, for example).
     *
     * The default is false.
     */
    public static boolean USE_GRAPHICS = true ;

    public static boolean USE_INSTRUMENT = false;
	/**
	 * If true, audio of pc (Mixer Audio) is recorded and analysed
	 * please be sure that you selected the right output audio and be sure that nothing else is playing during the test
	 */
	public static boolean USE_AUDIO_TESTING = true;

    /**
     * If USE_SERVER_FOR_TEST is set to true, this will launch a fresh instance of the
     * Lab Recruits game.
     *
     * @param labRecruitesExeRootDir
     * @return
     */
    public static LabRecruitsTestServer start_LabRecruitsTestServer(String labRecruitesExeRootDir) {
    	LabRecruitsTestServer labRecruitsTestServer = null ;
    	if(USE_SERVER_FOR_TEST){
            labRecruitsTestServer =new LabRecruitsTestServer(
                    USE_GRAPHICS,
                    Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
            labRecruitsTestServer.waitForGameToLoad();
        }
    	return labRecruitsTestServer ;
    }

	/**
	 * If USE_SERVER_FOR_TEST is set to true, this will launch a fresh instance of the
	 * Lab Recruits game.
	 * If USE_AUDIO_TESTING is true the audio of the game will be recorded
	 *
	 * @param labRecruitesExeRootDir
	 * @param chunkLenght (in seconds)
	 * @return
	 */
	public static LabRecruitsTestServer start_LabRecruitsTestServerWithAudio(String labRecruitesExeRootDir, Integer chunkLenght) {
		LabRecruitsTestServer labRecruitsTestServer = null ;
		if(USE_SERVER_FOR_TEST){
			if(USE_AUDIO_TESTING) {
				labRecruitsTestServer = new LabRecruitsTestServer(
						USE_GRAPHICS,
						chunkLenght,
						Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
			} else{
				labRecruitsTestServer = new LabRecruitsTestServer(
						USE_GRAPHICS,
						Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
			}
			labRecruitsTestServer.waitForGameToLoad();
		}
		return labRecruitsTestServer ;
	}

    /**
     * After calling start_LabRecruitsTestServer(), and it launches an instance
     * of LR Game, you can this method to give an opportunity to the user to
     * reposition/resize the LR-window.
     */
    public static void youCanRepositionWindow() {
    	if(USE_GRAPHICS) {
    		System.out.println("You can drag then game window elsewhere for better viewing.") ;
			//new Scanner(System.in) . nextLine() ;
    	}
    }


}
