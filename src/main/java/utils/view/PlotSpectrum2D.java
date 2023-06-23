package utils.view;
//import require classes and packages

import entity.math.Complex;

import javax.swing.*;
import java.awt.*;

//Extends JPanel class
public class PlotSpectrum2D extends JPanel{
    //initialize coordinates
    Complex[][] spectrum;
    int marg = 60;

    boolean withLine = true;
    private boolean logModeEnabled;

    PlotSpectrum2D(){
        super();
    }

    PlotSpectrum2D(Complex[][] spectrum, boolean logModeEnabled){
        super();
        this.spectrum = spectrum;
        this.logModeEnabled = logModeEnabled;
    }

    protected void paintComponent(Graphics grf){
        //create instance of the Graphics to use its methods
        super.paintComponent(grf);
        paintSpectrum(spectrum, (Graphics2D)grf);
    }


    //main() method start
    public static void plotSpectrum(Complex[][] spectrum, String title) {

        //create an instance of JFrame class
        JFrame frame = new JFrame(title);
        // set size, layout and location for frame.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PlotSpectrum2D(spectrum, true));
        frame.setSize(600, 600);
        frame.setLocation(200, 200);
        frame.setVisible(true);
    }

    private static double findMaxMagnitude(Complex[][] spectrum) {
        double max = 0;
        for (int t = 0; t < spectrum.length; t++) {
            for (int freq = 0; freq < spectrum[t].length; freq++) {
                if (spectrum[t][freq].abs() > max) {
                    max = Math.log(spectrum[t][freq].abs() + 1);
                }
            }
        }
        return max;
    }

    public void paintSpectrum(Complex[][] results, Graphics2D g2d) {
        double magScale = 255 / findMaxMagnitude(results);
        int size = this.getSize().height;
        int blockSizeX = 10;
        int blockSizeY = 10;
        for (int i = 0; i < results.length; i++) {
            int freq = 1;
            for (int line = 1; line < size; line++) {
                // To get the magnitude of the sound at a given frequency slice
                // get the abs() from the complex number.
                // In this case I use Math.log to get a more managable number (used for color)
                double magnitude = Math.log(results[i][freq].abs() + 1);
                int scaledMagnitude = Math.min((int) (magnitude * magScale), 255);
                // The more blue in the color the more intensity for a given frequency point:
                try {
                    g2d.setColor(new Color(0, scaledMagnitude, scaledMagnitude));
                } catch (Exception e) {
                    System.err.println("magnitude: " + magnitude + "\n scaledMagnitude: " + scaledMagnitude);
                    throw e;
                }
                // Fill:
                g2d.fillRect(i * blockSizeX, (size - line) * blockSizeY, blockSizeX, blockSizeY);

                // I used a improviced logarithmic scale and normal scale:
                if (logModeEnabled && (Math.log10(line) * Math.log10(line)) > 1) {
                    freq += (int) (Math.log10(line) * Math.log10(line));
                } else {
                    freq++;
                }
            }
        }
    }
}