package me.frogtown.zoomiecars;

public class Car{

    /**
     * Horizontal offset from center of the road.
     */
    public int horizontalPosition;

    /**
     * Position on the track
     */
    public double position;

    /**
     * The current speed of the car.
     */
    public double tSpeed = 0;

    /**
     * Whether or not this is the player controlled car.
     */
    public boolean isPlayer = false;

    /**
     * Represents a car on the track.
     * @param isPlayer
     */
    public Car(boolean isPlayer){
        this.isPlayer = isPlayer;
    }

    /**
     * Updates the speed and steps the car forward.
     */
    public void Step(){
        int qWidth = Display.Width/4;
        double hSpeed = 10;

        if(isPlayer){
            hSpeed = (InputHandler.leftPresses.size() - InputHandler.rightPresses.size())*5;
        }

        hSpeed -= Main.roadCurveFactor*10;
        hSpeed *= Main.dt;

        horizontalPosition += hSpeed;
        if(horizontalPosition < qWidth){
            horizontalPosition = qWidth;
        }
        if(horizontalPosition > qWidth*3){
            horizontalPosition = qWidth*3;
        }

        double modifier = 1.5-(Math.abs(horizontalPosition - qWidth*2)/(qWidth*2));
        tSpeed = (InputHandler.rightPresses.size() + InputHandler.leftPresses.size() + 10) * modifier;
        position += tSpeed * Main.dt * 20;
    }

    /**
     * Adds the car sprite to the display.
     */
    public void Render(){
        if(!isPlayer){
            Display.replacements[UI.GetColor(5, 0, 0)] = UI.GetColor(0, 0, 5);
            Display.replacements[UI.GetColor(4, 2, 2)] = UI.GetColor(2, 2, 5);
            Display.replacements[UI.GetColor(5, 2, 0)] = UI.GetColor(2, 0, 5);
        }
        Display.Image(Resources.redcar, horizontalPosition - Resources.redcar.rgb[0].length/2, Display.Height - Resources.redcar.rgb.length - 10);
        if(!isPlayer){
            Display.replacements[UI.GetColor(5, 0, 0)] = 0;
            Display.replacements[UI.GetColor(4, 2, 2)] = 0;
            Display.replacements[UI.GetColor(5, 2, 0)] = 0;
        }
    }
}