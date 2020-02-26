package me.frogtown.zoomiecars;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Keeps track of the frame buffers, and provides functions to write to them.
 * Colors are represented as chars because they are unsigned bytes, and fall in the range 16-231.
 * Color can be calculated as 16 + (36 x red) + (6 x green) + blue, where red/green/blue are in the range [0, 5].
 */
public class Display {

    /**
     * Measured height that can be displayed.
     */
    public static int Height = -1;

    /**
     * Measured width that can be displayed.
     */
    public static int Width = -1;

    /**
     * The ESCAPE character (like the button on the top left of your keyboard) used in terminal escape sequences.
     */
    public static char ESC = (char)0x1B;

    /**
     * Frame buffer storing the color for each pixel.
     */
    public static char[][] frame;

    /**
     * Previously rendered frame.
     */
    public static char[][] lastFrame;

    /**
     * Dictionary mapping source color to what it should be replaced with. Valid colors are 16-231, so some space is wasted here.
     */
    public static char[] replacements = new char[256];

    private static char black = (char) 16;
    private static StringBuilder builder = new StringBuilder(10000);

    public static void Init(){
        CommandMaximize();
        CommandSetTargetDimensions();
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
                    lastFrame[y][x] = black;
                }
            }
        }

        for(int i = 0; i < replacements.length; i++){
            replacements[i] = Colors.noReplacement;
        }
    }

    /**
     * Command the terminal to go to 180 rows and 240 columns if possible.
     */
    private static void CommandSetTargetDimensions(){
        int targetHeight = 180;
        int targetWidth = 240;
        System.out.print(ESC + "[8;" + (targetHeight/2) + ";" + targetWidth + "t");
    }

    /**
     * Prompts the terminal for it's size and listens for the response.
     */
    private static void CommandGetWidthAndHeight(){
        System.out.print(ESC + "[18t"); //Report the size of the text area in characters as ESC[8;ROWS;COLSt

        if(InputHandler.ReadNextChar() != KeyEvent.VK_ESCAPE) { return; }
        if(InputHandler.ReadNextChar() != KeyEvent.VK_OPEN_BRACKET) { return; }
        if(InputHandler.ReadNextChar() != KeyEvent.VK_8) { return; }
        if(InputHandler.ReadNextChar() != KeyEvent.VK_SEMICOLON) { return; }

        Height = InputHandler.ReadNumericValue() * 2; //Double height to account for our half-character shenanigans.
        Width = InputHandler.ReadNumericValue();
    }

    private static void CommandMaximize(){
        System.out.print(ESC + "[9;1t");
    }

    /**
     * Sets a single pixel's color in the output buffer with bounds checking and color replacement.
     * @param x
     * @param y
     * @param color
     */
    public static void SetPixel(int x, int y, char color){
        //Any additional checks/logic here should be duplicated in RectFill and other places that directly write to frame.

        //Bounds checks
        if(y < 0 || y >= Height) { return; }
        if(x < 0 || x >= Width) { return; }

        //Null color check. Valid colors are in the range 16-231, 0 is never used intentionally as a color.
        if(color == 0) { return; }

        //Color replacement
        if(replacements[color] != Colors.noReplacement){
            color = replacements[color];
        }
        frame[y][x] = color;
    }

    /**
     * Adds a solid color rectangle to the output buffer.
     * @param topLeftX
     * @param topLeftY
     * @param width
     * @param height
     * @param color
     */
    public static void RectFill(int topLeftX, int topLeftY, int width, int height, char color){
        //Don't display if we are out of bounds
        if(topLeftX + width < 0){ return; }
        if(topLeftX >= Display.Width){ return; }

        //Clamp X bounds into the screen
        if(topLeftX < 0){ topLeftX = 0; }
        if(topLeftX + width >= Display.Width){ width = Display.Width - topLeftX - 1; }

        //Do color replacement
        if(replacements[color] != Colors.noReplacement){
            color = replacements[color];
        }

        for(int y = topLeftY; y < topLeftY + height; y++){
            if(y >= Height) break;

            //Use System.arraycopy to exponentially fill each row. It's cool, but this probably doesn't actually speed up performance much.
            frame[y][topLeftX] = color;
            for (int i = 1; i < width; i += i) {
                System.arraycopy(frame[y], topLeftX, frame[y],topLeftX + i, ((width - i) < i) ? (width - i) : i);
            }
        }
    }

    /**
     * Adds and image to the buffer with no scaling.
     * @param image
     * @param topLeftX Top left x position of the sprite
     * @param topLeftY Top left y position of the sprite
     */
    public static void Image(Asset image, int topLeftX, int topLeftY){
        for (int y = Math.max(topLeftY, 0); y < topLeftY + image.rgb.length; y++) {
            if(y >= Height) return;
            for (int x = Math.max(topLeftX, 0); x < topLeftX + image.rgb[0].length; x++) {
                if(x >= Width) break;

                SetPixel(x, y, image.rgb[y - topLeftY][x - topLeftX]);
            }
        }
    }

    /**
     * Scales the image based on the visual distance at the Y value. Downscales by skipping rows and columns.
     * @param image
     * @param bottomCenterX Bottom center x position of the sprite
     * @param bottomCenterY Bottom center y position of the sprite
     */
    public static void ScaledImage(Asset image, int bottomCenterX, int bottomCenterY){
        //Determine scaled image size
        int clampedY = Math.min(bottomCenterY, Height - 1);
        int newHeight = (int)(image.rgb.length * Main.distanceScalarByY[clampedY]);
        int newWidth = (int)(image.rgb[0].length * Main.distanceScalarByY[clampedY]);
        if(newHeight == 0 || newWidth == 0){
            return;
        }

        //Calculate ratio of (# removed cols) / (total cols), and same for rows. This is used to know when to skip a row while copying.
        double rowSkipVal = (image.rgb[0].length - newWidth) / (double)image.rgb[0].length;
        double colSkipVal = (image.rgb.length - newHeight) / (double)image.rgb.length;

        //Calculate top left position of image
        int topLeftX =  bottomCenterX - newWidth/2;
        int topLeftY = bottomCenterY - newHeight;

        //Position in the output that we are writing to.
        int resizedX=topLeftX;
        int resizedY=topLeftY;

        //Used to track when to skip columns/rows
        double curXSkip = 0;
        double curYSkip = 0;

        for(int imageX = 0; imageX < image.rgb[0].length; imageX++){
            curXSkip += colSkipVal;
            if(curXSkip>=1){
                //Skip this entire row by allowing imageX to increment, but not resizedX.
                curXSkip--;
            }else{
                resizedX++;
                resizedY = topLeftY;
                curYSkip = 0;
                for(int imageY = 0; imageY < image.rgb.length; imageY++){
                    curYSkip += rowSkipVal;
                    if(curYSkip >= 1){
                        //Skip this column by allowing imageY to increment, but not resizedY.
                        curYSkip--;
                    }else{
                        resizedY++;

                        //We're relying on the bounds check of SetPixel here, topLeftX/Y could be negative.
                        //We could ignore the first however many rows/columns instead, but that would affect how we track which rows/cols to skip.
                        SetPixel(resizedX, resizedY, image.rgb[imageY][imageX]);
                    }
                }
            }
        }
    }

    public static void Cleanup(){
        System.out.print(ESC + "[8;;t"); //Reset height/width to default
        System.out.print(ESC + "[0m"); //Clear all attributes
        System.out.print(ESC + "[10m"); //Default font
    }

    public static void RenderFrame(){
        char top, bottom, lastTop = 0, lastBottom = 0;
        boolean needsCursorReset = true;
        boolean isFirst = true;

        //Clear the string builder while preserving the buffer.
        builder.setLength(0);

        builder.append(ESC + "[12m"); //Alternate font 2.
        //This turns the _ character into ▀ that fills the top half of the cell so we can fake double height.
        //Foreground color controls the top color, background color controls the bottom color.

        for(int y = 0; y < Height; y+=2){
            for(int x = 0; x < Width; x++){
                //Get the color of the top and bottom cells
                top = frame[y][x];
                if(y < (Height - 1)){
                    bottom = frame[y+1][x];
                }else{
                    bottom = black;
                }

                //Skip rendering this cell if it has the same top and bottom colors as the last frame.
                boolean skipThisCell = true;
                if(frame[y][x] != lastFrame[y][x]){
                    skipThisCell = false;
                }
                if(y < (Height - 1) && frame[y+1][x] != lastFrame[y+1][x]){
                    skipThisCell = false;
                }
                if(skipThisCell){
                    needsCursorReset = true;
                    continue;
                }

                //If we had previously skipped a cell we need to fast-forward to the current cell
                if(needsCursorReset){
                    int row = (y + 1)/2 + 1;
                    int col = x + 1;
                    builder.append(ESC + "[" + row + ";" + col + "H"); //set cursor position, 1 based
                }

                if(isFirst || top != lastTop){
                    builder.append(ESC + "[38;5;" + (short)top + "m"); //set foreground color
                    lastTop = top;
                }
                if(isFirst || bottom != lastBottom){
                    builder.append(ESC + "[48;5;" + (short)bottom + "m"); //set background color
                    lastBottom = bottom;
                }
                isFirst = false;

                builder.append("_"); //Character ▀ in the alternate font.
            }
        }

        //This cleanup is not really needed, but valuable for when the program is interrupted.
        builder.append(ESC + "[10m"); //Default font
        builder.append(ESC + "[0m"); //All attributes off
        builder.append(ESC + "[" + ((Height+1)/2) + ";1H"); //Move cursor to bottom left

        System.out.print(builder.toString());

        char[][] temp = lastFrame;
        lastFrame = frame;
        frame = temp;
    }

    public static void SaveDebugFrame(){
        try {
            BufferedImage debugImage = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
            int unit = 255 / 6;
            for (int y = 0; y < Height; y++) {
                for (int x = 0; x < Width; x++) {
                    int color = frame[y][x];
                    if(color < 16){color = Colors.black;}
                    color -= 16;

                    int r5 = color / 36;
                    color %= 36;

                    int g5 = color / 6;
                    color %= 6;

                    int b5 = color;
                    debugImage.setRGB(x, y, new Color(r5 * unit, g5 * unit, b5 * unit).getRGB());
                }
            }

            File file = new File("debug_frame.bmp");
            try {
                ImageIO.write(debugImage, "bmp", file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Saved debug frame to " + file.getAbsolutePath());

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
    }
}
