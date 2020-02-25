package me.frogtown.zoomiecars;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class Font {

    public static String Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.!,";
    public BufferedImage source;
    public Asset[] images = new Asset[Chars.length()];

    protected Font(File fontFile,int[] widths){
        try{
            source = ImageIO.read(fontFile);
            int left = 0;
            for(int charI = 0; charI < Chars.length(); charI++){
                String ch = Chars.substring(charI, charI+1);
                int width = widths[charI];
                images[charI] = new Asset(source.getSubimage(left, 0, width, source.getHeight()));
                left += width + 1;
            }
        }catch (Exception e){

        }
    }

    public static int[] GetWidth(int schar, int lchar){
        int[] widths = new int[Font.Chars.length()];
        for(int i = 0; i < widths.length; i++){
            String chara = Font.Chars.substring(i, i + 1);
            if(chara.equals("M") || chara.equals("W") || chara.equals("Q")) {
                widths[i] = lchar;
            }else{
                widths[i] = schar;
            }
        }
        return widths;
    }

    private Asset GetImage(String c){
        int pos = Chars.indexOf(c);
        if(pos >= 0){
            return images[pos];
        }
        return null;
    }

    public void DrawText(String text, int x, int y){
        int left = x;
        for(int i = 0; i < text.length(); i++){
            Asset img = GetImage(text.toUpperCase().substring(i, i+1));
            if(img != null){
                Display.Image(img, left, y);
                left += img.rgb[0].length + 1;
            }else{
                left += 5;
            }
        }
    }

    public void DrawText(String text, int x, int y, char color){
        if(color != -1){
            Display.replacements[16] = color;
        }
        int left = x;
        for(int i = 0; i < text.length(); i++){
            Asset img = GetImage(text.toUpperCase().substring(i, i+1));
            if(img != null){
                Display.Image(img, left, y);
                left += img.rgb[0].length + 1;
            }else{
                left += 5;
            }
        }
        if(color != -1){
            Display.replacements[16] = 0;
        }
    }

    public void DrawText(String text, int x, int y, char color, int charToColor, char color2){
        int left = x;
        for(int i = 0; i < text.length(); i++){
            Asset img = GetImage(text.toUpperCase().substring(i, i+1));

            if(color != -1){
                if(i == charToColor){
                    Display.replacements[16] = color2;
                }else{
                    Display.replacements[16] = color;
                }
            }

            if(img != null){
                Display.Image(img, left, y);
                left += img.rgb[0].length + 1;
            }else{
                left += GetImage("A").rgb[0].length + 1;
            }

            if(color != -1){
                Display.replacements[16] = 0;
            }
        }
    }
}
