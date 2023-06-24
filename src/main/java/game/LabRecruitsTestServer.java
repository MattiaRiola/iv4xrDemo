/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package game;

import environments.LabRecruitsEnvironment;
import helperclasses.Util;
import logger.PrintColor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class LabRecruitsTestServer {
    String pythonScriptPath;
    private Process server;
    private Process audioRecorder;

    // cannot be called outside this class
    public LabRecruitsTestServer(Boolean useGraphics) {
        start(useGraphics, Platform.INSTALL_PATH);
    }

    public LabRecruitsTestServer(Boolean useGraphics, String redirectPath) {
        start(useGraphics, redirectPath);
    }

    public LabRecruitsTestServer(boolean useGraphics, Integer chunkLenght, String redirectPath) {
        start(useGraphics, redirectPath);
        String userDir = System.getProperty("user.dir");
        pythonScriptPath = Paths.get(userDir, "src/test/python/audio_recorder.py").toAbsolutePath().toString();
        Util.verifyPath(pythonScriptPath);

        waitFor(Process::isAlive);

    }

    public void startRecording() throws IOException {
        if (audioRecorder != null && audioRecorder.isAlive())
            throw new IllegalCallerException("The current server is still running. Close the server first by calling Close();");

        System.out.println("Starting python audio recorder... ");
        ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        audioRecorder = pb.start();
        System.out.println("Python audio recorder started");

    }

    private void start(Boolean useGraphics, String binaryPath) {
        // try to start the server

        if (Platform.isLinux())
            useGraphics = false;

        Util.verifyPath(binaryPath);

        if (server != null && server.isAlive())
            throw new IllegalCallerException("The current server is still running. Close the server first by calling Close();");

        try {
            ProcessBuilder pb = new ProcessBuilder(useGraphics ?
                    new String[]{binaryPath} :
                    new String[]{binaryPath, "-batchmode", "-nographics"});

            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            server = pb.start();
            waitFor(Process::isAlive);

        } catch (IOException e) {
            System.out.println(PrintColor.FAILURE() + ": Cannot start LabRecruits server!\n" + e.getMessage());
        }
    }

    // returns from method when the game has loaded
    public void waitForGameToLoad() {
        if(server == null)
            throw new IllegalCallerException("Cannot wait for game to load, because the server is has not started yet!");
        if (!server.isAlive())
            throw new IllegalCallerException("Cannot wait for game to load, because, the server already closed down!");

        // try to connect with an empty configuration
        new LabRecruitsEnvironment();
    }

    // close the server by destroying the process
    public void close() {
        if (server != null){

            try {
                //server.waitFor();
                server.destroy();
                server.waitFor();
                closeAudioRecorder();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void closeAudioRecorder(){
        if (audioRecorder != null){
            try {
                System.out.println("Closing Python audio recorder ...");
                audioRecorder.destroy();
                audioRecorder.waitFor();
                System.out.println("Python audio recorder closed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // check whether the process is alive
    public boolean isRunning() {
        if (server == null)
            return false;
        return server.isAlive();
    }

    // wait for a certain condition
    private void waitFor(Function<Process, Boolean> eval){
        try {
            while (!eval.apply(server))
                server.waitFor(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println(PrintColor.FAILURE() + ": Cannot run the process for " + eval.toString());
        }
    }
}
