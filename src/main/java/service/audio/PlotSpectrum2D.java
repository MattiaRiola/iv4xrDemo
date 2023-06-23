package service.audio;
//import require classes and packages

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

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
    public static void plotSpectrum(Complex[][] spectrum){

        //create an instance of JFrame class
        JFrame frame = new JFrame();
        // set size, layout and location for frame.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PlotSpectrum2D(spectrum, true));
        frame.setSize(600, 600);
        frame.setLocation(200, 200);
        frame.setVisible(true);
    }
    public void paintSpectrum(Complex[][] results, Graphics2D g2d){
        int size = this.getSize().height;
        int blockSizeX = 10;
        int blockSizeY = 10;
        for(int i = 0; i < results.length; i++) {
            int freq = 1;
            for(int line = 1; line < size; line++) {
                // To get the magnitude of the sound at a given frequency slice
                // get the abs() from the complex number.
                // In this case I use Math.log to get a more managable number (used for color)
                double magnitude = Math.log(results[i][freq].abs()+1);

                // The more blue in the color the more intensity for a given frequency point:
                g2d.setColor(new Color(0,(int)magnitude*10,(int)magnitude*20));
                // Fill:
                g2d.fillRect(i*blockSizeX, (size-line)*blockSizeY,blockSizeX,blockSizeY);

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