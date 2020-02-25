package me.frogtown.zoomiecars;

import javax.imageio.ImageIO;
import java.io.File;

public class Resources {
    public static Asset cloud;
    public static Asset sun;
    public static Asset redcar;
    public static Asset tree;
    public static Font fontA;
    public static Font fontB;

    public static void Init(){
        try{
            redcar = new Asset(ImageIO.read(new File("./Assets/red car.bmp")));
            tree = new Asset(ImageIO.read(new File("./Assets/tree.bmp")));
            cloud = new Asset(ImageIO.read(new File("./Assets/cloud.bmp")));
            sun = new Asset(ImageIO.read(new File("./Assets/sun.bmp")));
            fontA = new FontA();
            fontB = new FontB();
        }catch (Exception e){
            System.out.println("Missing assets");
            System.exit(1);
        }
    }
}