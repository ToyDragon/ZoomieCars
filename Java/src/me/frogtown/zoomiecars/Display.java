package me.frogtown.zoomiecars;

import java.awt.event.KeyEvent;

public class Display {

    public static int Height = -1;
    public static int Width = -1;

    public static char ESC = (char)0x1B;

    public static char[][] lastFrame;
    public static char[][] frame;

    public static char[] replacements = new char[256];

    private static StringBuilder builder = new StringBuilder(10000);

    public static void Init(){
        CommandMaximize();
        CommandSetMaxDimensions();
        CommandGetWidthAndHeight();

        if(Width < 20 || Height < 20){
            System.out.println("Couldn't load screen size");
            System.exit(-1);
        }

        lastFrame = new char[Height][Width];
        frame = new char[Height][Width];

        for(int y = 0; y < lastFrame.length; y++){
            for(int x = 0; x < lastFrame[y].length; x++){
                for(int i = 0; i < lastFrame[y][x]; i++){
                    lastFrame[y][x] = (byte) 0;
                }
            }
        }

        for(int i = 0; i < replacements.length; i++){
            replacements[i] = (char) 0;
        }
    }

    private static void CommandSetMaxDimensions(){
        int targetHeight = 180;
        int targetWidth = 240;
        System.out.print(ESC + "[8;" + (targetHeight/2) + ";" + targetWidth + "t");
    }

    private static int NumericKeycodeToNumber(int keyCode){
        if(keyCode == KeyEvent.VK_0) { return 0; }
        if(keyCode == KeyEvent.VK_1) { return 1; }
        if(keyCode == KeyEvent.VK_2) { return 2; }
        if(keyCode == KeyEvent.VK_3) { return 3; }
        if(keyCode == KeyEvent.VK_4) { return 4; }
        if(keyCode == KeyEvent.VK_5) { return 5; }
        if(keyCode == KeyEvent.VK_6) { return 6; }
        if(keyCode == KeyEvent.VK_7) { return 7; }
        if(keyCode == KeyEvent.VK_8) { return 8; }
        if(keyCode == KeyEvent.VK_9) { return 9; }
        return -1;
    }

    private static int ReadNextChar(){
        try {
            while (!InputHandler.is.ready()) ;
            int val = InputHandler.is.read();
            return val;
        }catch (Exception e) {e.printStackTrace();}
        return 0;
    }

    private static void CommandGetWidthAndHeight(){
        System.out.print(ESC + "[18t");
        int width = 0;
        int height = 0;
        //TODO timeout after a second or something
        try {
            ReadNextChar(); //ESC
            ReadNextChar(); //[
            ReadNextChar(); //9
            ReadNextChar(); //;

            while(true){
                int digit = NumericKeycodeToNumber(ReadNextChar());
                if(digit == -1){ break; }

                height = (height * 10) + digit;
            }

            while(true){
                int digit = NumericKeycodeToNumber(ReadNextChar());
                if(digit == -1){ break; }

                width = (width * 10) + digit;
            }

        }catch (Exception e){}

        Width = width;
        Height = height * 2;
    }

    private static void CommandMaximize(){
        System.out.print(ESC + "[9;1t");
    }

    public static void SetPixel(int x, int y, char color){
        if(y < 0 || y >= Height) return;
        if(x < 0 || x >= Width) return;
        if(color == 0) return;
        if(replacements[color] != 0){
            color = replacements[color];
        }
        frame[y][x] = color;
    }

    public static void RectFill(int x, int y, int width, int height, char color){

        //Don't display if we are out of bounds
        if(x + width < 0){ return; }
        if(x >= Display.Width){ return; }

        //Clamp X bounds into the screen
        if(x < 0){ x = 0; }
        if(x + width >= Display.Width){ width = Display.Width - x - 1; }

        if(replacements[color] != 0){
            color = replacements[color];
        }
        for(int yy = y; yy < y + height; yy++){
            if(yy >= Height) break;

            frame[yy][x] = color;
            for (int i = 1; i < width; i += i) {
                System.arraycopy(frame[yy], x, frame[yy],x + i, ((width - i) < i) ? (width - i) : i);
            }
        }
    }

