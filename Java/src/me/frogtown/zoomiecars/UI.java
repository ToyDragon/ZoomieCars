package me.frogtown.zoomiecars;

public class UI{

    /**
     * Screen X position that the road starts
     */
    public static int[] roadStartByY;

    /**
     * Screen Y position that the road starts
     */
    public static int[] roadEndByY;

    /**
     * Characters pressed by the left hand to make the car go zoom zoom.
     */
    private static String leftChars = "ASD";

    /**
     * Characters pressed by the right hand to make the car go zoom zoom.
     */
    private static String rightChars = "JKL";

    public static void Init()
    {
        roadStartByY = new int[Display.Height];
        roadEndByY = new int[Display.Height];
    }

    /**
     * Draws clouds, sun, grass.
     */
    public static void DrawNature(){
        int qWidth = Display.Width/4;

        Display.RectFill(0, 0, Display.Width, Display.Height/3, Colors.skyBlue);

        Display.Image(Resources.sun,  Display.Width * 6 / 7 - Resources.sun.rgb[0].length, 5);
        int scrollSize = (6 * qWidth) - qWidth;
        int timeOffset = (int)(Main.totalMillis + Main.playerCar.rotation)/750 - Resources.cloud.rgb[0].length;
        int cloudX = (timeOffset - qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 10);

        cloudX = (int)(timeOffset + 2.5*qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 25);

        cloudX = (int)(timeOffset + 4.25*qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 10);

        cloudX = (int)(timeOffset + 4.5*qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 25);

        Display.RectFill(0, Main.horizonY, Display.Width, Display.Height, Colors.grassGreen);
    }

    public static void Render(){
        DrawNature();
        DrawRoad();
        DrawTrees();
        DrawHUD();
    }

    private static void DrawHUD(){
        int qWidth = Display.Width/4;

        //Background for entire section
        Display.RectFill(qWidth*2 - 55, 10, 110, 24, Colors.black);

        //Indicator for which button the left hand needs to press
        Resources.fontA.DrawText("Left Hand", qWidth*2 - 55 + 1, 12, Colors.white);
        Resources.fontB.DrawText(leftChars, qWidth*2 - 55 + 1, 21, Colors.white, InputHandler.lChar, Colors.salmon);

        //Indicator for which button the right hand needs to press
        Resources.fontA.DrawText("Right Hand", qWidth*2 + 5, 12, Colors.white);
        Resources.fontB.DrawText(rightChars, qWidth*2 + 28, 21, Colors.white, 2 - InputHandler.rChar, Colors.salmon);

        //Partial-power generated from the left hand
        Display.RectFill(qWidth*2 - 100, 10, 35, 24, Colors.offGray);
        Resources.fontA.DrawText("Power", qWidth*2 - 95, 12, Colors.white);
        Resources.fontB.DrawText(PadZeroes(InputHandler.leftPresses.size() + 5, 3), qWidth*2 - 95, 21, Colors.softPink);

        //Partial-power generated from the right hand
        Display.RectFill(qWidth*2 + 65, 10, 35, 24, Colors.offGray);
        Resources.fontA.DrawText("Power", qWidth*2 + 70, 12, Colors.white);
        Resources.fontB.DrawText(PadZeroes(InputHandler.rightPresses.size() + 5, 3), qWidth*2 + 70, 21, Colors.softPink);

        //Total power generated from both hands together
        Display.RectFill(qWidth*2 - 20, 30, 40, 24, Colors.offGray);
        Resources.fontA.DrawText("Speed", qWidth*2 - 12, 32, Colors.white);
        Resources.fontB.DrawText(PadZeroes((int)Main.playerCar.tSpeed, 3), qWidth*2 - 13, 41, Colors.green);
    }

    private static void DrawTrees(){
        int treeGap = 200; //Distance between trees
        int initTreeDist = ((int)(Main.LowestVisibleDistance()/treeGap))*treeGap;
        for(int i = 7; i >= 0; i--){
            int distance = initTreeDist + i * treeGap;
            if(distance < Main.LowestVisibleDistance()){ continue; }
            boolean isLeft = (distance/treeGap)%2 == 0;
            int y = Main.YAtPosition(distance);
            if(y == -1) { continue; }
            if(isLeft){
                Display.ScaledImage(Resources.tree, roadStartByY[y] - 5, y);
            }else{
                Display.ScaledImage(Resources.tree, roadEndByY[y] + 5, y);
            }
        }
    }

    private static void DrawRoad(){
        int endRoadWidth = 20;
        double baseRoadWidth = Display.Width*0.90 - endRoadWidth;

        for(int y = Main.horizonY; y < Display.Height; y++){
            int middle = Main.GetCenterAtY(y);
            double distanceModifier = Main.linearDistanceScalarByY[y];

            //Draw asphalt portion of road
            int roadWidth = (int)(distanceModifier*baseRoadWidth) + endRoadWidth;
            roadStartByY[y] = middle - roadWidth/2;
            roadEndByY[y] = middle + roadWidth/2;
            Display.RectFill(roadStartByY[y], y, roadWidth, 1, Colors.asphaltGray);

            double dist = Main.PosAtY(y);
            int lineWidth = Math.max((int)(20*distanceModifier), 2); //scaled width of the road lines at this distance.

            //Dotted line in the center of the road
            boolean isLine = ((int)(dist/125)%3) == 0;
            if(isLine) {
                Display.RectFill(middle - lineWidth / 2, y, lineWidth, 1, Colors.white);
            }

            //Striping on the edge of the road
            isLine = ((int)(dist/125)%2) == 0;
            if(isLine){
                Display.RectFill(roadStartByY[y] - lineWidth/2, y, lineWidth, 1, Colors.pink);
                Display.RectFill(roadEndByY[y] - lineWidth/2, y, lineWidth, 1, Colors.pink);
            }else{
                Display.RectFill(roadStartByY[y] - lineWidth/2, y, lineWidth, 1, Colors.black);
                Display.RectFill(roadEndByY[y] - lineWidth/2, y, lineWidth, 1, Colors.black);
            }
        }
    }

    private static String PadZeroes(int val, int zeroes){
        String s = val + "";
        while(s.length() < zeroes){
            s = "0" + s;
        }
        return s;
    }
}