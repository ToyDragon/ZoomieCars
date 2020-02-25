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

    public static char skyBlue = GetColor(2, 3, 5);
    public static char grassGreen = GetColor(2, 5, 2);

    private static String leftChars = "ASD";
    private static String rightChars = "JKL";

    public static void Init()
    {
        roadStartByY = new int[Display.Height];
        roadEndByY = new int[Display.Height];
    }

    public static char GetColor(int r, int g, int b){
        return (char)(16 + (36 * r) + (6 * g) + b);
    }

    public static void DrawClouds(){
        int qWidth = Display.Width/4;

        Display.RectFill(0, 0, Display.Width, Display.Height/3, skyBlue);

        Display.Image(Resources.sun,  Display.Width * 6 / 7 - Resources.sun.rgb[0].length, 5);
        int scrollSize = (6 * qWidth) - qWidth;
        int timeOffset = (int)Main.totalMillis/750 - Resources.cloud.rgb[0].length;
        int cloudX = (timeOffset - qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 10);

        cloudX = (int)(timeOffset + 2.5*qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 25);

        cloudX = (int)(timeOffset + 4.25*qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 10);

        cloudX = (int)(timeOffset + 4.5*qWidth) % scrollSize;
        Display.Image(Resources.cloud, cloudX, 25);

        Display.RectFill(0, Display.Height/3, Display.Width, Display.Height, grassGreen);
    }

    public static void Render(){
        DrawClouds();
        DrawRoad();
        DrawTrees();
        DrawHUD();
    }

    private static void DrawHUD(){
        int qWidth = Display.Width/4;
        Display.RectFill(qWidth*2 - 55, 10, 110, 24, GetColor(0, 0, 0));
        Resources.fontA.DrawText("Left Hand", qWidth*2 - 55 + 1, 12, GetColor(5, 5, 5));
        Resources.fontB.DrawText(leftChars, qWidth*2 - 55 + 1, 21, GetColor(5, 5, 5), InputHandler.lChar, GetColor(5, 2, 2));

        Resources.fontA.DrawText("Right Hand", qWidth*2 + 5, 12, GetColor(5, 5, 5));
        Resources.fontB.DrawText(rightChars, qWidth*2 + 28, 21, GetColor(5, 5, 5), 2 - InputHandler.rChar, GetColor(5, 2, 2));

        Display.RectFill(qWidth*2 - 100, 10, 35, 24, GetColor(1, 1, 2));
        Resources.fontA.DrawText("Power", qWidth*2 - 95, 12, GetColor(5, 5, 5));
        Resources.fontB.DrawText(PadZeroes(InputHandler.leftPresses.size() + 5, 3), qWidth*2 - 95, 21, GetColor(4, 2, 2));
        Display.RectFill(qWidth*2 + 65, 10, 35, 24, GetColor(1, 1, 2));
        Resources.fontA.DrawText("Power", qWidth*2 + 70, 12, GetColor(5, 5, 5));
        Resources.fontB.DrawText(PadZeroes(InputHandler.rightPresses.size() + 5, 3), qWidth*2 + 70, 21, GetColor(4, 2, 2));

        Display.RectFill(qWidth*2 - 20, 30, 40, 24, GetColor(2, 1, 1));
        Resources.fontA.DrawText("Speed", qWidth*2 - 12, 32, GetColor(5, 5, 5));
        Resources.fontB.DrawText(PadZeroes((int)Main.playerCar.tSpeed, 3), qWidth*2 - 13, 41, GetColor(2, 5, 2));
    }

    private static void DrawTrees(){
        int treeGap = 200; //Distance between trees
        int initTreeDist = ((int)(Main.LowestVisibleDistance()/treeGap))*treeGap;
        for(int i = 7; i >= 0; i--){
            int distance = initTreeDist + i * treeGap;
            if(distance < Main.LowestVisibleDistance()){ continue; }
            boolean isLeft = (distance/treeGap)%2 == 0;
            int y = Main.YAtDistance(distance);
            if(y == -1) { continue; }
            if(isLeft){
                Display.Image(Resources.tree, roadStartByY[y] - 5, y, Main.distanceOffsetByY[y]);
            }else{
                Display.Image(Resources.tree, roadEndByY[y] + 5, y, Main.distanceOffsetByY[y]);
            }
        }
    }

    private static void DrawRoad(){
        for(int y = Main.horizonY; y < Display.Height; y++){
            int middle = Main.GetCenterAtY(y);

            int endRoadWidth = 20;
            double baseRoadWidth = Display.Width*0.90 - endRoadWidth;
            double distanceModifier = Main.linearDistanceScalarByY[y];

            int roadWidth = (int)(distanceModifier*baseRoadWidth) + endRoadWidth;
            int roadStart = middle - roadWidth/2;
            int roadEnd = middle + roadWidth/2;
            roadStartByY[y] = roadStart;
            roadEndByY[y] = roadEnd;
            Display.RectFill(roadStart, y, roadWidth, 1, GetColor(3, 3, 3));

            int lineWidth = Math.max((int)(20*distanceModifier), 2);
            double dist = Main.PosAtY(y);
            boolean isLine = ((int)(dist/125)%3) == 0;
            if(isLine) {
                Display.RectFill(middle - lineWidth / 2, y, lineWidth, 1, GetColor(5, 5, 5));
            }

            isLine = ((int)(dist/125)%2) == 0;
            if(isLine){
                Display.RectFill(roadStart - lineWidth/2, y, lineWidth, 1, GetColor(5, 2, 1));
                Display.RectFill(roadEnd - lineWidth/2, y, lineWidth, 1, GetColor(5, 2, 1));
            }else{
                Display.RectFill(roadStart - lineWidth/2, y, lineWidth, 1, GetColor(0, 0, 0));
                Display.RectFill(roadEnd - lineWidth/2, y, lineWidth, 1, GetColor(0, 0, 0));
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