    public static void Image(Asset image, int x, int y){
        for (int yy = y; yy < y + image.rgb.length; yy++) {
            if(yy >= Height) return;
            if(yy < 0) continue;
            for (int xx = x; xx < x + image.rgb[0].length; xx++) {
                if(xx >= Width) break;
                if(xx < 0) continue;

                SetPixel(xx, yy, image.rgb[yy - y][xx - x]);
            }
        }
    }

    public static void Image(Asset image, int x, int y, int distance){
        distance = Math.max(distance,0);
        int newHeight = (int)(image.rgb.length * Math.max((1 - (distance/750.0)),0));
        int newWidth = (int)(image.rgb[0].length * Math.max((1 - (distance/750.0)),0));
        if(newHeight == 0 || newWidth == 0){
            return;
        }
        double rowSkipVal = (image.rgb[0].length - newWidth) / (double)image.rgb[0].length;
        double colSkipVal = (image.rgb.length - newHeight) / (double)image.rgb.length;
        int initX =  x - newWidth/2;
        int initY = y - newHeight;
        int srcX=0,srcY=0;
        double curXSkip = 0, curYSkip = 0;
        for(int xx = initX; xx < initX + image.rgb[0].length; xx++){
            curXSkip+=colSkipVal;
            if(curXSkip>=1){
                curXSkip--;
                continue;
            }
            srcX++;
            srcY = 0;
            curYSkip = 0;
            for(int yy = initY; yy < initY + image.rgb.length; yy++){
                curYSkip+=rowSkipVal;
                if(curYSkip>=1){
                    curYSkip--;
                    continue;
                }
                srcY++;

                if(xx < 0 || yy < 0 || xx >= Width || yy >= Height){
                    continue;
                }

                SetPixel(initX + srcX, initY + srcY, image.rgb[yy - initY][xx - initX]);
            }
        }
    }

    public static void Cleanup(){
        System.out.print(ESC + "[8;;t" + ESC + "[0m" + ESC + "[10m");
    }

    public static void RenderFrame(){
        char top,bottom;
        char nullColor = (char) -1;
        char lastTop = nullColor;
        char lastBottom = nullColor;
        char black = (char) 0;
        builder.setLength(0);
        boolean skippedCell = true;
        for(int y = 0; y < Height; y+=2){
            builder.append(ESC + "[12m");
            for(int x = 0; x < Width; x++){
                top = frame[y][x];

                if(y < (Height - 1)){
                    bottom = frame[y+1][x];
                }else{
                    bottom = black;
                }

                boolean skipThisCell = true;
                if(frame[y][x] != lastFrame[y][x]){
                    skipThisCell = false;
                }
                if(y < (Height - 1) && frame[y+1][x] != lastFrame[y+1][x]){
                    skipThisCell = false;
                }
                if(skipThisCell){
                    skippedCell = true;
                    continue;
                }

                if(skippedCell){
                    builder.append(ESC + "[" + ((y+1)/2) + ";" + (x+1) + "H");
                    lastTop = nullColor;
                    lastBottom = nullColor;
                }

                boolean skipColor = false;
                if(x != 0 && top == lastTop) {
                    skipColor = true;
                }
                if(!skipColor){
                    builder.append(ESC + "[38;5;" + (short)top + "m");
                }

                skipColor = false;
                if(x != 0 && bottom == lastBottom){
                    skipColor = true;
                }
                if(!skipColor){
                    builder.append(ESC + "[48;5;" + (short)bottom + "m");
                }

                builder.append("_");

                lastTop = top;
                lastBottom = bottom;
            }
            skippedCell = true;
        }
        builder.append(ESC + "[10m" + ESC + "[27m" + ESC + "[0m");
        builder.append(ESC + "[" + ((Height+1)/2) + ";1H");
        System.out.print(builder.toString());

        char[][] temp = lastFrame;
        lastFrame = frame;
        frame = temp;
    }
}
