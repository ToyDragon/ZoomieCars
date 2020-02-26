package me.frogtown.zoomiecars;

import java.io.*;

public class Main {

    /**
     * Set to true when the player presses escape to indicate the program should stop.
     */
    public static boolean stop = false;

    /**
     * Items this far away shrink to 0 height.
     */
    public static double maxDisplayedDistance = 750.0;

    /**
     * Items this far behind the camera are not displayed.
     */
    public static double minDisplayedDistance = -50.0;

    /**
     * Total millis elapsed during execution.
     */
    public static long totalMillis = 0;

    /**
     * Y position of the horizon.
     */
    public static int horizonY;

    /**
     * Time modifier for this frame. Will be 1 if running at exactly target framerate, higher if the framerate is slow and we should timewarp.
     */
    public static double dt = 0;

    /**
     * Time in millis of the current frame.
     */
    public static long thisFrame;

    /**
     * Distance in front of the camera of each row on the screen
     */
    public static int[] distanceOffsetByY;

    /**
     * Exponential scalar for objects at each given Y value. Good for displaying sprites.
     */
    public static double[] distanceScalarByY;

    /**
     * Linear scalar for objects at each given Y value. Good for scaling road/terrain.
     */
    public static double[] linearDistanceScalarByY;

    /**
     * Player controlled car.
     */
    public static Car playerCar;

    /**
     * AI controlled car.
     */
    public static Car otherCar;

    /**
     * How curvy this portion of the road is. 0 is straight, positive turns right, negative turns left.
     */
    public static double roadCurveFactor = 0;

    private static FileWriter debugWriter;

    public static void Debug(String s){
        if(debugWriter != null) {
            try {
                debugWriter.write(s + "\r\n");
                debugWriter.flush();
            } catch (Exception e) {}
        }
    }

    /**
     * Info about the course.
     *   s = straight
     *   l = slight left
     *   L = hard left
     *   r = slight right
     *   R = hard right
     */
    public static char[] course = {
            's', 's', 's', 's', 's', 's', 's', 'r', 'r', 'r', 'r', 's', 's', 's', 's', 'l', 's', 'r', 'R', 'R', 's', 's', 's', 's', 's', 'R', 'R', 'R'
    };

    /**
     * Gets the curve of the track at a given position. LERP between the two nearest elements in the course array.
     * @param pos
     * @return
     */
    public static double GetCurveAtPos(double pos){
        int distPerSegment = 1000;
        int i = (int)(pos / distPerSegment) % course.length;
        double lerp = (pos % distPerSegment)/distPerSegment;
        double thisCurve = GetCurveForChar(course[i]);
        int nextI = (i + 1) % course.length;
        double nextCurve = GetCurveForChar(course[nextI]);

        return (thisCurve * (1-lerp)) + (nextCurve * lerp);
    }

    /**
     * Converts easy-to-type course characters into numbers usable for display.
     * @param s
     * @return 0 for straight, positive value for right, negative value for left.
     */
    private static double GetCurveForChar(char s){
        if(s == 's') return 0;
        if(s == 'r') return 0.5;
        if(s == 'R') return 1;
        if(s == 'l') return -0.5;
        if(s == 'L') return -1;
        return 0;
    }

    /**
     * Gets the X position of the center of the road for the given Y value, accounting for road curvature.
     * @param y
     * @return
     */
    public static int GetCenterAtY(int y){
        double expDistanceModifier = Math.pow((Display.Height - y)/(double)(Display.Height - horizonY) + 1, 5);
        return Display.Width/2 + (int)(roadCurveFactor * expDistanceModifier);
    }

    /**
     * Gets the first Y value that is at least the given position along the track.
     * @param position
     * @return
     */
    public static int YAtPosition(double position){
        if(position > PosAtY(0)){
            return -1;
        }

        if(position < PosAtY(Display.Height - 1) + minDisplayedDistance){
            return -1;
        }

        for(int y = Display.Height - 1; y > horizonY; y--){
            if(PosAtY(y) >= position){
                return y;
            }
        }

        return -1;
    }

