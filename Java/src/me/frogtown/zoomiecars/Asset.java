package me.frogtown.zoomiecars;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Asset {
    public char[][] rgb;
    public Asset(BufferedImage image){
        int divisor = 43;
        rgb = new char[image.getHeight()][image.getWidth()];
        for(int y = 0; y < rgb.length; y++){
            for(int x = 0; x < rgb[y].length; x++){
                Color c = new Color(image.getRGB(x, y));
                if(c.getRed() == 254 && c.getGreen() == 254 && c.getBlue() == 254){
                    rgb[y][x] = (char) 0;
                }else{
                    int r = c.getRed()/divisor;
                    int g = c.getGreen()/divisor;
                    int b = c.getBlue()/divisor;
                    rgb[y][x] = (char) (16 + (36 * r) + (6 * g) + b);
                }
            }
        }
        image.flush();
    }
}
