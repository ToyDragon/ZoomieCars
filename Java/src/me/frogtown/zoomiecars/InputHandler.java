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
                    Main.Stop = true;
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