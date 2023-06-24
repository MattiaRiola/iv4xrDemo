package utils.view;
//import require classes and packages

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

//Extends JPanel class
public class Plot2D extends JPanel{
    //initialize coordinates
    int[] cord = {0,0,0,0,0,0,0};
    int marg = 60;

    boolean withLine = true;
    private Color color = Color.RED;

    Plot2D(){
        super();
    }

    Plot2D(int[] cord){
        super();
        this.cord = cord;
    }
    Plot2D(int[] cord, Color color){
        super();
        this.cord = cord;
        this.color = color;
    }

    protected void paintComponent(Graphics grf){
        //create instance of the Graphics to use its methods
        super.paintComponent(grf);
        Graphics2D graph = (Graphics2D)grf;
        // get width and height
        int width = getWidth();
        int height = getHeight();
        double offset = (double) height /2;

        paintGraph(graph, width, height, offset);

        //find value of x and scale to plot points
        double x = (double)(width-2*marg)/(cord.length-1);
        double scale = (double)(height/2-2*marg)/(getMax());

        //set color for points
        graph.setPaint(color);

        // set points to the graph
        for(int i=0; i<cord.length; i++){
            double x1 = marg+i*x;
            double y1 = height - marg - scale * cord[i] - offset;
            graph.fill(new Ellipse2D.Double(x1 - 2, y1 - 2, 4, 4));
            if (i > 0) {
                double prevX = marg + x * (i - 1);
                double prevY = height - marg - scale * cord[i - 1] - offset;
                graph.drawLine((int) (prevX), (int) (prevY), (int) (x1), (int) (y1));
            }
        }
    }

    private void paintGraph(Graphics2D graph, int width, int height, double offset) {
        graph.setColor(Color.BLACK);

        //Sets the value of a single preference for the rendering algorithms.
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // draw graph
        graph.draw(new Line2D.Double(marg, marg, marg, height - marg));
        graph.draw(new Line2D.Double(marg, height - marg - offset, width - marg, height - marg - offset));
    }

    //create getMax() method to find maximum value
    private int getMax() {
        int max = -Integer.MAX_VALUE;
        for (int i = 0; i < cord.length; i++) {
            if (Math.abs(cord[i]) > max)
                max = Math.abs(cord[i]);

        }
        return max;
    }

    //main() method start
    public static void plotArray(int[] data, Color color){

        //create an instance of JFrame class
        JFrame frame = new JFrame();
        // set size, layout and location for frame.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocation(200, 200);
        frame.add(new Plot2D(data, color));
        frame.setVisible(true);
    }
    public static void plot2Array(int[] data1,int[] data2){

        //create an instance of JFrame class
        JFrame frame = new JFrame();
        // set size, layout and location for frame.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Plot2D(data2,Color.BLUE));
        frame.add(new Plot2D(data1,Color.RED));
        frame.setSize(600, 600);
        frame.setLocation(200, 200);
        frame.setVisible(true);
    }
}