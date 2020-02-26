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
     * Current rotation of the car based on turns in the track.
     */
    public double rotation;

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
            hSpeed -= Main.roadCurveFactor*10;
        }else{
            hSpeed = 0; //TODO
        }

        hSpeed *= Main.dt;

        horizontalPosition += hSpeed;
        if(horizontalPosition < qWidth){
            horizontalPosition = qWidth;
        }
        if(horizontalPosition > qWidth*3){
            horizontalPosition = qWidth*3;
        }

        rotation += Main.roadCurveFactor*10*Main.dt;

        if(isPlayer) {
            tSpeed = InputHandler.rightPresses.size() + InputHandler.leftPresses.size() + 10;
        }else{
            tSpeed = 20; //TODO
        }

        double modifier = 1.5-(Math.abs(horizontalPosition - qWidth*2)/(qWidth*2));
        position += tSpeed * modifier * Main.dt * 20;
    }

    /**
     * Adds the car sprite to the display.
     */
    public void Render(){
        if(!isPlayer){

            double distFromPlayer = position - Main.playerCar.position;
            if(distFromPlayer < -100 || distFromPlayer > 750){
                return;
            }

            int y = Main.YAtDistance(distFromPlayer);
            if(y == -1){
                return;
            }

            //Hard coded colors right out of the car image.
            Display.replacements[Colors.GetColor(5, 0, 0)] = Colors.GetColor(0, 0, 5);
            Display.replacements[Colors.GetColor(4, 2, 2)] = Colors.GetColor(2, 2, 5);
            Display.replacements[Colors.GetColor(5, 2, 0)] = Colors.GetColor(2, 0, 5);

            double horizontalScalar = horizontalPosition/(double)Display.Width;
            int clampedY = Math.min(y, Display.Height-1);
            int roadWidth = UI.roadEndByY[clampedY] - UI.roadStartByY[clampedY];
            int bottomMiddleX = (int)(UI.roadStartByY[clampedY] + horizontalScalar*roadWidth);
            Display.ScaledImage(Resources.redcar, bottomMiddleX, y);

            Display.replacements[Colors.GetColor(5, 0, 0)] = Colors.noReplacement;
            Display.replacements[Colors.GetColor(4, 2, 2)] = Colors.noReplacement;
            Display.replacements[Colors.GetColor(5, 2, 0)] = Colors.noReplacement;
        }else{
            //Don't scale the player car or anything fancy like that, just always draw it at the same y pos.
            Display.Image(Resources.redcar, horizontalPosition - Resources.redcar.rgb[0].length/2, Display.Height - Resources.redcar.rgb.length - 10);
        }
    }
}