    /**
     * Gets the first Y value that is at least the given distance from the camera.
     * @param distance
     * @return
     */
    public static int YAtDistance(double distance){
        if(distance > maxDisplayedDistance){
            return -1;
        }

        if(distance < minDisplayedDistance){
            return -1;
        }

        if(distance < 0){
            return Display.Height + (int)((30*distance)/minDisplayedDistance);
        }

        //TODO: Could be more efficient with cool maths, but this doesn't seem to be a performance bottleneck.
        for(int y = Display.Height - 1; y >= 0; y--){
            if(distanceOffsetByY[y] >= distance){
                return y;
            }
        }

        return -1;
    }

    /**
     * Gets the position along the track of the given Y position.
     * @param y
     * @return
     */
    public static int PosAtY(int y){
        return (int)playerCar.position + distanceOffsetByY[y];
    }

    /**
     * The position of the very closest portion of the screen.
     * @return
     */
    public static double LowestVisibleDistance(){
        return (int)playerCar.position + distanceOffsetByY[distanceOffsetByY.length - 1];
    }

    /**
     * Calculate the numbers that don't change between frames.
     */
    private static void InitDistanceArrays(){
        distanceOffsetByY = new int[Display.Height];
        distanceScalarByY = new double[Display.Height];
        linearDistanceScalarByY = new double[Display.Height];
        horizonY = Display.Height/3;

        int closeThreshold = Display.Height / 4; //Items this many rows from the bottom all look the same distance to prevent warping.

        double range = (double)(Display.Height - horizonY);
        for(int y = horizonY; y < Display.Height; y++) {
            double dist = Display.Height - y;
            dist *= Math.pow(2, Math.max(dist / closeThreshold, 1));
            distanceOffsetByY[y] = (int)dist;
            distanceScalarByY[y] = Math.max((1 - (dist/maxDisplayedDistance)), 0);
            linearDistanceScalarByY[y] = (y - horizonY)/range;
        }

        //Treat everything above the horizon as the same distance as the horizon
        for(int y = 0; y < horizonY; y++) {
            distanceOffsetByY[y] = distanceOffsetByY[horizonY];
            distanceScalarByY[y] = 0;
            linearDistanceScalarByY[y] = 0;
        }
    }

    public static void main(String[] args) {
        try{
            debugWriter = new FileWriter(new File("debug.log"));
        }catch(Exception e){
            System.out.println("Unable to open debug log file.");
            try{Thread.sleep(1000);}
            catch (Exception ee){}
        }
        long lastFrame = System.currentTimeMillis();
        thisFrame = System.currentTimeMillis();

        Display.Init(); //Display MUST init first, it queries the terminal for it's size which everything else relies on.
        UI.Init();
        Resources.Init();

        playerCar = new Car(true);
        playerCar.horizontalPosition = Display.Width/2;

        otherCar = new Car(false);
        otherCar.horizontalPosition = Display.Width*2/3;

        InitDistanceArrays();

        long expectedMS = 1000 / 25;

        while(true){
            roadCurveFactor = Main.GetCurveAtPos(playerCar.position);

            UI.Render();

            playerCar.Step();
            otherCar.Step();

            playerCar.Render();
            otherCar.Render();

            Display.RenderFrame();

            thisFrame = System.currentTimeMillis();
            long ellapsed = thisFrame - lastFrame;
            lastFrame = thisFrame;
            totalMillis += ellapsed;
            dt = ellapsed / 1000.0;
            long wait = Math.max(expectedMS - ellapsed, 0);
            try{
                Thread.sleep(wait);
            }catch (InterruptedException e){ /* This only happens if we do it intentionally from another thread, safe to ignore. */ }
            InputHandler.HandleInputs();
        }
    }
}