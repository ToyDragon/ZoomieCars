package me.frogtown.zoomiecars;

public class Main {

    /**
     * Set to true when the player presses escape.
     */
    public static boolean Stop = false;

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
     * Non-player controlled car.
     */
    public static Car otherCar;

    /**
     * How curvy this portion of the road is. 0 is straight, positive turns right, negative turns left.
     */
    public static double roadCurveFactor = 0;

    /**
     * Info about the course.
     */
    public static char[] course = {
            's', 's', 's', 's', 's', 's', 's', 'r', 'r', 'r', 'r', 's', 's', 's', 's', 'l', 's', 'r', 'R', 'R', 's', 's', 's', 's', 's', 'R', 'R', 'R'
    };

    public static double GetCurveAtPos(double pos){
        int distPerSegment = 1000;
        int courseLength = course.length * distPerSegment;
        int i = (int)(pos / distPerSegment) % course.length;
        double lerp = (pos % distPerSegment)/distPerSegment;
        double thisCurve = GetCurveForChar(course[i]);
        int nextI = (i + 1) % course.length;
        double nextCurve = GetCurveForChar(course[nextI]);

        return (thisCurve * (1-lerp)) + (nextCurve * lerp);
    }

    private static double GetCurveForChar(char s){
        if(s == 's') return 0;
        if(s == 'r') return 0.5;
        if(s == 'R') return 1;
        if(s == 'l') return -0.5;
        if(s == 'L') return -1;
        return 0;
    }

    public static int GetCenterAtY(int y){
        double expDistanceModifier = Math.pow((Display.Height - y)/(double)(Display.Height - horizonY) + 1, 5);
        return Display.Width/2 + (int)(roadCurveFactor * expDistanceModifier);
    }

    public static int YAtDistance(double distance){
        for(int y = Display.Height - 1; y >= 0; y--){
            if(PosAtY(y) >= distance){
                return y;
            }
        }
        return -1;
    }

    public static int PosAtY(int y){
        return (int)playerCar.position + distanceOffsetByY[y];
    }

    public static double LowestVisibleDistance(){
        return (int)playerCar.position + distanceOffsetByY[distanceOffsetByY.length - 1];
    }

    private static void InitDistanceArray(){
        int closeThreshold = Display.Height / 4; //Items this many rows from the bottom all look the same distance to prevent warping.

        double range = (double)(Display.Height - horizonY);
        for(int y = horizonY; y < Display.Height; y++) {
            double dist = Display.Height - y;
            dist *= Math.pow(2, Math.max(dist / closeThreshold, 1));
            distanceOffsetByY[y] = (int)dist;
            distanceScalarByY[y] = Math.max((1 - (dist/750.0)), 0);
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
        long lastFrame = System.currentTimeMillis();
        thisFrame = System.currentTimeMillis();

        Display.Init();
        UI.Init();
        Resources.Init();

        playerCar = new Car(true);
        playerCar.horizontalPosition = Display.Width/2;

        otherCar = new Car(false);
        otherCar.horizontalPosition = Display.Width/2 + 10;

        distanceOffsetByY = new int[Display.Height];
        distanceScalarByY = new double[Display.Height];
        linearDistanceScalarByY = new double[Display.Height];
        horizonY = Display.Height/3;

        InitDistanceArray();

        long expectedMS = 1000 / 25;

        while(true){
            roadCurveFactor = Main.GetCurveAtPos(playerCar.position);

            UI.Render();

            playerCar.Step();
            playerCar.Render();

            //otherCar.Step();
            //otherCar.Render();;

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