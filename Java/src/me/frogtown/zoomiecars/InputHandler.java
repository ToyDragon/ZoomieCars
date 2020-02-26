package me.frogtown.zoomiecars;

import java.awt.event.KeyEvent;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

public class InputHandler {

    public static InputStreamReader is = new InputStreamReader(System.in);

    /**
     * Ordered list of times that the left hand pressed the correct buttons, every frame we'll add to the front and remove time older than 1s ago from the back.
     */
    public static LinkedList<Long> leftPresses = new LinkedList<Long>();

    /**
     * Ordered list of times that the right hand pressed the correct buttons, every frame we'll add to the front and remove time older than 1s ago from the back.
     */
    public static LinkedList<Long> rightPresses = new LinkedList<Long>();

    /**
     * Next character that the left hand needs to press.
     */
    public static int lChar = 0;

    /**
     * Next character that the right hand needs to press.
     */
    public static int rChar = 0;

    /**
     * List of key codes that were pressed this frame.
     */
    private static ArrayList<Integer> inputs = new ArrayList<Integer>(100);

    /**
     * Reusable array for the three ints of input that get read for each keypress.
     */
    private static int[] allInts = new int[3];

    /**
     * Get's the next keycode from the input stream. Waits 100ms maximum.
     * @return
     */
    public static int ReadNextChar(){
        try {
            int tries = 0;
            while (!InputHandler.is.ready() && tries < 10){
                tries++;
                Thread.sleep(10);
            }
            if(tries == 10){
                System.out.println("Failed to read char :(");
                return 0;
            }else {
                int val = InputHandler.is.read();
                return val;
            }
        }catch (Exception e) {
            //We don't expect the exception, just print it for debug.
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Reads the next numeric value until it reaches a non-numeric character. Consumes the terminating character.
     * @return
     */
    public static int ReadNumericValue(){
        int value = 0;
        while(true){
            int digit = NumericKeycodeToNumber(ReadNextChar());
            if(digit == -1){ break; }

            value = (value * 10) + digit;
        }

        return value;
    }

    /**
     * Probably not the best way to convert key codes to numbers, but this really isn't a performance bottleneck and is unlikely to need extending.
     * @param keyCode
     * @return
     */
    public static int NumericKeycodeToNumber(int keyCode){
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

    /**
     * Processes pending payments
     */
    public static void HandleInputs(){
        try{
            inputs.clear();
            while (is.ready()) {
                int inputPos = 0;
                while (is.ready()) {  //Three bytes come in for every keypress
                    allInts[inputPos++] = is.read();
                    if (inputPos == 1 && allInts[0] != 0x1b) {
                        break;
                    }
                    if (inputPos == 3) {
                        break;
                    }
                }

                if (inputPos == 1) {
                    inputs.add(allInts[0]);
                }
            }
            for(int input : inputs){
                if(input == KeyEvent.VK_ESCAPE){
                    Display.Cleanup();
                    Main.stop = true;
                    System.out.println("Thanks for playing wing commander");
                    System.exit(1);
                }
                else if(Character.toUpperCase(input) == KeyEvent.VK_A && lChar == 0){
                    lChar++;
                    leftPresses.addFirst(Main.thisFrame);
                }
                else if(Character.toUpperCase(input) == KeyEvent.VK_S && lChar == 1) {
                    lChar++;
                    leftPresses.addFirst(Main.thisFrame);
                }
                else if(Character.toUpperCase(input) == KeyEvent.VK_D && lChar == 2){
                    lChar = 0;
                    leftPresses.addFirst(Main.thisFrame);
                }
                else if(Character.toUpperCase(input) == KeyEvent.VK_L && rChar == 0){
                    rChar++;
                    rightPresses.addFirst(Main.thisFrame);
                }
                else if(Character.toUpperCase(input) == KeyEvent.VK_K && rChar == 1) {
                    rChar++;
                    rightPresses.addFirst(Main.thisFrame);
                }
                else if(Character.toUpperCase(input) == KeyEvent.VK_J && rChar == 2){
                    rChar = 0;
                    rightPresses.addFirst(Main.thisFrame);
                }
                else if(input == KeyEvent.VK_NUMPAD0){
                    Display.SaveDebugFrame();
                }
            }
        }catch (Exception e){}

        long cutoff = Main.thisFrame - 1000;
        while(leftPresses.size() > 0 && leftPresses.getLast() < cutoff){
            leftPresses.removeLast();
        }
        while(rightPresses.size() > 0 && rightPresses.getLast() < cutoff){
            rightPresses.removeLast();
        }
    }
}